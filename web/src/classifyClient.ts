import type { CvdAuthoringAction, CvdFrame, CvdSceneSettings } from './teavm.d.ts';
import type { MoveHandleCmd, WorkerRequest, WorkerResponse } from './workerMessages';

export type FrameRequest = {
  width: number;
  height: number;
  move?: MoveHandleCmd;
  settings?: CvdSceneSettings;
  actions?: CvdAuthoringAction[];
};

/**
 * Talks to {@link ./classifyWorker.ts}: loads TeaVM once, coalesces in-flight
 * frame requests so drag spam keeps only the latest pending update.
 */
export class ClassifyClient {
  onFrame: ((frame: CvdFrame, ms: number) => void) | null = null;
  onError: ((err: Error) => void) | null = null;

  private readonly worker: Worker;
  private readonly readyPromise: Promise<void>;
  private resolveReady!: () => void;
  private rejectReady!: (err: Error) => void;

  private nextRequestId = 1;
  private busy = false;
  private pending: FrameRequest | null = null;
  private readySettled = false;

  constructor(teavmUrl: string) {
    this.readyPromise = new Promise<void>((resolve, reject) => {
      this.resolveReady = resolve;
      this.rejectReady = reject;
    });

    this.worker = new Worker(new URL('./classifyWorker.ts', import.meta.url), {
      type: 'module',
    });

    this.worker.onmessage = (event: MessageEvent<WorkerResponse>) => {
      const msg = event.data;
      if (msg.type === 'ready') {
        if (!this.readySettled) {
          this.readySettled = true;
          this.resolveReady();
        }
        return;
      }
      if (msg.type === 'error') {
        const err = new Error(msg.message);
        if (!this.readySettled) {
          this.readySettled = true;
          this.rejectReady(err);
        }
        this.onError?.(err);
        if (msg.requestId != null) {
          this.busy = false;
          this.pump();
        }
        return;
      }
      if (msg.type === 'frame') {
        this.busy = false;
        this.onFrame?.(msg.frame, msg.ms);
        this.pump();
      }
    };

    this.worker.onerror = (event) => {
      const err = new Error(event.message || 'Classify worker failed');
      if (!this.readySettled) {
        this.readySettled = true;
        this.rejectReady(err);
      }
      this.onError?.(err);
    };

    this.post({ type: 'init', teavmUrl });
  }

  whenReady(): Promise<void> {
    return this.readyPromise;
  }

  /** Queue a classify pass; coalesces pending work without dropping authoring actions. */
  enqueue(req: FrameRequest): void {
    if (this.pending == null) {
      this.pending = { ...req, actions: req.actions ? [...req.actions] : undefined };
    } else {
      this.pending = {
        width: req.width,
        height: req.height,
        move: req.move ?? this.pending.move,
        settings: { ...this.pending.settings, ...req.settings },
        actions: [...(this.pending.actions ?? []), ...(req.actions ?? [])],
      };
      if (this.pending.actions?.length === 0) {
        this.pending.actions = undefined;
      }
    }
    this.pump();
  }

  terminate(): void {
    this.worker.terminate();
  }

  private post(msg: WorkerRequest): void {
    this.worker.postMessage(msg);
  }

  private pump(): void {
    if (this.busy || this.pending == null) {
      return;
    }
    const req = this.pending;
    this.pending = null;
    this.busy = true;
    this.post({
      type: 'frame',
      requestId: this.nextRequestId++,
      width: req.width,
      height: req.height,
      move: req.move,
      settings: req.settings,
      actions: req.actions,
    });
  }
}
