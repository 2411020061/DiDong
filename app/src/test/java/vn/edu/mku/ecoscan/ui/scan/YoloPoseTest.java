package vn.edu.mku.ecoscan.ui.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeNoException;

import android.content.Context;
import android.graphics.Bitmap;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.io.FileNotFoundException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import vn.edu.mku.ecoscan.ml.YoloPoseDetector;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class YoloPoseTest {

    private YoloPoseDetector detector;

    @Before
    public void setUp() {
        try {
            Context context = RuntimeEnvironment.getApplication();
            detector = new YoloPoseDetector(context);
        } catch (UnsatisfiedLinkError | FileNotFoundException e) {
            assumeNoException("TFLite native JNI or asset unavailable on host JVM", e);
        } catch (Exception e) {
            if (e.getCause() instanceof FileNotFoundException) {
                assumeNoException("TFLite asset unavailable on host JVM", (Exception) e.getCause());
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @After
    public void tearDown() {
        if (detector != null) {
            detector.close();
        }
    }

    @Test
    public void testDetectNullBitmapReturnsNull() {
        if (detector == null) return;
        assertNull(detector.detect(null));
    }

    @Test
    public void testDetectBlankBitmapReturnsNullOrLandmarks() {
        if (detector == null) return;
        Bitmap blank = Bitmap.createBitmap(640, 640, Bitmap.Config.ARGB_8888);
        List<NormalizedLandmark> landmarks = detector.detect(blank);
        if (landmarks != null) {
            assertEquals(33, landmarks.size());
        }
        blank.recycle();
    }
}
