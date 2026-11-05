package vn.edu.mku.ecoscan.ui.scan;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class PoseOverlayTransformTest {
    @Test public void rotatesViewportCropForAllCameraOrientations() {
        assertArrayEquals(new float[] {10, 20, 510, 420},
                PoseOverlayTransform.rotateCrop(10, 20, 510, 420, 640, 480, 0), 0.001f);
        assertArrayEquals(new float[] {60, 10, 460, 510},
                PoseOverlayTransform.rotateCrop(10, 20, 510, 420, 640, 480, 90), 0.001f);
        assertArrayEquals(new float[] {130, 60, 630, 460},
                PoseOverlayTransform.rotateCrop(10, 20, 510, 420, 640, 480, 180), 0.001f);
        assertArrayEquals(new float[] {20, 130, 420, 630},
                PoseOverlayTransform.rotateCrop(10, 20, 510, 420, 640, 480, 270), 0.001f);
    }

    @Test public void alignsCroppedPreviewAndMirrorsFrontCameraOnce() {
        float[] crop = {50, 0, 350, 400};
        float[] back = PoseOverlayTransform.map(0.25f, 0.5f, 400, 400,
                crop, 300, 400, false);
        float[] front = PoseOverlayTransform.map(0.25f, 0.5f, 400, 400,
                crop, 300, 400, true);
        assertEquals(50f, back[0], 0.001f);
        assertEquals(200f, back[1], 0.001f);
        assertEquals(250f, front[0], 0.001f);
        assertEquals(200f, front[1], 0.001f);
    }

    @Test public void neckTimerRejectsJointsOutsideVisibleCameraCrop() {
        List<NormalizedLandmark> points = new ArrayList<>();
        for (int i = 0; i < 33; i++) points.add(point(0.5f, 0.5f));
        float[] crop = {25, 0, 75, 100};
        assertTrue(PoseOverlayTransform.requiredPointsInCrop(
                ExerciseType.NECK, points, 100, 100, crop));
        points.set(0, point(0.1f, 0.5f));
        assertFalse(PoseOverlayTransform.requiredPointsInCrop(
                ExerciseType.NECK, points, 100, 100, crop));
    }

    private static NormalizedLandmark point(float x, float y) {
        return NormalizedLandmark.create(x, y, 0f,
                Optional.of(0.9f), Optional.empty());
    }
}
