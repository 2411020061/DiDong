package vn.edu.mku.ecoscan.ui.scan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExercisePoseEvaluatorTest {
    private final float[] x = new float[33], y = new float[33], v = new float[33];

    private void point(int index, float px, float py) {
        x[index] = px; y[index] = py; v[index] = 0.9f;
    }

    private void shoulders() {
        point(11, 0.35f, 0.40f);
        point(12, 0.65f, 0.40f);
    }

    @Test public void shoulderRequiresCrossedArmAndSupportingHand() {
        shoulders();
        point(13, 0.48f, 0.42f);
        point(15, 0.66f, 0.43f);
        assertFalse(ExercisePoseEvaluator.evaluate(ExerciseType.SHOULDER, x, y, v).correct);
        point(16, 0.48f, 0.48f);
        assertTrue(ExercisePoseEvaluator.evaluate(ExerciseType.SHOULDER, x, y, v).correct);
    }

    @Test public void bicepsRequiresBothArmsExtended() {
        shoulders();
        point(13, 0.22f, 0.40f); point(15, 0.08f, 0.40f);
        assertFalse(ExercisePoseEvaluator.evaluate(ExerciseType.BICEPS, x, y, v).correct);
        point(14, 0.78f, 0.40f); point(16, 0.92f, 0.40f);
        assertTrue(ExercisePoseEvaluator.evaluate(ExerciseType.BICEPS, x, y, v).correct);
    }

    @Test public void wristDirectionsAreDistinctAndRequireVisibleFinger() {
        shoulders();
        point(13, 0.23f, 0.40f); point(15, 0.10f, 0.40f);
        assertFalse(ExercisePoseEvaluator.evaluate(ExerciseType.WRIST_UP, x, y, v).correct);
        point(19, 0.10f, 0.30f);
        assertFalse(ExercisePoseEvaluator.evaluate(ExerciseType.WRIST_UP, x, y, v).correct);
        point(16, 0.12f, 0.32f);
        assertTrue(ExercisePoseEvaluator.evaluate(ExerciseType.WRIST_UP, x, y, v).correct);
        assertFalse(ExercisePoseEvaluator.evaluate(ExerciseType.WRIST_DOWN, x, y, v).correct);
        point(19, 0.10f, 0.50f);
        point(16, 0.12f, 0.52f);
        assertTrue(ExercisePoseEvaluator.evaluate(ExerciseType.WRIST_DOWN, x, y, v).correct);
        v[19] = 0.2f;
        assertFalse(ExercisePoseEvaluator.evaluate(ExerciseType.WRIST_DOWN, x, y, v).correct);
    }
}
