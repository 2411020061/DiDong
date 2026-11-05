package vn.edu.mku.ecoscan.ui.scan;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

/** Maps an upright MediaPipe landmark through CameraX's ViewPort crop to PreviewView. */
public final class PoseOverlayTransform {
    private PoseOverlayTransform() { }

    /** Returns left, top, right, bottom after clockwise image rotation. */
    public static float[] rotateCrop(float left, float top, float right, float bottom,
            int rawWidth, int rawHeight, int rotationDegrees) {
        switch (rotationDegrees) {
            case 0: return new float[] {left, top, right, bottom};
            case 90: return new float[] {rawHeight - bottom, left, rawHeight - top, right};
            case 180: return new float[] {rawWidth - right, rawHeight - bottom,
                    rawWidth - left, rawHeight - top};
            case 270: return new float[] {top, rawWidth - right, bottom, rawWidth - left};
            default: throw new IllegalArgumentException("Unsupported camera rotation: " + rotationDegrees);
        }
    }

    /** Returns x,y in the overlay view; front-camera mirroring happens exactly once. */
    static float[] map(float normalizedX, float normalizedY, int imageWidth, int imageHeight,
            float[] crop, int viewWidth, int viewHeight, boolean mirrorFront) {
        float cropWidth = crop[2] - crop[0];
        float cropHeight = crop[3] - crop[1];
        if (cropWidth <= 0 || cropHeight <= 0 || viewWidth <= 0 || viewHeight <= 0)
            return new float[] {Float.NaN, Float.NaN};
        float scale = Math.max(viewWidth / cropWidth, viewHeight / cropHeight);
        float x = (normalizedX * imageWidth - crop[0]) * scale
                + (viewWidth - cropWidth * scale) / 2f;
        float y = (normalizedY * imageHeight - crop[1]) * scale
                + (viewHeight - cropHeight * scale) / 2f;
        return new float[] {mirrorFront ? viewWidth - x : x, y};
    }

    /** Do not count a pose whose required joints are outside the camera's visible crop. */
    static boolean requiredPointsInCrop(ExerciseType type, List<NormalizedLandmark> landmarks,
            int width, int height, float[] crop) {
        if (landmarks == null || landmarks.size() != 33 || crop == null || crop.length != 4)
            return false;
        if (!inside(landmarks, 11, width, height, crop)
                || !inside(landmarks, 12, width, height, crop)) return false;
        switch (type) {
            case NECK:
                return inside(landmarks, 0, width, height, crop)
                        && inside(landmarks, 7, width, height, crop)
                        && inside(landmarks, 8, width, height, crop);
            case SHOULDER:
                return inside(landmarks, 15, width, height, crop)
                        && inside(landmarks, 16, width, height, crop)
                        && (inside(landmarks, 13, width, height, crop)
                            || inside(landmarks, 14, width, height, crop));
            case BICEPS:
                return inside(landmarks, 13, width, height, crop)
                        && inside(landmarks, 14, width, height, crop)
                        && inside(landmarks, 15, width, height, crop)
                        && inside(landmarks, 16, width, height, crop);
            case WRIST_UP:
            case WRIST_DOWN:
                return (inside(landmarks, 13, width, height, crop)
                        && inside(landmarks, 15, width, height, crop)
                        && inside(landmarks, 19, width, height, crop))
                        || (inside(landmarks, 14, width, height, crop)
                        && inside(landmarks, 16, width, height, crop)
                        && inside(landmarks, 20, width, height, crop));
            default: return false;
        }
    }

    private static boolean inside(List<NormalizedLandmark> landmarks, int index,
            int width, int height, float[] crop) {
        NormalizedLandmark point = landmarks.get(index);
        float x = point.x() * width, y = point.y() * height;
        float insetX = (crop[2] - crop[0]) * 0.08f;
        float insetY = (crop[3] - crop[1]) * 0.08f;
        return point.visibility().orElse(0f) >= 0.55f
                && Float.isFinite(x) && Float.isFinite(y)
                && x >= crop[0] + insetX && x <= crop[2] - insetX
                && y >= crop[1] + insetY && y <= crop[3] - insetY;
    }
}
