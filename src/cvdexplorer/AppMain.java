package cvdexplorer;

import cvdexplorer.core.ScenePreparation;
import cvdexplorer.core.ScenePreparation.PreparedScene;
import cvdexplorer.io.SceneFileIo;
import cvdexplorer.io.SceneJsonException;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.SceneState;
import cvdexplorer.model.SiteMemberFactory;
import cvdexplorer.model.SiteMemberKind;
import cvdexplorer.metric.MetricKind;
import cvdexplorer.metric.MetricMemberCompatibility;
import cvdexplorer.render.ClusterColorizer;
import cvdexplorer.render.HelpOverlay;
import cvdexplorer.render.MemberOverlayRenderer;
import cvdexplorer.render.RasterDiagramRenderer;
import cvdexplorer.render.SkeletonOverlayRenderer;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import xyz.marsavic.drawingfx.application.DrawingApplication;
import xyz.marsavic.drawingfx.application.Options;
import xyz.marsavic.drawingfx.drawing.Drawing;
import xyz.marsavic.drawingfx.drawing.DrawingUtils;
import xyz.marsavic.drawingfx.drawing.View;
import xyz.marsavic.drawingfx.gadgets.annotations.RecurseGadgets;
import xyz.marsavic.drawingfx.utils.camera.CameraSimple;
import xyz.marsavic.functions.F_R_R;
import xyz.marsavic.geometry.Box;
import xyz.marsavic.geometry.Transformation;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.input.InputEvent;
import xyz.marsavic.input.InputState;
import xyz.marsavic.input.KeyCode;

public class AppMain implements Drawing {
    public static final Vector sizeInitial = Vector.xy(960, 960);
    public static final Vector gridCellD = Vector.xy(16, 16);

    @RecurseGadgets
    final SceneState state = SceneState.demo();

    private final CameraSimple camera = new CameraSimple(F_R_R.cutoff01(t -> F_R_R.power(t, 8)));
    private final RasterDiagramRenderer rasterDiagramRenderer = new RasterDiagramRenderer();
    private final SkeletonOverlayRenderer skeletonOverlayRenderer = new SkeletonOverlayRenderer();

    private final double pointRadius = 6.0;
    private final double mouseReach = 10.0;
    private final double draggingMinDistance = 1.0;

    private double pixelWidth = 1.0;
    private boolean dragging = false;
    private Vector draggingStartPoint;
    private int activeClusterIndex = 0;
    private int selectedClusterIndex = -1;
    private int selectedMemberIndex = -1;
    private int selectedHandleIndex = -1;

    private int prevGadgetActiveClusterOneBased = 0;
    private MetricKind lastValidMetricKind = state.metricKind;
    private SiteMemberKind lastValidSiteMemberKind = state.siteMemberKind;
    private MetricKind lastRejectedMetricKind = null;
    private SiteMemberKind lastRejectedSiteMemberKind = null;
    private Integer lastRejectedClusterCount = null;
    private Integer lastRejectedMemberCount = null;
    private int lastRejectedMemberCountClusterOneBased = -1;
    private Alert activeErrorAlert = null;
    private String activeErrorDialogKey = null;

    private List<Selection> coMovingHandles = List.of();
    private Vector snapTargetPosition = null;

    // After FileChooser, Control can stay pressed in InputState; ignore it for camera until released.
    private boolean ignoreControlModifierForCamera;

    @Override
    public void draw(View view) {
        normalizeMetricSelection();
        normalizeSiteMemberKindSelection();
        normalizeClusterCountGadget();
        normalizeActiveClusterMemberCountGadget();

        int g = state.activeClusterOneBased;
        if (g != prevGadgetActiveClusterOneBased) {
            activeClusterIndex = g - 1;
            prevGadgetActiveClusterOneBased = g;
        }

        normalizeSelection();

        view.addTransformation(camera.getTransformation());
        pixelWidth = 1.0 / view.transformation().getScale();

        DrawingUtils.clear(view, state.backgroundColor());

        PreparedScene preparedScene = ScenePreparation.prepare(state);
        if (state.showDiagram || state.showSkeleton) {
            drawDiagram(view, preparedScene);
        }
        if (state.showMembers) {
            drawMembers(view, preparedScene);
        }
        if (state.showHelp) {
            HelpOverlay.draw(view);
        }
    }

