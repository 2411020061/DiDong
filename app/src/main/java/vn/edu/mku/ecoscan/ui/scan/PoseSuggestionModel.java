package vn.edu.mku.ecoscan.ui.scan;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/** Suggests one of the eight trained movements from the same 33 MediaPipe landmarks.
 *  Input follows the shoulder-centered 132-value preprocessing used by colab/train.ipynb.
 */
public final class PoseSuggestionModel implements AutoCloseable {
    private static final String MODEL = "goi_y_8_dong_tac.tflite";
    private static final String LABELS = "labels_8_dong_tac.txt";
    private static final String[] EXPECTED_LABELS = {
            "cang_tay_gap_len", "cang_tay_gap_xuong", "canh_tay_phai", "canh_tay_trai",
            "co_cheo_phai", "co_cheo_trai", "co_nghieng_phai", "co_nghieng_trai"
    };
    private final Interpreter interpreter;
    private final float[][] input = new float[1][132];
    private final float[][] output = new float[1][8];

    public PoseSuggestionModel(Context context) throws Exception {
        List<String> labelList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(LABELS), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    labelList.add(line);
                }
            }
        }
        if (labelList.size() != EXPECTED_LABELS.length)
            throw new IllegalStateException("Model gợi ý cần đúng 8 nhãn");
        for (int i = 0; i < EXPECTED_LABELS.length; i++)
            if (!EXPECTED_LABELS[i].equals(labelList.get(i)))
                throw new IllegalStateException("Thứ tự nhãn model gợi ý không khớp");
        try (AssetFileDescriptor asset = context.getAssets().openFd(MODEL);
             FileChannel channel = new FileInputStream(asset.getFileDescriptor()).getChannel()) {
            MappedByteBuffer data = channel.map(FileChannel.MapMode.READ_ONLY,
                    asset.getStartOffset(), asset.getDeclaredLength());
            interpreter = new Interpreter(data);
        }
        if (interpreter.getInputTensor(0).numElements() != 132
                || interpreter.getOutputTensor(0).numElements() != EXPECTED_LABELS.length) {
            interpreter.close();
            throw new IllegalStateException("Model gợi ý cần 132 số đầu vào và 8 nhãn đầu ra");
        }
    }

    public String suggest(ExerciseType requested, List<NormalizedLandmark> landmarks) {
        if (landmarks == null || landmarks.size() != 33) return null;
        NormalizedLandmark left = landmarks.get(11), right = landmarks.get(12), nose = landmarks.get(0);
        float width = (float) Math.hypot(left.x() - right.x(), left.y() - right.y());
        if (width < 0.02f || visibility(nose) < 0.35f
                || visibility(left) < 0.35f || visibility(right) < 0.35f) return null;
        float cx = (left.x() + right.x()) / 2f;
        float cy = (left.y() + right.y()) / 2f;
        float cz = (left.z() + right.z()) / 2f;
        for (int i = 0; i < 33; i++) {
            NormalizedLandmark p = landmarks.get(i);
            if (!Float.isFinite(p.x()) || !Float.isFinite(p.y()) || !Float.isFinite(p.z())) return null;
            input[0][i * 4] = (p.x() - cx) / width;
            input[0][i * 4 + 1] = (p.y() - cy) / width;
            input[0][i * 4 + 2] = (p.z() - cz) / width;
            input[0][i * 4 + 3] = visibility(p);
        }
        interpreter.run(input, output);
        int best = -1;
        float confidence = 0.55f;
        for (int i = 0; i < output[0].length; i++) {
            float score = output[0][i];
            if (Float.isFinite(score) && score > confidence) {
                confidence = score;
                best = i;
            }
        }
        return best < 0 ? null : guidanceFor(requested, EXPECTED_LABELS[best]);
    }

    static String guidanceFor(ExerciseType requested, String label) {
        ExerciseType detected;
        if (label.equals("cang_tay_gap_len")) detected = ExerciseType.WRIST_UP;
        else if (label.equals("cang_tay_gap_xuong")) detected = ExerciseType.WRIST_DOWN;
        else if (label.equals("canh_tay_phai") || label.equals("canh_tay_trai"))
            detected = ExerciseType.SHOULDER;
        else if (label.startsWith("co_cheo_") || label.startsWith("co_nghieng_"))
            detected = ExerciseType.NECK;
        else return null;
        return detected == requested ? null
                : "Camera nhận thấy " + detected.title.toLowerCase() + "; hãy tập "
                        + requested.title.toLowerCase();
    }

    private static float visibility(NormalizedLandmark p) {
        return p.visibility().orElse(0f);
    }

    @Override public void close() { interpreter.close(); }
}
