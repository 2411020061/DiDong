package vn.edu.mku.ecoscan.ui.scan;

import org.junit.Test;
import java.nio.ByteBuffer;
import static org.junit.Assert.*;

public class Yuv420PixelsTest {
    @Test public void readsPaddedRowsAndSharedInterleavedChromaWithOffsets() {
        byte[] packedY = new byte[16];
        for (int i = 0; i < 16; i++) packedY[i] = (byte) (16 + i * 10);
        byte[] packedU = {90, (byte) 128, (byte) 180, 100};
        byte[] packedV = {(byte) 240, (byte) 128, 100, (byte) 180};
        int[] expected = Yuv420Pixels.toArgb(4, 4, ByteBuffer.wrap(packedY), 4, 1,
                ByteBuffer.wrap(packedU), 2, 1, ByteBuffer.wrap(packedV), 2, 1);
        ByteBuffer y = ByteBuffer.allocate(26);
        y.position(2);
        for (int row = 0; row < 4; row++)
            for (int col = 0; col < 4; col++) y.put(2 + row * 6 + col, packedY[row * 4 + col]);
        ByteBuffer uv = ByteBuffer.allocate(14);
        for (int row = 0; row < 2; row++)
            for (int col = 0; col < 2; col++) {
                uv.put(3 + row * 6 + col * 2, packedU[row * 2 + col]);
                uv.put(4 + row * 6 + col * 2, packedV[row * 2 + col]);
            }
        ByteBuffer u = uv.duplicate(), v = uv.duplicate();
        u.position(3); v.position(4);
        assertArrayEquals(expected, Yuv420Pixels.toArgb(4, 4, y, 6, 1, u, 6, 2, v, 6, 2));
        assertEquals(2, y.position());
        assertEquals(3, u.position());
        assertEquals(4, v.position());
    }

    @Test public void keepsRedRedAndProducesOpaqueBlackAndWhite() {
        int red = convert(82, 90, 240);
        assertTrue(((red >> 16) & 255) >= 250);
        assertTrue(((red >> 8) & 255) < 5);
        assertTrue((red & 255) < 5);
        assertEquals(0xff000000, convert(16, 128, 128));
        assertEquals(0xffffffff, convert(235, 128, 128));
    }

    private int convert(int y, int u, int v) {
        return Yuv420Pixels.toArgb(1, 1, ByteBuffer.wrap(new byte[]{(byte)y}), 1, 1,
                ByteBuffer.wrap(new byte[]{(byte)u}), 1, 1,
                ByteBuffer.wrap(new byte[]{(byte)v}), 1, 1)[0];
    }
}