    private void drawDiagram(View view, PreparedScene preparedScene) {
        Transformation transform = view.transformation();
        Box imageBox = view.nativeBox().positive();
        ClusterColorizer colorizer = new ClusterColorizer(
                preparedScene.clusters(),
                state.backgroundColor(),
                state.showShading
        );
        RasterDiagramRenderer.RenderResult result = rasterDiagramRenderer.render(
                transform.inverse(),
                imageBox,
                p -> classify(p, preparedScene),
                state.showDiagram ? colorizer::color : null
        );

        if (result == null) {
            return;
        }

        view.setTransformation(Transformation.IDENTITY);
        Image diagram = result.image();
        if (diagram != null) {
            view.drawImage(imageBox, diagram);
        }
        if (state.showSkeleton) {
            Transformation tFromPixels = transform.inverse();
            skeletonOverlayRenderer.draw(
                    view,
                    result.ownershipGrid(),
                    // Reuse the active metric so the contour position tracks the true cluster boundary.
                    (pixelPoint, clusterIndex) -> preparedScene.metric().score(
                            tFromPixels.applyTo(pixelPoint),
                            preparedScene.clusters().get(clusterIndex)
                    )
            );
        }
        view.setTransformation(transform);
    }

    private RasterDiagramRenderer.Classification classify(Vector point, PreparedScene preparedScene) {
        int bestIndex = -1;
        double bestScore = Double.POSITIVE_INFINITY;

        for (int clusterIndex = 0; clusterIndex < preparedScene.clusters().size(); clusterIndex++) {
            ClusterSite cluster = preparedScene.clusters().get(clusterIndex);
            double score = preparedScene.metric().score(point, cluster);
            if ((score < bestScore) || ((score == bestScore) && (clusterIndex < bestIndex))) {
                bestIndex = clusterIndex;
                bestScore = score;
            }
        }

        return new RasterDiagramRenderer.Classification(bestIndex, bestScore);
    }

    private void drawMembers(View view, PreparedScene preparedScene) {
        double handleR = pointRadius * pixelWidth;
        for (int clusterIndex = 0; clusterIndex < preparedScene.clusters().size(); clusterIndex++) {
            ClusterSite cluster = preparedScene.clusters().get(clusterIndex);
            boolean activeCluster = activeClusterIndex >= 0 && clusterIndex == activeClusterIndex;
            for (int memberIndex = 0; memberIndex < cluster.size(); memberIndex++) {
                ClusterMember member = cluster.members().get(memberIndex);
                boolean memberSelected = clusterIndex == selectedClusterIndex && memberIndex == selectedMemberIndex;
                int handleSel = memberSelected ? selectedHandleIndex : -1;
                MemberOverlayRenderer.drawMember(
                        view,
                        member,
                        cluster.color(),
                        activeCluster,
                        memberSelected,
                        handleSel,
                        handleR,
                        pixelWidth
                );
            }
        }
        if (snapTargetPosition != null) {
            MemberOverlayRenderer.drawSnapIndicator(view, snapTargetPosition, handleR, pixelWidth);
        }
    }

    private void updateSelectionStart(InputEvent event, Vector pointerWorld) {
        if (!dragging && event.isMouseButtonPress(1)) {
            Selection selection = nearestHandle(pointerWorld, mouseReach * pixelWidth);
            if (selection == null) {
                clearSelectionAndActiveCluster();
                coMovingHandles = List.of();
            } else {
                selectedClusterIndex = selection.clusterIndex();
                selectedMemberIndex = selection.memberIndex();
                selectedHandleIndex = selection.handleIndex();
                activeClusterIndex = selection.clusterIndex();
                state.activeClusterOneBased = selection.clusterIndex() + 1;
                prevGadgetActiveClusterOneBased = state.activeClusterOneBased;
                draggingStartPoint = pointerWorld;
                coMovingHandles = findColocatedHandles(
                        selectedClusterIndex, selectedMemberIndex, selectedHandleIndex);
            }
        }
    }

    private void updateDraggingState(InputState inputState, Vector pointerWorld) {
        if (inputState.mouseButtonPressed(1)) {
            if (draggingStartPoint != null && pointerWorld.distanceTo(draggingStartPoint) > draggingMinDistance * pixelWidth) {
                dragging = true;
            }
        } else {
            dragging = false;
            draggingStartPoint = null;
            coMovingHandles = List.of();
            snapTargetPosition = null;
        }
    }

