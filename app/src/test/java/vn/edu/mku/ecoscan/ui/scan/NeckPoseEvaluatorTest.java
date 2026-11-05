package vn.edu.mku.ecoscan.ui.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class NeckPoseEvaluatorTest {
    private static final class Landmarks {
        final float[] x = new float[33], y = new float[33], visible = new float[33];
        Landmarks() {
            for (int i = 0; i < 33; i++) { x[i] = 0.5f; y[i] = 0.5f; visible[i] = 0.9f; }
            set(11, .3f, .65f); set(12, .7f, .65f);
            set(7, .43f, .35f); set(8, .57f, .35f);
            set(2, .45f, .33f); set(5, .55f, .33f);
            set(0, .5f, .36f);
        }
        void set(int i, float px, float py) { x[i] = px; y[i] = py; }
        NeckPoseEvaluator.Result evaluate() { return NeckPoseEvaluator.evaluate(x, y, visible); }
    }

    @Test public void acceptsSideBendInBothDirections() {
        Landmarks left = new Landmarks();
        left.set(2, .45f, .30f); left.set(5, .55f, .37f);
        left.set(7, .43f, .31f); left.set(8, .57f, .39f);
        assertTrue(left.evaluate().correct);
        assertEquals(NeckPoseEvaluator.StretchType.SIDE, left.evaluate().type);
        Landmarks right = new Landmarks();
        right.set(2, .45f, .37f); right.set(5, .55f, .30f);
        right.set(7, .43f, .39f); right.set(8, .57f, .31f);
        assertTrue(right.evaluate().correct);
    }

    @Test public void acceptsDiagonalDownInBothDirections() {
        Landmarks left = new Landmarks();
        left.set(7, .43f, .34f); left.set(8, .57f, .36f);
        left.set(0, .44f, .40f);
        assertTrue(left.evaluate().correct);
        assertEquals(NeckPoseEvaluator.StretchType.DIAGONAL, left.evaluate().type);
        Landmarks right = new Landmarks();
        right.set(7, .43f, .34f); right.set(8, .57f, .36f);
        right.set(0, .56f, .40f);
        assertTrue(right.evaluate().correct);
    }

    @Test public void rejectsMissingCoreLandmarksAndLeaningShoulders() {
        Landmarks missingEar = new Landmarks();
        missingEar.visible[7] = .2f;
        assertFalse(missingEar.evaluate().correct);
        Landmarks missingShoulder = new Landmarks();
        missingShoulder.visible[11] = .3f;
        assertFalse(missingShoulder.evaluate().correct);
        Landmarks leaning = new Landmarks();
        leaning.set(12, .7f, .75f);
        leaning.set(2, .45f, .30f); leaning.set(5, .55f, .37f);
        leaning.set(7, .43f, .31f); leaning.set(8, .57f, .39f);
        assertFalse(leaning.evaluate().correct);
    }

    @Test public void rejectsNeutralForwardBendAndHunch() {
        assertFalse(new Landmarks().evaluate().correct);
        Landmarks forward = new Landmarks();
        forward.set(0, .5f, .45f);
        assertFalse(forward.evaluate().correct);
        Landmarks hunch = new Landmarks();
        hunch.set(7, .43f, .34f); hunch.set(8, .57f, .36f);
        hunch.set(0, .82f, .57f);
        assertFalse(hunch.evaluate().correct);
    }
}
