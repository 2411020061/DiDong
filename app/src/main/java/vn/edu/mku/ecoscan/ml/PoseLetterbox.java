package vn.edu.mku.ecoscan.ml;

/** Geometry shared by YOLO input padding and output projection into the source frame. */
public final class PoseLetterbox {
    public final int width, height, left, top;
    private final int size;

    public PoseLetterbox(int imageWidth, int imageHeight, int size) {
        this.size = size;
        float scale = Math.min(size / (float) imageWidth, size / (float) imageHeight);
        width = Math.max(1, Math.round(imageWidth * scale));
        height = Math.max(1, Math.round(imageHeight * scale));
        left = (size - width) / 2;
        top = (size - height) / 2;
    }

    public float x(float normalized) { return (normalized * size - left) / width; }
    public float y(float normalized) { return (normalized * size - top) / height; }
}
