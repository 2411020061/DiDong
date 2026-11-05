package vn.edu.mku.ecoscan.ui.scan;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PoseAccuracyTrackerTest {
    @Test public void reportsObservedPostureAccuracyInsteadOfTimerProgress() {
        PoseAccuracyTracker tracker = new PoseAccuracyTracker();
        assertEquals(0, tracker.percent());
        tracker.observe(true);
        tracker.observe(false);
        tracker.observe(true);
        tracker.observe(true);
        assertEquals(75, tracker.percent());
    }
}
