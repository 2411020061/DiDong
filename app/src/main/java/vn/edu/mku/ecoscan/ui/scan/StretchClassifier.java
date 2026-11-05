package vn.edu.mku.ecoscan.ui.scan;

import android.content.Context;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.tensorflow.lite.Interpreter;

public final class StretchClassifier implements AutoCloseable {
  private final Interpreter interpreter;
  private final List<String> labels = new ArrayList<>();

  public StretchClassifier(Context context) throws IOException {
    byte[] bytes;
    try (InputStream in = context.getAssets().open("stretch_classifier.tflite");
         ByteArrayOutputStream dst = new ByteArrayOutputStream()) {
      byte[] chunk = new byte[8192]; int n;
      while ((n = in.read(chunk)) != -1) dst.write(chunk, 0, n);
      bytes = dst.toByteArray();
    }
    ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
    buffer.put(bytes); buffer.rewind();
    interpreter = new Interpreter(buffer);
    try (BufferedReader r = new BufferedReader(new InputStreamReader(
        context.getAssets().open("labels.txt"), StandardCharsets.UTF_8))) {
      String line; while ((line = r.readLine()) != null) if (!line.trim().isEmpty()) labels.add(line.trim());
    }
    if (!Arrays.equals(interpreter.getInputTensor(0).shape(), new int[]{1,99}) ||
        !Arrays.equals(interpreter.getOutputTensor(0).shape(), new int[]{1,labels.size()}))
      throw new IllegalArgumentException("Unexpected model shape or labels");
  }

  // Run off the UI thread, serially. Never call run/close concurrently.
  public synchronized float[] classifyRaw(float[] xyz) {
    if (xyz.length != 99) throw new IllegalArgumentException("Need 99 floats");
    for (float v : xyz) if (Float.isNaN(v) || Float.isInfinite(v))
      throw new IllegalArgumentException("Invalid coordinates");
    float[][] output = new float[1][labels.size()];
    interpreter.run(new float[][]{xyz}, output);
    return output[0];
  }

  public float[] classifyLandmarks(List<NormalizedLandmark> landmarks, float minVisibility) {
    if (landmarks == null || landmarks.size() != 33) return null;
    float[] xyz = new float[99];
    for (int i = 0; i < 33; i++) {
      NormalizedLandmark p = landmarks.get(i);
      if (p == null || p.visibility().orElse(0f) < minVisibility) return null;
      xyz[i*3] = p.x();
      xyz[i*3+1] = p.y();
      xyz[i*3+2] = p.z();
    }
    return classifyRaw(xyz);
  }

  public String labelAt(int index) { return labels.get(index); }

  @Override public synchronized void close() { interpreter.close(); }
}