    private void applyPointerEdits(Vector position, InputState inputState) {
        if (dragging && hasSelection()) {
            Vector target = position;
            if (state.snapToHandles) {
                target = snapToNearestHandle(position, mouseReach * pixelWidth,
                        selectedClusterIndex, selectedMemberIndex, selectedHandleIndex);
                snapTargetPosition = target.equals(position) ? null : target;
            } else {
                snapTargetPosition = null;
            }

            ClusterSite cluster = state.clusters().get(selectedClusterIndex);
            ClusterMember member = cluster.members().get(selectedMemberIndex);
            cluster.setMember(selectedMemberIndex, member.withHandle(selectedHandleIndex, target));

            boolean shiftHeld = inputState.keyPressed(KeyCode.SHIFT);
            if (!shiftHeld) {
                for (Selection co : coMovingHandles) {
                    ClusterSite coCluster = state.clusters().get(co.clusterIndex());
                    ClusterMember coMember = coCluster.members().get(co.memberIndex());
                    coCluster.setMember(co.memberIndex(), coMember.withHandle(co.handleIndex(), target));
                }
            }
        } else {
            snapTargetPosition = null;
        }
    }

    private void applyKeys(InputEvent event, Vector pointerWorld) {
        if (event.isKeyPress(KeyCode.H)) state.showHelp ^= true;
        if (event.isKeyPress(KeyCode.P)) state.showMembers ^= true;
        if (event.isKeyPress(KeyCode.D)) state.showDiagram ^= true;
        if (event.isKeyPress(KeyCode.K)) state.showSkeleton ^= true;
        if (event.isKeyPress(KeyCode.G)) state.snapToGrid ^= true;
        if (event.isKeyPress(KeyCode.F)) state.snapToHandles ^= true;
        if (event.isKeyPress(KeyCode.S)) state.showShading ^= true;
        if (event.isKeyPress(KeyCode.M)) cycleMetricWithValidation();

        if (event.isKeyPress(KeyCode.E)) {
            int n = state.clusterCount();
            if (n > 0) {
                state.activeClusterOneBased = state.activeClusterOneBased >= n ? 1 : state.activeClusterOneBased + 1;
                activeClusterIndex = state.activeClusterOneBased - 1;
                prevGadgetActiveClusterOneBased = state.activeClusterOneBased;
            }
        }

        if (event.isKeyPress(KeyCode.A)) {
            String invalidAddMessage = MetricMemberCompatibility.invalidNewMemberMessage(state.metricKind, state.siteMemberKind)
                    .orElse(null);
            if (invalidAddMessage != null) {
                state.siteMemberKind = lastValidSiteMemberKind;
                showCompatibilityError(invalidAddMessage);
                return;
            }
            int clusterIdx = state.activeClusterOneBased - 1;
            ClusterSite cluster = state.clusters().get(clusterIdx);
            cluster.addMember(SiteMemberFactory.createDefault(state.siteMemberKind, clusterIdx, cluster.size(), pointerWorld));
            activeClusterIndex = clusterIdx;
            prevGadgetActiveClusterOneBased = state.activeClusterOneBased;
            state.targetPointCountForActiveCluster = cluster.size();
            selectedClusterIndex = clusterIdx;
            selectedMemberIndex = cluster.size() - 1;
            selectedHandleIndex = 0;
        }

        if (event.isKeyPress(KeyCode.X) && hasSelection()) {
            ClusterSite cluster = state.clusters().get(selectedClusterIndex);
            if (cluster.size() > 1) {
                cluster.removeMember(selectedMemberIndex);
                selectedMemberIndex = Math.min(selectedMemberIndex, cluster.size() - 1);
                selectedHandleIndex = Math.min(selectedHandleIndex, cluster.members().get(selectedMemberIndex).handleCount() - 1);
                if (selectedClusterIndex == state.activeClusterOneBased - 1) {
                    state.targetPointCountForActiveCluster = cluster.size();
                }
            }
        }

        if (event.isKeyPress(KeyCode.N)) {
            state.copyFrom(SceneState.demo());
            lastValidMetricKind = state.metricKind;
            lastValidSiteMemberKind = state.siteMemberKind;
            clearRejectedGadgetAttempts();
            activeClusterIndex = state.activeClusterOneBased - 1;
            prevGadgetActiveClusterOneBased = state.activeClusterOneBased;
            clearSelection();
        }
    }

