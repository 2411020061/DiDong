package vn.edu.mku.ecoscan.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
    private static final float CONF_THRESHOLD = 0.5f;

    private final Interpreter interpreter;
    private final ByteBuffer inputBuffer;
    private final int[] intValues;
    private final float[][][] output;

    private float[] personBox;

    public synchronized float[] getPersonBox() {
        return personBox == null ? null : personBox.clone();
    }

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
        personBox = null;
        if (bitmap == null || interpreter == null) return null;

        PoseLetterbox geometry = new PoseLetterbox(bitmap.getWidth(), bitmap.getHeight(), INPUT_SIZE);
        Bitmap scaled = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaled);
        canvas.drawColor(Color.rgb(114, 114, 114));
        canvas.drawBitmap(bitmap, null, new Rect(geometry.left, geometry.top,
                geometry.left + geometry.width, geometry.top + geometry.height),
                new Paint(Paint.FILTER_BITMAP_FLAG));
        scaled.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
        scaled.recycle();

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

        DetectionCandidate best = decode(output[0]);
        if (best == null) return null;
        personBox = new float[] {geometry.x(best.x1), geometry.y(best.y1),
                geometry.x(best.x2), geometry.y(best.y2), best.score};

        List<NormalizedLandmark> landmarks = new ArrayList<>(33);
        for (int i = 0; i < 33; i++) {
            int src = COCO_TO_MEDIAPIPE[i];
            float x = geometry.x(best.kx[src]);
            float y = geometry.y(best.ky[src]);
            float c = best.kc[src];
            if (i >= 17 && i <= 22) c = 0f;

            landmarks.add(NormalizedLandmark.create(x, y, 0f, Optional.of(c), Optional.empty()));
        }

        return landmarks;
    }

    /** Decodes this model's normalized, post-sigmoid output; no JNI required. */
    public static DetectionCandidate decode(float[][] out0) {
        DetectionCandidate best = null;
        for (int a = 0; a < out0[0].length; a++) {
            float rawScore = out0[4][a];
            // This export includes sigmoid and coordinate normalization in its graph.
            float score = rawScore;
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

                    kx[k] = Math.max(0f, Math.min(1f, rawX));
                    ky[k] = Math.max(0f, Math.min(1f, rawY));
                    kc[k] = rawKc;
                }
                if (best == null || score > best.score)
                    best = new DetectionCandidate(score, x1, y1, x2, y2, kx, ky, kc);
            }
        }

        return best;
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

    @Override
    public synchronized void close() {
        if (interpreter != null) {
            interpreter.close();
        }
    }
}
