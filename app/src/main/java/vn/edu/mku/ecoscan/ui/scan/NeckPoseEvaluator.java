package vn.edu.mku.ecoscan.ui.scan;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

/** Checks the two seated neck stretches independently, including mirrored directions. */
public final class NeckPoseEvaluator {
    public enum StretchType { NONE, SIDE, DIAGONAL }

    public static final class Result {
        public final boolean correct;
        public final StretchType type;
        public final String guidance;

        Result(boolean correct, StretchType type, String guidance) {
            this.correct = correct;
            this.type = type;
            this.guidance = guidance;
        }
    }

    private static final int NOSE = 0, LEFT_EYE = 2, RIGHT_EYE = 5;
    private static final int LEFT_EAR = 7, RIGHT_EAR = 8;
    private static final int LEFT_SHOULDER = 11, RIGHT_SHOULDER = 12;
    private static final float MIN_CORE_VISIBILITY = 0.60f;
    private static final float MIN_FACE_VISIBILITY = 0.55f;

    static Result evaluate(List<NormalizedLandmark> landmarks) {
        if (landmarks == null || landmarks.size() != 33) return missing();
        float[] x = new float[33], y = new float[33], visibility = new float[33];
        for (int i = 0; i < 33; i++) {
            NormalizedLandmark landmark = landmarks.get(i);
            x[i] = landmark.x();
            y[i] = landmark.y();
            visibility[i] = landmark.visibility().orElse(0f);
        }
        return evaluate(x, y, visibility);
    }

    /** Coordinates are normalized to the upright, unmirrored camera frame. */
    static Result evaluate(float[] x, float[] y, float[] visibility) {
        if (x == null || y == null || visibility == null
                || x.length != 33 || y.length != 33 || visibility.length != 33) return missing();
        if (!visible(x, y, visibility, NOSE, MIN_CORE_VISIBILITY)
                || !visible(x, y, visibility, LEFT_SHOULDER, MIN_CORE_VISIBILITY)
                || !visible(x, y, visibility, RIGHT_SHOULDER, MIN_CORE_VISIBILITY)) return missing();
        double shoulderWidth = Math.hypot(x[LEFT_SHOULDER] - x[RIGHT_SHOULDER],
                y[LEFT_SHOULDER] - y[RIGHT_SHOULDER]);
        if (shoulderWidth < 0.16) return reject("Ngồi gần camera hơn để thấy rõ đầu và hai vai");
        if (lineAngle(x, y, LEFT_SHOULDER, RIGHT_SHOULDER) > 12)
            return reject("Giữ thân thẳng và hai vai cân bằng");
        if (!visible(x, y, visibility, LEFT_EAR, MIN_FACE_VISIBILITY)
                || !visible(x, y, visibility, RIGHT_EAR, MIN_FACE_VISIBILITY))
            return reject("Để camera nhìn rõ hai tai, đầu và hai vai");

        double earMidX = (x[LEFT_EAR] + x[RIGHT_EAR]) / 2.0;
        double earMidY = (y[LEFT_EAR] + y[RIGHT_EAR]) / 2.0;
        double noseShift = Math.abs(x[NOSE] - earMidX) / shoulderWidth;
        double noseDrop = (y[NOSE] - earMidY) / shoulderWidth;
        double earTilt = lineAngle(x, y, LEFT_EAR, RIGHT_EAR);

        // Side bend: level shoulders, a tilted face, and no downward head turn.
        boolean bothEyes = visible(x, y, visibility, LEFT_EYE, MIN_FACE_VISIBILITY)
                && visible(x, y, visibility, RIGHT_EYE, MIN_FACE_VISIBILITY);
        if (bothEyes) {
            double eyeTilt = lineAngle(x, y, LEFT_EYE, RIGHT_EYE);
            if (between(eyeTilt, 18, 45) && between(earTilt, 16, 45)
                    && Math.abs(eyeTilt - earTilt) <= 16
                    && noseShift <= 0.11 && between(noseDrop, -0.08, 0.12)) {
                return new Result(true, StretchType.SIDE,
                        "Nghiêng cổ sang bên đúng, giữ hai vai cân bằng");
            }
        }

        // Diagonal-down: the nose moves down and across from the ear midpoint.
        // Upper limits reject a hunched torso or an excessive neck bend.
        if (between(noseShift, 0.10, 0.40) && between(noseDrop, 0.08, 0.35)
                && between(earTilt, 7, 35)) {
            return new Result(true, StretchType.DIAGONAL,
                    "Cúi chéo cổ đúng, giữ thân thẳng và vai cân bằng");
        }
        if (earTilt > 45 || noseDrop > 0.35)
            return reject("Giảm độ nghiêng hoặc cúi cổ, không gắng sức");
        return reject("Nghiêng cổ sang bên hoặc cúi chéo xuống như ảnh mẫu");
    }

    private static boolean visible(float[] x, float[] y, float[] v, int index, float minimum) {
        return Float.isFinite(x[index]) && Float.isFinite(y[index])
                && Float.isFinite(v[index]) && v[index] >= minimum
                && x[index] >= 0.02f && x[index] <= 0.98f
                && y[index] >= 0.02f && y[index] <= 0.98f;
    }

    private static double lineAngle(float[] x, float[] y, int a, int b) {
        return Math.toDegrees(Math.atan2(Math.abs(y[a] - y[b]), Math.abs(x[a] - x[b])));
    }

    private static boolean between(double value, double min, double max) {
        return value >= min && value <= max;
    }

    private static Result missing() {
        return reject("Đặt đầu và hai vai vào khung hình");
    }

    private static Result reject(String guidance) {
        return new Result(false, StretchType.NONE, guidance);
    }
}