    @Override
    public void receiveEvent(View view, InputEvent event, InputState inputState, Vector pointerWorld, Vector pointerViewBase) {
        if (ignoreControlModifierForCamera
                && (!inputState.keyPressed(KeyCode.CONTROL) || event.isKeyRelease(KeyCode.CONTROL))) {
            ignoreControlModifierForCamera = false;
        }

        if (event.isKeyPress(KeyCode.S) && inputState.keyPressed(KeyCode.CONTROL)) {
            saveSceneToFile();
            return;
        }
        if (event.isKeyPress(KeyCode.O) && inputState.keyPressed(KeyCode.CONTROL)) {
            loadSceneFromFile();
            return;
        }
        boolean controlForCamera = inputState.keyPressed(KeyCode.CONTROL) && !ignoreControlModifierForCamera;
        if (controlForCamera && !event.isKey()) {
            camera.receiveEvent(view, event, inputState, pointerWorld, pointerViewBase);
            return;
        }

        Vector pointer = state.snapToGrid ? pointerWorld.round(gridCellD) : pointerWorld;

        updateSelectionStart(event, pointerWorld);
        updateDraggingState(inputState, pointerWorld);
        applyPointerEdits(pointer, inputState);
        applyKeys(event, pointer);
    }

    private Selection nearestHandle(Vector pointerWorld, double radiusLimit) {
        Selection best = null;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (int clusterIndex = 0; clusterIndex < state.clusterCount(); clusterIndex++) {
            ClusterSite cluster = state.clusters().get(clusterIndex);
            for (int memberIndex = 0; memberIndex < cluster.size(); memberIndex++) {
                ClusterMember member = cluster.members().get(memberIndex);
                boolean memberSelected = clusterIndex == selectedClusterIndex && memberIndex == selectedMemberIndex;
                for (int h = 0; h < member.handleCount(); h++) {
                    if (!HandleVisibility.isVisible(member, h, memberSelected)) {
                        continue;
                    }
                    double distance = member.getHandle(h).distanceTo(pointerWorld);
                    if (distance < bestDistance && distance < radiusLimit) {
                        bestDistance = distance;
                        best = new Selection(clusterIndex, memberIndex, h);
                    }
                }
            }
        }

        return best;
    }

    private Vector snapToNearestHandle(Vector position, double snapRadius,
            int excludeCluster, int excludeMember, int excludeHandle) {
        double bestDistance = Double.POSITIVE_INFINITY;
        Vector bestPosition = null;

        for (int ci = 0; ci < state.clusterCount(); ci++) {
            ClusterSite cluster = state.clusters().get(ci);
            for (int mi = 0; mi < cluster.size(); mi++) {
                ClusterMember member = cluster.members().get(mi);
                boolean memberSelected = ci == selectedClusterIndex && mi == selectedMemberIndex;
                for (int h = 0; h < member.handleCount(); h++) {
                    if (ci == excludeCluster && mi == excludeMember && h == excludeHandle) {
                        continue;
                    }
                    if (!HandleVisibility.isVisible(member, h, memberSelected)) {
                        continue;
                    }
                    Vector handlePos = member.getHandle(h);
                    double distance = handlePos.distanceTo(position);
                    if (distance < bestDistance && distance < snapRadius) {
                        bestDistance = distance;
                        bestPosition = handlePos;
                    }
                }
            }
        }

        return bestPosition != null ? bestPosition : position;
    }

    private List<Selection> findColocatedHandles(int clusterIndex, int memberIndex, int handleIndex) {
        ClusterSite cluster = state.clusters().get(clusterIndex);
        Vector position = cluster.members().get(memberIndex).getHandle(handleIndex);
        List<Selection> result = new ArrayList<>();

        for (int mi = 0; mi < cluster.size(); mi++) {
            ClusterMember member = cluster.members().get(mi);
            boolean memberSelected = clusterIndex == selectedClusterIndex && mi == selectedMemberIndex;
            for (int h = 0; h < member.handleCount(); h++) {
                if (mi == memberIndex && h == handleIndex) {
                    continue;
                }
                if (!HandleVisibility.isVisible(member, h, memberSelected)) {
                    continue;
                }
                if (member.getHandle(h).equals(position)) {
                    result.add(new Selection(clusterIndex, mi, h));
                }
            }
        }

        return result;
    }

    private boolean hasSelection() {
        return selectedClusterIndex >= 0 && selectedMemberIndex >= 0 && selectedHandleIndex >= 0;
    }

    private void clearSelection() {
        selectedClusterIndex = -1;
        selectedMemberIndex = -1;
        selectedHandleIndex = -1;
        dragging = false;
        draggingStartPoint = null;
        coMovingHandles = List.of();
        snapTargetPosition = null;
    }

    private void clearSelectionAndActiveCluster() {
        clearSelection();
        activeClusterIndex = -1;
    }

