package vn.edu.mku.ecoscan.ui.scan;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PostureHoldTimerTest {
    @Test public void countsOnlyContinuousValidPostureAndResetsWhenPointsDisappear() {
        PostureHoldTimer timer = new PostureHoldTimer(30000, 1200);
        assertEquals(0, timer.update(true, 1000));
        assertEquals(500, timer.update(true, 1500));
        assertEquals(0, timer.update(false, 1600));
        assertEquals(0, timer.update(true, 1700));
        assertEquals(300, timer.update(true, 2000));
    }

    @Test public void rejectsLongFrameGapsAndStopsAtGoal() {
        PostureHoldTimer timer = new PostureHoldTimer(30000, 1200);
        timer.update(true, 1000);
        assertEquals(0, timer.update(true, 2300));
        for (int time = 2400; time <= 34000; time += 100) timer.update(true, time);
        assertEquals(30000, timer.update(true, 34100));
    }
}
