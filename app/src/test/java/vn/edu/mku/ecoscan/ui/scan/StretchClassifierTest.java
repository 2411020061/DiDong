package vn.edu.mku.ecoscan.ui.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeNoException;

import android.content.Context;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class StretchClassifierTest {

    private static final float[] TEST_INPUT_XYZ = new float[] {
        261.32019f, 199.09206f, -158.13133f,
        269.37787f, 184.0844f, -142.97273f,
        274.05264f, 183.91643f, -143.04102f,
        278.34732f, 184.05118f, -143.14842f,
        252.1326f, 184.48917f, -144.67049f,
        244.50589f, 184.53528f, -144.7423f,
        237.95615f, 184.78246f, -144.82773f,
        282.01593f, 188.49382f, -40.6572f,
        226.5022f, 188.6751f, -44.680828f,
        267.31985f, 214.43315f, -118.13474f,
        248.64024f, 216.24765f, -119.41192f,
        313.78912f, 266.12106f, -62.370163f,
        180.17389f, 264.2254f, -6.4869437f,
        265.56293f, 367.7067f, -91.98472f,
        93.0701f, 332.98486f, -106.25236f,
        198.4387f, 378.92834f, -181.88333f,
        91.47326f, 233.83566f, -290.53513f,
        182.27184f, 393.42828f, -196.35678f,
        81.64832f, 204.10301f, -314.62732f,
        184.41246f, 377.43765f, -198.91968f,
        88.746765f, 193.87181f, -309.46365f,
        185.90828f, 371.42398f, -182.97308f,
        92.38253f, 205.92569f, -291.73242f,
        275.5635f, 463.78946f, -4.8593764f,
        193.32124f, 463.61093f, 4.8037524f,
        404.6103f, 532.5713f, -288.80118f,
        73.243004f, 528.47272f, -284.27942f,
        235.55838f, 513.50787f, -55.034657f,
        248.44252f, 533.953f, -103.18645f,
        221.54387f, 488.5885f, -18.869602f,
        257.80154f, 509.49905f, -71.34577f,
        162.41621f, 520.00616f, -24.449055f,
        327.83716f, 550.5921f, -92.132225f
    };

    private static final float[] EXPECTED_PROBABILITIES = new float[] {
        0.0019116f, 0.016350083f, 0.00022776869f, 0.00014364095f,
        0.0009651394f, 0.000104063525f, 0.042429436f, 0.0011175296f,
        2.296881e-05f, 0.93672776f
    };

    private StretchClassifier classifier;

    @Before
    public void setUp() {
        try {
            Context context = RuntimeEnvironment.getApplication();
            classifier = new StretchClassifier(context);
        } catch (UnsatisfiedLinkError e) {
            assumeNoException("TFLite native JNI library unavailable on host JVM", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() {
        if (classifier != null) {
            classifier.close();
        }
    }

    @Test
    public void testClassifyRawMatchesTestVector() {
        if (classifier == null) return;
        float[] probs = classifier.classifyRaw(TEST_INPUT_XYZ);
        assertNotNull(probs);
        assertEquals(EXPECTED_PROBABILITIES.length, probs.length);

        int bestIndex = -1;
        float maxProb = -1f;
        for (int i = 0; i < probs.length; i++) {
            assertEquals(EXPECTED_PROBABILITIES[i], probs[i], 1e-4f);
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                bestIndex = i;
            }
        }

        assertEquals("vai_trai", classifier.labelAt(bestIndex));
    }

    @Test
    public void testClassifyLandmarksRejectsNullOrLowVisibility() {
        if (classifier == null) return;
        assertNull(classifier.classifyLandmarks(null, 0.5f));

        List<NormalizedLandmark> incomplete = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            incomplete.add(NormalizedLandmark.create(0.5f, 0.5f, 0f, Optional.of(0.9f), Optional.empty()));
        }
        assertNull(classifier.classifyLandmarks(incomplete, 0.5f));
    }
}
