package cvdexplorer;

import cvdexplorer.core.ScenePreparation;
import cvdexplorer.core.ScenePreparation.PreparedScene;
import cvdexplorer.model.ClusterMember;
import cvdexplorer.model.ClusterSite;
import cvdexplorer.model.SceneState;
import cvdexplorer.model.SiteMemberFactory;
import cvdexplorer.render.ClusterColorizer;
import cvdexplorer.render.HelpOverlay;
import cvdexplorer.render.MemberOverlayRenderer;
import cvdexplorer.render.RasterDiagramRenderer;
import cvdexplorer.render.SkeletonOverlayRenderer;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
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

    @Override
    public void draw(View view) {
        state.ensureClusterCountMatchesGadget();
        state.ensureActiveClusterMemberCount();

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
            Image skeleton = skeletonOverlayRenderer.render(result.ownershipGrid());
            view.drawImage(imageBox, skeleton);
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
    }

    private void updateSelectionStart(InputEvent event, Vector pointerWorld) {
        if (!dragging && event.isMouseButtonPress(1)) {
            Selection selection = nearestHandle(pointerWorld, mouseReach * pixelWidth);
            if (selection == null) {
                clearSelectionAndActiveCluster();
            } else {
                selectedClusterIndex = selection.clusterIndex();
                selectedMemberIndex = selection.memberIndex();
                selectedHandleIndex = selection.handleIndex();
                activeClusterIndex = selection.clusterIndex();
                state.activeClusterOneBased = selection.clusterIndex() + 1;
                prevGadgetActiveClusterOneBased = state.activeClusterOneBased;
                draggingStartPoint = pointerWorld;
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
        }
    }

    private void applyPointerEdits(Vector pointerWorld) {
        if (dragging && hasSelection()) {
            ClusterSite cluster = state.clusters().get(selectedClusterIndex);
            ClusterMember member = cluster.members().get(selectedMemberIndex);
            cluster.setMember(selectedMemberIndex, member.withHandle(selectedHandleIndex, pointerWorld));
        }
    }

    private void applyKeys(InputEvent event, Vector pointerWorld) {
        if (event.isKeyPress(KeyCode.H)) state.showHelp ^= true;
        if (event.isKeyPress(KeyCode.P)) state.showMembers ^= true;
        if (event.isKeyPress(KeyCode.D)) state.showDiagram ^= true;
        if (event.isKeyPress(KeyCode.K)) state.showSkeleton ^= true;
        if (event.isKeyPress(KeyCode.G)) state.snapToGrid ^= true;
        if (event.isKeyPress(KeyCode.S)) state.showShading ^= true;
        if (event.isKeyPress(KeyCode.M)) state.cycleMetric();

        if (event.isKeyPress(KeyCode.E)) {
            int n = state.clusterCount();
            if (n > 0) {
                state.activeClusterOneBased = state.activeClusterOneBased >= n ? 1 : state.activeClusterOneBased + 1;
                activeClusterIndex = state.activeClusterOneBased - 1;
                prevGadgetActiveClusterOneBased = state.activeClusterOneBased;
            }
        }

        if (event.isKeyPress(KeyCode.A)) {
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
            activeClusterIndex = state.activeClusterOneBased - 1;
            prevGadgetActiveClusterOneBased = state.activeClusterOneBased;
            clearSelection();
        }
    }

    @Override
    public void receiveEvent(View view, InputEvent event, InputState inputState, Vector pointerWorld, Vector pointerViewBase) {
        if (inputState.keyPressed(KeyCode.CONTROL)) {
            camera.receiveEvent(view, event, inputState, pointerWorld, pointerViewBase);
            return;
        }

        Vector pointer = state.snapToGrid ? pointerWorld.round(gridCellD) : pointerWorld;

        updateSelectionStart(event, pointerWorld);
        updateDraggingState(inputState, pointerWorld);
        applyPointerEdits(pointer);
        applyKeys(event, pointer);
    }

    private Selection nearestHandle(Vector pointerWorld, double radiusLimit) {
        Selection best = null;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (int clusterIndex = 0; clusterIndex < state.clusterCount(); clusterIndex++) {
            ClusterSite cluster = state.clusters().get(clusterIndex);
            for (int memberIndex = 0; memberIndex < cluster.size(); memberIndex++) {
                ClusterMember member = cluster.members().get(memberIndex);
                for (int h = 0; h < member.handleCount(); h++) {
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

    private boolean hasSelection() {
        return selectedClusterIndex >= 0 && selectedMemberIndex >= 0 && selectedHandleIndex >= 0;
    }

    private void clearSelection() {
        selectedClusterIndex = -1;
        selectedMemberIndex = -1;
        selectedHandleIndex = -1;
        dragging = false;
        draggingStartPoint = null;
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
