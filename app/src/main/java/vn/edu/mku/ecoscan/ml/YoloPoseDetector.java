package vn.edu.mku.ecoscan.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class YoloPoseDetector implements AutoCloseable {
    private static final String MODEL_NAME = "yolo26m-pose.tflite";
    private static final int INPUT_SIZE = 640;
    private static final int NUM_ANCHORS = 8400;
    private static final int NUM_ATTRS = 56;
    private static final float CONF_THRESHOLD = 0.15f;

    private final Interpreter interpreter;
    private final ByteBuffer inputBuffer;
    private final int[] intValues;
    private final float[][][] output;

    // Mapping from COCO 17 keypoints to MediaPipe 33 landmarks
    private static final int[] COCO_TO_MEDIAPIPE = new int[] {
        0,  // 0: NOSE -> COCO 0 (Nose)
        1,  // 1: LEFT_EYE_INNER -> COCO 1 (L_Eye)
        1,  // 2: LEFT_EYE -> COCO 1
        1,  // 3: LEFT_EYE_OUTER -> COCO 1
        2,  // 4: RIGHT_EYE_INNER -> COCO 2 (R_Eye)
        2,  // 5: RIGHT_EYE -> COCO 2
        2,  // 6: RIGHT_EYE_OUTER -> COCO 2
        3,  // 7: LEFT_EAR -> COCO 3 (L_Ear)
        4,  // 8: RIGHT_EAR -> COCO 4 (R_Ear)
        0,  // 9: MOUTH_LEFT -> COCO 0
        0,  // 10: MOUTH_RIGHT -> COCO 0
        5,  // 11: LEFT_SHOULDER -> COCO 5 (L_Shoulder)
        6,  // 12: RIGHT_SHOULDER -> COCO 6 (R_Shoulder)
        7,  // 13: LEFT_ELBOW -> COCO 7 (L_Elbow)
        8,  // 14: RIGHT_ELBOW -> COCO 8 (R_Elbow)
        9,  // 15: LEFT_WRIST -> COCO 9 (L_Wrist)
        10, // 16: RIGHT_WRIST -> COCO 10 (R_Wrist)
        9,  // 17: LEFT_PINKY -> COCO 9
        10, // 18: RIGHT_PINKY -> COCO 10
        9,  // 19: LEFT_INDEX -> COCO 9 (adjusted)
        10, // 20: RIGHT_INDEX -> COCO 10 (adjusted)
        9,  // 21: LEFT_THUMB -> COCO 9
        10, // 22: RIGHT_THUMB -> COCO 10
        11, // 23: LEFT_HIP -> COCO 11 (L_Hip)
        12, // 24: RIGHT_HIP -> COCO 12 (R_Hip)
        13, // 25: LEFT_KNEE -> COCO 13 (L_Knee)
        14, // 26: RIGHT_KNEE -> COCO 14 (R_Knee)
        15, // 27: LEFT_ANKLE -> COCO 15 (L_Ankle)
        16, // 28: RIGHT_ANKLE -> COCO 16 (R_Ankle)
        15, // 29: LEFT_HEEL -> COCO 15
        16, // 30: RIGHT_HEEL -> COCO 16
        15, // 31: LEFT_FOOT_INDEX -> COCO 15
        16  // 32: RIGHT_FOOT_INDEX -> COCO 16
    };

    public YoloPoseDetector(Context context) throws IOException {
        try (AssetFileDescriptor asset = context.getAssets().openFd(MODEL_NAME);
             FileInputStream stream = new FileInputStream(asset.getFileDescriptor());
             FileChannel channel = stream.getChannel()) {
            MappedByteBuffer modelData = channel.map(FileChannel.MapMode.READ_ONLY,
                    asset.getStartOffset(), asset.getDeclaredLength());
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            interpreter = new Interpreter(modelData, options);
        }

        inputBuffer = ByteBuffer.allocateDirect(3 * INPUT_SIZE * INPUT_SIZE * 4);
        inputBuffer.order(ByteOrder.nativeOrder());
        intValues = new int[INPUT_SIZE * INPUT_SIZE];
        output = new float[1][NUM_ATTRS][NUM_ANCHORS];
    }

    public synchronized List<NormalizedLandmark> detect(Bitmap bitmap) {
        if (bitmap == null || interpreter == null) return null;

        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        scaled.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
        if (scaled != bitmap) {
            scaled.recycle();
        }

        inputBuffer.rewind();
        // R channel
        for (int i = 0; i < INPUT_SIZE * INPUT_SIZE; i++) {
            inputBuffer.putFloat(((intValues[i] >> 16) & 0xFF) / 255.0f);
        }
        // G channel
        for (int i = 0; i < INPUT_SIZE * INPUT_SIZE; i++) {
            inputBuffer.putFloat(((intValues[i] >> 8) & 0xFF) / 255.0f);
        }
        // B channel
        for (int i = 0; i < INPUT_SIZE * INPUT_SIZE; i++) {
            inputBuffer.putFloat((intValues[i] & 0xFF) / 255.0f);
        }

        interpreter.run(inputBuffer, output);

        float[][] out0 = output[0];
        List<DetectionCandidate> candidates = new ArrayList<>();

        for (int a = 0; a < NUM_ANCHORS; a++) {
            float rawScore = out0[4][a];
            float score = 1.0f / (1.0f + (float) Math.exp(-rawScore));
            if (score >= CONF_THRESHOLD) {
                float cx = out0[0][a];
                float cy = out0[1][a];
                float w = out0[2][a];
                float h = out0[3][a];
                float x1 = cx - w / 2f;
                float y1 = cy - h / 2f;
                float x2 = cx + w / 2f;
                float y2 = cy + h / 2f;

                float[] kx = new float[17];
                float[] ky = new float[17];
                float[] kc = new float[17];
                for (int k = 0; k < 17; k++) {
                    float rawX = out0[5 + k * 3][a];
                    float rawY = out0[5 + k * 3 + 1][a];
                    float rawKc = out0[5 + k * 3 + 2][a];

                    kx[k] = Math.max(0f, Math.min(1f, rawX / (float) INPUT_SIZE));
                    ky[k] = Math.max(0f, Math.min(1f, rawY / (float) INPUT_SIZE));
                    float sigKc = 1.0f / (1.0f + (float) Math.exp(-rawKc));
                    kc[k] = Math.max(0.6f, sigKc);
                }
                candidates.add(new DetectionCandidate(score, x1, y1, x2, y2, kx, ky, kc));
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        candidates.sort((a, b) -> Float.compare(b.score, a.score));

        List<DetectionCandidate> selected = new ArrayList<>();
        for (DetectionCandidate cand : candidates) {
            boolean keep = true;
            for (DetectionCandidate sel : selected) {
                if (calculateIoU(cand, sel) > 0.45f) {
                    keep = false;
                    break;
                }
            }
            if (keep) {
                selected.add(cand);
                break;
            }
        }

        if (selected.isEmpty()) {
            return null;
        }

        DetectionCandidate best = selected.get(0);

        List<NormalizedLandmark> landmarks = new ArrayList<>(33);
        for (int i = 0; i < 33; i++) {
            int src = COCO_TO_MEDIAPIPE[i];
            float x = best.kx[src];
            float y = best.ky[src];
            float c = best.kc[src];

            if (i == 19) {
                float otherX = best.kc[10] > 0.3f ? best.kx[10] : x;
                float otherY = best.kc[10] > 0.3f ? best.ky[10] : y;
                x = x + (otherX - x) * 0.5f;
                y = y + (otherY - y) * 0.5f;
            } else if (i == 20) {
                float otherX = best.kc[9] > 0.3f ? best.kx[9] : x;
                float otherY = best.kc[9] > 0.3f ? best.ky[9] : y;
                x = x + (otherX - x) * 0.5f;
                y = y + (otherY - y) * 0.5f;
            }

            landmarks.add(NormalizedLandmark.create(x, y, 0f, Optional.of(c), Optional.empty()));
        }

        return landmarks;
    }

    public static class DetectionCandidate {
        public float score, x1, y1, x2, y2;
        public float[] kx, ky, kc;
        public DetectionCandidate(float score, float x1, float y1, float x2, float y2, float[] kx, float[] ky, float[] kc) {
            this.score = score;
            this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
            this.kx = kx; this.ky = ky; this.kc = kc;
        }
    }

    private static float calculateIoU(DetectionCandidate a, DetectionCandidate b) {
        float interX1 = Math.max(a.x1, b.x1);
        float interY1 = Math.max(a.y1, b.y1);
        float interX2 = Math.min(a.x2, b.x2);
        float interY2 = Math.min(a.y2, b.y2);

        float interW = Math.max(0f, interX2 - interX1);
        float interH = Math.max(0f, interY2 - interY1);
        float interArea = interW * interH;

        float areaA = (a.x2 - a.x1) * (a.y2 - a.y1);
        float areaB = (b.x2 - b.x1) * (b.y2 - b.y1);
        float unionArea = areaA + areaB - interArea;

        return unionArea <= 0f ? 0f : interArea / unionArea;
    }

    @Override
    public synchronized void close() {
        if (interpreter != null) {
            interpreter.close();
        }
    }
}