    private void normalizeSelection() {
        if (state.clusterCount() == 0) {
            activeClusterIndex = -1;
            clearSelection();
            return;
        }

        if (activeClusterIndex >= state.clusterCount()) {
            activeClusterIndex = state.clusterCount() - 1;
        }

        if (!hasSelection()) {
            return;
        }

        if (selectedClusterIndex >= state.clusterCount()) {
            clearSelection();
            return;
        }

        ClusterSite cluster = state.clusters().get(selectedClusterIndex);
        if (selectedMemberIndex >= cluster.size()) {
            clearSelection();
            return;
        }

        ClusterMember member = cluster.members().get(selectedMemberIndex);
        if (selectedHandleIndex >= member.handleCount()) {
            selectedHandleIndex = member.handleCount() - 1;
        }
    }

    private record Selection(int clusterIndex, int memberIndex, int handleIndex) {
    }

    /** FileChooser needs a window; any showing JavaFX window is enough. */
    private static Window firstShowingWindow() {
        for (Window w : Window.getWindows()) {
            if (w.isShowing()) {
                return w;
            }
        }
        return null;
    }

    private void afterNativeFileDialog() {
        ignoreControlModifierForCamera = true;
        Window w = firstShowingWindow();
        if (w != null) {
            Platform.runLater(w::requestFocus);
        }
    }

    private void cycleMetricWithValidation() {
        MetricKind nextMetricKind = nextMetricKind(state.metricKind);
        String invalidMetricMessage = MetricMemberCompatibility.invalidMetricMessage(nextMetricKind, state.clusters())
                .orElse(null);
        if (invalidMetricMessage != null) {
            showCompatibilityError(invalidMetricMessage);
            return;
        }
        state.metricKind = nextMetricKind;
        lastValidMetricKind = nextMetricKind;
    }

    private void normalizeMetricSelection() {
        MetricKind selectedMetricKind = state.metricKind;
        if (selectedMetricKind == lastValidMetricKind) {
            lastRejectedMetricKind = null;
            return;
        }
        if (selectedMetricKind == lastRejectedMetricKind) {
            state.metricKind = lastValidMetricKind;
            return;
        }

        String invalidMetricMessage = MetricMemberCompatibility.invalidMetricMessage(selectedMetricKind, state.clusters())
                .orElse(null);
        if (invalidMetricMessage != null) {
            lastRejectedMetricKind = selectedMetricKind;
            state.metricKind = lastValidMetricKind;
            showCompatibilityError(invalidMetricMessage);
            return;
        }
        lastRejectedMetricKind = null;
        lastValidMetricKind = selectedMetricKind;
    }

    private void normalizeSiteMemberKindSelection() {
        SiteMemberKind selectedSiteMemberKind = state.siteMemberKind;
        if (selectedSiteMemberKind == lastValidSiteMemberKind) {
            lastRejectedSiteMemberKind = null;
            return;
        }
        if (selectedSiteMemberKind == lastRejectedSiteMemberKind) {
            state.siteMemberKind = lastValidSiteMemberKind;
            return;
        }

        String invalidNewMemberMessage = MetricMemberCompatibility.invalidNewMemberMessage(state.metricKind, selectedSiteMemberKind)
                .orElse(null);
        if (invalidNewMemberMessage != null) {
            lastRejectedSiteMemberKind = selectedSiteMemberKind;
            state.siteMemberKind = lastValidSiteMemberKind;
            showCompatibilityError(invalidNewMemberMessage);
            return;
        }
        lastRejectedSiteMemberKind = null;
        lastValidSiteMemberKind = selectedSiteMemberKind;
    }

    private static MetricKind nextMetricKind(MetricKind metricKind) {
        return switch (metricKind) {
            case MINIMUM_DISTANCE -> MetricKind.MAXIMUM_DISTANCE;
            case MAXIMUM_DISTANCE -> MetricKind.SUM_OF_DISTANCES;
            case SUM_OF_DISTANCES -> MetricKind.MINIMUM_DISTANCE;
        };
    }

    private void showCompatibilityError(String message) {
        showErrorDialog("Unsupported metric/member combination", message);
    }

    private void normalizeClusterCountGadget() {
        int requestedClusterCount = state.numberOfClusters;
        String compatibilityError = state.ensureClusterCountMatchesGadget().orElse(null);
        if (compatibilityError == null) {
            lastRejectedClusterCount = null;
            return;
        }
        if (!Integer.valueOf(requestedClusterCount).equals(lastRejectedClusterCount)) {
            lastRejectedClusterCount = requestedClusterCount;
            showCompatibilityError(compatibilityError);
        }
    }

