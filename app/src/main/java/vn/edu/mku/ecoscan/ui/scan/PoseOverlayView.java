package vn.edu.mku.ecoscan.ui.scan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.List;

public class PoseOverlayView extends View {

    private Paint dotPaint;
    private Paint dotBorderPaint;
    private Paint linePaint;
    private final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float[] personBox;
    private boolean correct;

    public void setPersonBox(float[] box) {
        personBox = box == null ? null : box.clone();
        invalidate();
    }

    private List<NormalizedLandmark> mediaPipePose;
    private float[] mediaPipeCrop;
    private int imageWidth = 480;
    private int imageHeight = 640;
    private boolean isFrontCamera = false;
    private String currentMode = "Exercise";

    private static final int[][] POSE_CONNECTIONS = {
            // Face
            {0, 1}, {1, 2}, {2, 3}, {3, 7}, {0, 4}, {4, 5}, {5, 6}, {6, 8}, {9, 10},
            // Torso
            {11, 12}, {11, 23}, {12, 24}, {23, 24},
            // Left Arm
            {11, 13}, {13, 15}, {15, 17}, {15, 19}, {15, 21}, {17, 19},
            // Right Arm
            {12, 14}, {14, 16}, {16, 18}, {16, 20}, {16, 22}, {18, 20},
            // Left Leg
            {23, 25}, {25, 27}, {27, 29}, {27, 31}, {29, 31},
            // Right Leg
            {24, 26}, {26, 28}, {28, 30}, {28, 32}, {30, 32}
    };

    public PoseOverlayView(Context context) {
        super(context);
        init();
    }

    public PoseOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.parseColor("#12C875"));
        dotPaint.setStyle(Paint.Style.FILL);

        dotBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotBorderPaint.setColor(Color.WHITE);
        dotBorderPaint.setStyle(Paint.Style.STROKE);
        dotBorderPaint.setStrokeWidth(2f * density);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#12C875"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3.5f * density);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setMediaPipePose(List<NormalizedLandmark> landmarks, int imgWidth,
            int imgHeight, boolean frontCamera, float[] crop) {
        mediaPipePose = (landmarks != null && !landmarks.isEmpty()) ? landmarks : null;
        imageWidth = imgWidth;
        imageHeight = imgHeight;
        isFrontCamera = frontCamera;
        mediaPipeCrop = crop;
        invalidate();
    }

    public void setMode(String mode) {
        this.currentMode = mode;
        int color = Color.parseColor("Exercise".equals(mode) ? "#12C875" : "#00E5FF");
        dotPaint.setColor(color);
        linePaint.setColor(color);
        invalidate();
    }

    public void setExerciseCorrectness(boolean correct) {
        this.correct = correct;
        int color = Color.parseColor(correct ? "#087D4A" : "#FFB300");
        dotPaint.setColor(color);
        linePaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        if (mediaPipePose != null && personBox != null) {
            drawPersonBox(canvas, viewWidth, viewHeight);
        }
        if (mediaPipePose != null) {
            drawMediaPipePose(canvas, viewWidth, viewHeight);
        }
    }

    private void drawPersonBox(Canvas canvas, int width, int height) {
        float[] crop = mediaPipeCrop == null
                ? new float[] {0, 0, imageWidth, imageHeight} : mediaPipeCrop;
        float[] a = PoseOverlayTransform.map(personBox[0], personBox[1], imageWidth,
                imageHeight, crop, width, height, isFrontCamera);
        float[] b = PoseOverlayTransform.map(personBox[2], personBox[3], imageWidth,
                imageHeight, crop, width, height, isFrontCamera);
        float left = Math.max(2, Math.min(a[0], b[0]));
        float right = Math.min(width - 2, Math.max(a[0], b[0]));
        float top = Math.max(2, Math.min(a[1], b[1]));
        float bottom = Math.min(height - 2, Math.max(a[1], b[1]));
        if (left >= right || top >= bottom) return;
        float d = getResources().getDisplayMetrics().density;
        boxPaint.setColor(Color.parseColor(correct ? "#087D4A" : "#FFB300"));
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(3 * d);
        canvas.drawRect(left, top, right, bottom, boxPaint);
        boxPaint.setStyle(Paint.Style.FILL);
        boxPaint.setTextSize(14 * d);
        canvas.drawText(String.format(java.util.Locale.US, "person %.2f · %s",
                personBox[4], correct ? "Đúng" : "Chưa đúng"), left + 4 * d,
                Math.min(bottom - 4 * d, top + 20 * d), boxPaint);
    }

    private void drawMediaPipePose(Canvas canvas, int viewWidth, int viewHeight) {
        if (mediaPipePose.size() != 33) return;

        float density = getResources().getDisplayMetrics().density;
        float radius = 6.5f * density;

        // 1. Draw skeleton connecting lines
        for (int[] connection : POSE_CONNECTIONS) {
            int startIdx = connection[0];
            int endIdx = connection[1];
            NormalizedLandmark startPoint = mediaPipePose.get(startIdx);
            NormalizedLandmark endPoint = mediaPipePose.get(endIdx);

            if (!visible(startPoint) || !visible(endPoint)) continue;

            float[] startPos = mapMediaPipe(startPoint, viewWidth, viewHeight);
            float[] endPos = mapMediaPipe(endPoint, viewWidth, viewHeight);

            if (!insideView(startPos, viewWidth, viewHeight) && !insideView(endPos, viewWidth, viewHeight))
                continue;

            canvas.drawLine(startPos[0], startPos[1], endPos[0], endPos[1], linePaint);
        }

        // 2. Draw dots at landmarks with clean white outline
        for (NormalizedLandmark landmark : mediaPipePose) {
            if (!visible(landmark)) continue;
            float[] position = mapMediaPipe(landmark, viewWidth, viewHeight);
            if (!insideView(position, viewWidth, viewHeight)) continue;

            canvas.drawCircle(position[0], position[1], radius, dotPaint);
            canvas.drawCircle(position[0], position[1], radius, dotBorderPaint);
        }
    }

    private boolean visible(NormalizedLandmark point) {
        if (point == null) return false;
        float v = point.visibility().orElse(1.0f);
        return v >= 0.1f && Float.isFinite(point.x()) && Float.isFinite(point.y());
    }

    private float[] mapMediaPipe(NormalizedLandmark point, int viewWidth, int viewHeight) {
        float[] crop = mediaPipeCrop == null
                ? new float[] {0, 0, imageWidth, imageHeight} : mediaPipeCrop;
        return PoseOverlayTransform.map(point.x(), point.y(), imageWidth, imageHeight,
                crop, viewWidth, viewHeight, isFrontCamera);
    }

    private boolean insideView(float[] point, int width, int height) {
        return Float.isFinite(point[0]) && Float.isFinite(point[1])
                && point[0] >= -50 && point[0] <= width + 50
                && point[1] >= -50 && point[1] <= height + 50;
    }
}
