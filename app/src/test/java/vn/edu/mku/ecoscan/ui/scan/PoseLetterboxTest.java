package vn.edu.mku.ecoscan.ui.scan;

import org.junit.Test;
import vn.edu.mku.ecoscan.ml.PoseLetterbox;
import static org.junit.Assert.*;

public class PoseLetterboxTest {
    @Test public void portraitAndLandscapeMapPaddingBackToCameraCoordinates() {
        PoseLetterbox portrait = new PoseLetterbox(480, 640, 640);
        assertEquals(80, portrait.left);
        assertEquals(0f, portrait.x(80f / 640), 0.0001f);
        assertEquals(1f, portrait.x(560f / 640), 0.0001f);
        assertEquals(0.5f, portrait.y(0.5f), 0.0001f);
        PoseLetterbox landscape = new PoseLetterbox(640, 480, 640);
        assertEquals(80, landscape.top);
        assertEquals(0f, landscape.y(80f / 640), 0.0001f);
        assertEquals(1f, landscape.y(560f / 640), 0.0001f);
        assertTrue(landscape.y(0) < 0); // Padding is outside the visible image.
    }
}
