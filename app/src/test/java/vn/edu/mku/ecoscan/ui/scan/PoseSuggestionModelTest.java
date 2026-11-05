package vn.edu.mku.ecoscan.ui.scan;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PoseSuggestionModelTest {
    @Test public void mapsAllEightModelLabelsToTheRequestedExercise() {
        String[] labels = {"cang_tay_gap_len", "cang_tay_gap_xuong", "canh_tay_phai",
                "canh_tay_trai", "co_cheo_phai", "co_cheo_trai", "co_nghieng_phai",
                "co_nghieng_trai"};
        ExerciseType[] types = {ExerciseType.WRIST_UP, ExerciseType.WRIST_DOWN,
                ExerciseType.SHOULDER, ExerciseType.SHOULDER, ExerciseType.NECK,
                ExerciseType.NECK, ExerciseType.NECK, ExerciseType.NECK};
        for (int i = 0; i < labels.length; i++) {
            assertNull(PoseSuggestionModel.guidanceFor(types[i], labels[i]));
            assertTrue(PoseSuggestionModel.guidanceFor(ExerciseType.BICEPS, labels[i])
                    .contains("hãy tập giãn bắp tay trước"));
        }
        assertNull(PoseSuggestionModel.guidanceFor(ExerciseType.NECK, "unknown"));
    }
}