    private void normalizeActiveClusterMemberCountGadget() {
        int requestedClusterOneBased = state.activeClusterOneBased;
        int requestedMemberCount = state.targetPointCountForActiveCluster;
        String compatibilityError = state.ensureActiveClusterMemberCount().orElse(null);
        if (compatibilityError == null) {
            lastRejectedMemberCount = null;
            lastRejectedMemberCountClusterOneBased = -1;
            return;
        }
        boolean repeatedRejectedRequest =
                requestedClusterOneBased == lastRejectedMemberCountClusterOneBased
                        && Integer.valueOf(requestedMemberCount).equals(lastRejectedMemberCount);
        if (!repeatedRejectedRequest) {
            lastRejectedMemberCountClusterOneBased = requestedClusterOneBased;
            lastRejectedMemberCount = requestedMemberCount;
            showCompatibilityError(compatibilityError);
        }
    }

    private void clearRejectedGadgetAttempts() {
        lastRejectedMetricKind = null;
        lastRejectedSiteMemberKind = null;
        lastRejectedClusterCount = null;
        lastRejectedMemberCount = null;
        lastRejectedMemberCountClusterOneBased = -1;
    }

    private void showErrorDialog(String header, String message) {
        Platform.runLater(() -> {
            String dialogKey = header + "\n" + message;
            if (activeErrorAlert != null && activeErrorAlert.isShowing()) {
                if (dialogKey.equals(activeErrorDialogKey)) {
                    focusActiveErrorAlert();
                    return;
                }
                activeErrorAlert.setHeaderText(header);
                activeErrorAlert.setContentText(message);
                activeErrorDialogKey = dialogKey;
                focusActiveErrorAlert();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cluster Voronoi Explorer");
            alert.setHeaderText(header);
            alert.setContentText(message);
            Window owner = firstShowingWindow();
            if (owner != null) {
                alert.initOwner(owner);
            }
            alert.setOnHidden(event -> {
                if (activeErrorAlert == alert) {
                    activeErrorAlert = null;
                    activeErrorDialogKey = null;
                }
            });
            activeErrorAlert = alert;
            activeErrorDialogKey = dialogKey;
            alert.show();
        });
    }

    private void focusActiveErrorAlert() {
        if (activeErrorAlert == null) {
            return;
        }
        Window window = activeErrorAlert.getDialogPane().getScene() == null
                ? null
                : activeErrorAlert.getDialogPane().getScene().getWindow();
        if (window != null) {
            window.requestFocus();
        }
    }

    private void saveSceneToFile() {
        // JSON schema: see SceneJsonCodec (version "1", clusters, metricKind, siteMemberKind).
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save scene");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON scene", "*.json"));
        File file = chooser.showSaveDialog(firstShowingWindow());
        afterNativeFileDialog();
        if (file == null) {
            return;
        }
        if (!file.getName().toLowerCase().endsWith(".json")) {
            file = new File(file.getParentFile(), file.getName() + ".json");
        }
        try {
            SceneFileIo.save(file.toPath(), state);
        } catch (IOException e) {
            showErrorDialog("Save failed", e.getMessage());
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    private void loadSceneFromFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load scene");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON scene", "*.json"));
        File file = chooser.showOpenDialog(firstShowingWindow());
        afterNativeFileDialog();
        if (file == null) {
            return;
        }
        try {
            SceneFileIo.load(state, file.toPath());
            lastValidMetricKind = state.metricKind;
            lastValidSiteMemberKind = state.siteMemberKind;
            clearRejectedGadgetAttempts();
            // Match gadget-driven active cluster and clear stale selection indices.
            activeClusterIndex = state.activeClusterOneBased - 1;
            prevGadgetActiveClusterOneBased = state.activeClusterOneBased;
            clearSelection();
        } catch (SceneJsonException e) {
            showErrorDialog("Load failed", e.getMessage());
            System.err.println("Load failed: " + e.getMessage());
        } catch (IOException e) {
            showErrorDialog("Load failed", e.getMessage());
            System.err.println("Load failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Options options = Options.redrawOnEvents();
        options.windowTitle = "Cluster Voronoi Explorer";
        options.drawingSize = sizeInitial;
        options.gridSubdivision = 8;
        options.gridInterval = gridCellD.x() * options.gridSubdivision;
        options.gridColor = Color.gray(0.0, 0.12);
        DrawingApplication.launch(options);
    }
}
