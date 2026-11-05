package vn.edu.mku.ecoscan.ui.scan;

import androidx.annotation.DrawableRes;
import vn.edu.mku.ecoscan.R;

/** Exercise identifiers, including a retired type kept for historical sessions. */
public enum ExerciseType {
    NECK("neck", "Giãn cổ", "Nghiêng bên hoặc cúi chéo, giữ vai cân bằng.", "Giãn cổ khi ngồi", R.drawable.neck_stretch_guide),
    SHOULDER("shoulder", "Giãn vai", "Đưa một tay ngang ngực, tay kia đỡ nhẹ khuỷu.", "Giãn vai ngang ngực", R.drawable.exercise_shoulder),
    BICEPS("biceps", "Giãn bắp tay trước", "Mở hai tay sang ngang, duỗi khuỷu nhẹ nhàng.", "Mở tay giãn bắp trước", R.drawable.exercise_biceps),
    WRIST_UP("wrist_up", "Gập cổ tay lên", "Duỗi một tay, dùng tay kia kéo nhẹ các ngón tay lên.", "Giãn cẳng tay hướng lên", R.drawable.exercise_wrist_up_assisted),
    WRIST_DOWN("wrist_down", "Gập cổ tay xuống", "Duỗi một tay, dùng tay kia kéo nhẹ các ngón tay xuống.", "Giãn cẳng tay hướng xuống", R.drawable.exercise_wrist_down_assisted);

    public static ExerciseType[] activeTypes() {
        return new ExerciseType[] {NECK, SHOULDER, WRIST_UP, WRIST_DOWN};
    }

    public final String key;
    public final String title;
    public final String instruction;
    public final String poseLabel;
    @DrawableRes public final int imageRes;

    ExerciseType(String key, String title, String instruction, String poseLabel,
            @DrawableRes int imageRes) {
        this.key = key;
        this.title = title;
        this.instruction = instruction;
        this.poseLabel = poseLabel;
        this.imageRes = imageRes;
    }

    public static ExerciseType fromKey(String key) {
        for (ExerciseType type : values()) if (type.key.equals(key)) return type;
        return NECK;
    }
}
