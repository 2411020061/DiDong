package vn.edu.mku.ecoscan.ui.scan;

import java.nio.ByteBuffer;

/** Reads each YUV_420_888 plane independently, including padding and shared UV buffers. */
final class Yuv420Pixels {
    static int[] toArgb(int width, int height, ByteBuffer y, int yRow, int yPixel,
            ByteBuffer u, int uRow, int uPixel, ByteBuffer v, int vRow, int vPixel) {
        int[] pixels = new int[width * height];
        int yStart = y.position(), uStart = u.position(), vStart = v.position();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int yy = Math.max(0, (y.get(yStart + row * yRow + col * yPixel) & 255) - 16);
                int uu = (u.get(uStart + row / 2 * uRow + col / 2 * uPixel) & 255) - 128;
                int vv = (v.get(vStart + row / 2 * vRow + col / 2 * vPixel) & 255) - 128;
                int r = clamp((298 * yy + 409 * vv + 128) >> 8);
                int g = clamp((298 * yy - 100 * uu - 208 * vv + 128) >> 8);
                int b = clamp((298 * yy + 516 * uu + 128) >> 8);
                pixels[row * width + col] = 0xff000000 | r << 16 | g << 8 | b;
            }
        }
        return pixels;
    }

    private static int clamp(int value) { return Math.max(0, Math.min(255, value)); }
}
