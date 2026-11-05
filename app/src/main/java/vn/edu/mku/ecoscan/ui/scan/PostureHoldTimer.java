package vn.edu.mku.ecoscan.ui.scan;

/** Counts only consecutive valid camera observations, using a monotonic clock. */
public final class PostureHoldTimer {
    private final long goalMs;
    private final long maxFrameGapMs;
    private long lastValidMs;
    private long elapsedMs;

    public PostureHoldTimer(long goalMs, long maxFrameGapMs) {
        this.goalMs = goalMs;
        this.maxFrameGapMs = maxFrameGapMs;
    }

    long update(boolean valid, long nowMs) {
        if (!valid) {
            lastValidMs = 0;
            elapsedMs = 0;
        } else {
            if (lastValidMs > 0) {
                long gap = nowMs - lastValidMs;
                if (gap > 0 && gap <= maxFrameGapMs) elapsedMs = Math.min(goalMs, elapsedMs + gap);
                else elapsedMs = 0;
            }
            lastValidMs = nowMs;
        }
        return elapsedMs;
    }
}
