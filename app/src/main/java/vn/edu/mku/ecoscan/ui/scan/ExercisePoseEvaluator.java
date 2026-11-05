package vn.edu.mku.ecoscan.ui.scan;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

/** Camera-facing geometric checks for the non-neck exercises. */
public final class ExercisePoseEvaluator {
    private static final int LEFT_SHOULDER = 11, RIGHT_SHOULDER = 12;
    private static final int[] SHOULDER = {11, 12}, ELBOW = {13, 14};
    private static final int[] WRIST = {15, 16}, INDEX = {19, 20};
    private static final float MIN_VISIBILITY = 0.65f;

    public static NeckPoseEvaluator.Result evaluate(ExerciseType type,
            List<NormalizedLandmark> landmarks) {
        if (landmarks == null || landmarks.size() != 33) return reject("Đặt vai và hai tay vào khung hình");
        float[] x = new float[33], y = new float[33], v = new float[33];
        for (int i = 0; i < 33; i++) {
            NormalizedLandmark point = landmarks.get(i);
            x[i] = point.x(); y[i] = point.y(); v[i] = point.visibility().orElse(0f);
        }
        return evaluate(type, x, y, v);
    }

    static NeckPoseEvaluator.Result evaluate(ExerciseType type, float[] x, float[] y, float[] v) {
        if (x == null || y == null || v == null || x.length != 33 || y.length != 33 || v.length != 33)
            return reject("Đặt vai và hai tay vào khung hình");
        if (!visible(x, y, v, LEFT_SHOULDER) || !visible(x, y, v, RIGHT_SHOULDER))
            return reject("Để camera nhìn rõ hai vai");
        double width = distance(x, y, LEFT_SHOULDER, RIGHT_SHOULDER);
        if (width < 0.16) return reject("Ngồi gần camera hơn để thấy rõ hai vai");
        if (Math.abs(y[LEFT_SHOULDER] - y[RIGHT_SHOULDER]) > width * 0.20)
            return reject("Giữ lưng thẳng, hai vai ngang nhau");

        for (int side = 0; side < 2; side++) {
            int shoulder = SHOULDER[side], elbow = ELBOW[side], wrist = WRIST[side];
            if (!visible(x, y, v, elbow) || !visible(x, y, v, wrist)) continue;
            double elbowAngle = angle(x, y, shoulder, elbow, wrist);
            if (type == ExerciseType.SHOULDER) {
                int otherShoulder = SHOULDER[1 - side], otherWrist = WRIST[1 - side];
                if (!visible(x, y, v, otherWrist)) continue;
                boolean acrossChest = distance(x, y, wrist, otherShoulder) < width * 0.75
                        && distance(x, y, wrist, shoulder) > width * 0.65;
                boolean supported = distance(x, y, otherWrist, elbow) < width * 0.75;
                if (acrossChest && supported && elbowAngle > 135
                        && Math.abs(y[elbow] - y[shoulder]) < width * 0.45)
                    return accept("Tay ngang ngực, tay kia đỡ khuỷu nhẹ nhàng");
            } else if (type == ExerciseType.WRIST_UP || type == ExerciseType.WRIST_DOWN) {
                int finger = INDEX[side], otherWrist = WRIST[1 - side];
                if (!visible(x, y, v, finger) || !visible(x, y, v, otherWrist)) continue;
                boolean extended = elbowAngle > 140
                        && Math.abs(x[wrist] - x[shoulder]) > width * 0.60
                        && Math.abs(y[wrist] - y[shoulder]) < width * 0.65;
                double fingerRise = (y[wrist] - y[finger]) / width;
                boolean bent = type == ExerciseType.WRIST_UP
                        ? fingerRise >= 0.18 && fingerRise <= 0.75
                        : fingerRise <= -0.18 && fingerRise >= -0.75;
                boolean assisted = distance(x, y, otherWrist, finger) < width * 0.60;
                if (extended && bent && assisted && Math.abs(x[finger] - x[wrist]) < width * 0.55)
                    return accept(type == ExerciseType.WRIST_UP
                            ? "Tay kia kéo nhẹ bàn tay lên, giữ khuỷu duỗi"
                            : "Tay kia kéo nhẹ bàn tay xuống, giữ khuỷu duỗi");
            }
        }

        if (type == ExerciseType.BICEPS) {
            boolean both = true;
            for (int side = 0; side < 2; side++) {
                int shoulder = SHOULDER[side], elbow = ELBOW[side], wrist = WRIST[side];
                int other = SHOULDER[1 - side];
                both &= visible(x, y, v, elbow) && visible(x, y, v, wrist);
                if (!both) break;
                both &= angle(x, y, shoulder, elbow, wrist) > 150
                        && Math.abs(x[wrist] - x[other]) > width * 1.35
                        && Math.abs(y[wrist] - y[shoulder]) < width * 0.55;
            }
            if (both) return accept("Mở hai tay ngang vai, khuỷu duỗi nhẹ");
        }

        if (type == ExerciseType.SHOULDER)
            return reject("Đưa một tay ngang ngực và dùng tay kia đỡ khuỷu");
        if (type == ExerciseType.BICEPS)
            return reject("Mở hai tay sang ngang, duỗi khuỷu trong mức dễ chịu");
        return reject(type == ExerciseType.WRIST_UP
                ? "Duỗi một tay và dùng tay kia kéo các ngón lên"
                : "Duỗi một tay và dùng tay kia kéo các ngón xuống");
    }

    private static boolean visible(float[] x, float[] y, float[] v, int i) {
        return Float.isFinite(x[i]) && Float.isFinite(y[i]) && Float.isFinite(v[i])
                && v[i] >= MIN_VISIBILITY && x[i] >= 0.02f && x[i] <= 0.98f
                && y[i] >= 0.02f && y[i] <= 0.98f;
    }

    private static double distance(float[] x, float[] y, int a, int b) {
        return Math.hypot(x[a] - x[b], y[a] - y[b]);
    }

    private static double angle(float[] x, float[] y, int a, int b, int c) {
        double ux = x[a] - x[b], uy = y[a] - y[b];
        double vx = x[c] - x[b], vy = y[c] - y[b];
        double denom = Math.hypot(ux, uy) * Math.hypot(vx, vy);
        if (denom < 0.0001) return 0;
        double cosine = Math.max(-1, Math.min(1, (ux * vx + uy * vy) / denom));
        return Math.toDegrees(Math.acos(cosine));
    }

    private static NeckPoseEvaluator.Result accept(String guidance) {
        return new NeckPoseEvaluator.Result(true, NeckPoseEvaluator.StretchType.SIDE, guidance);
    }

    private static NeckPoseEvaluator.Result reject(String guidance) {
        return new NeckPoseEvaluator.Result(false, NeckPoseEvaluator.StretchType.NONE, guidance);
    }
}
