package vn.edu.mku.ecoscan.ui.scan;

import org.junit.Test;
import vn.edu.mku.ecoscan.ml.YoloPoseDetector;
import static org.junit.Assert.*;

public class YoloOutputTest {
    @Test public void consecutiveFramesFollowMotionAndClearWhenPersonLeaves() {
        float[][] frame = new float[56][1];
        frame[4][0] = 0.9f;
        frame[5][0] = 0.25f;
        YoloPoseDetector.DetectionCandidate first = YoloPoseDetector.decode(frame);
        frame[5][0] = 0.75f;
        YoloPoseDetector.DetectionCandidate next = YoloPoseDetector.decode(frame);
        assertEquals(0.25f, first.kx[0], 0.0001f);
        assertEquals(0.75f, next.kx[0], 0.0001f);
        frame[4][0] = 0.01f;
        assertNull(YoloPoseDetector.decode(frame));
    }

    @Test public void emptySceneDoesNotBecomePersonAfterSecondSigmoid() {
        float[][] out = new float[56][2];
        out[4][0] = 0.001f;
        out[4][1] = 0.2f;
        assertNull(YoloPoseDetector.decode(out));
    }

    @Test public void preservesNormalizedCoordinatesAndLowJointConfidence() {
        float[][] out = new float[56][2];
        out[4][0] = 0.7f;
        out[4][1] = 0.9f;
        out[0][1] = 0.5f;
        out[1][1] = 0.5f;
        out[2][1] = 0.4f;
        out[3][1] = 0.8f;
        out[5][1] = 0.45f;
        out[6][1] = 0.2f;
        out[7][1] = 0.05f;
        YoloPoseDetector.DetectionCandidate result = YoloPoseDetector.decode(out);
        assertNotNull(result);
        assertEquals(0.9f, result.score, 0.0001f);
        assertEquals(0.3f, result.x1, 0.0001f);
        assertEquals(0.45f, result.kx[0], 0.0001f);
        assertEquals(0.2f, result.ky[0], 0.0001f);
        assertEquals(0.05f, result.kc[0], 0.0001f);
    }
}
