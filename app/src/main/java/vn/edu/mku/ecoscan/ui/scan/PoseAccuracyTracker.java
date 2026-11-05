package vn.edu.mku.ecoscan.ui.scan;

/** Percentage of camera observations that passed the selected exercise check. */
public final class PoseAccuracyTracker {
    private int observations;
    private int correctObservations;

    public void observe(boolean correct) {
        observations++;
        if (correct) correctObservations++;
    }

    public int percent() {
        return observations == 0 ? 0
                : Math.round(100f * correctObservations / observations);
    }
}
