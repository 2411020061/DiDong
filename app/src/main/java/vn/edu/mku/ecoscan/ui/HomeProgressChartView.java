package vn.edu.mku.ecoscan.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HomeProgressChartView extends View {
    private Paint linePaint;
    private Paint fillPaint;
    private Paint gridPaint;
    private Paint dotBgPaint;
    private Paint dotBorderPaint;
    private Paint textLabelPaint;
    private Paint textYLabelPaint;
    private Paint textValuePaint;
    private Paint badgeBgPaint;
    private Paint badgeTextPaint;

    private final int[] values = {62, 66, 70, 74, 78, 82, 88};
    private final String[] dates = {"12/04", "13/04", "14/04", "15/04", "16/04", "17/04", "18/04"};

    public HomeProgressChartView(Context context) {
        super(context);
        init();
    }

    public HomeProgressChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#0AA665"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2.5f * density);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#EAF2ED"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(density);

        dotBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotBgPaint.setColor(Color.WHITE);
        dotBgPaint.setStyle(Paint.Style.FILL);

        dotBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotBorderPaint.setColor(Color.parseColor("#0AA665"));
        dotBorderPaint.setStyle(Paint.Style.STROKE);
        dotBorderPaint.setStrokeWidth(2f * density);

        textLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textLabelPaint.setColor(Color.parseColor("#8094A8"));
        textLabelPaint.setTextSize(11f * density);
        textLabelPaint.setTextAlign(Paint.Align.CENTER);

        textYLabelPaint = new Paint(textLabelPaint);
        textYLabelPaint.setTextAlign(Paint.Align.RIGHT);

        textValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textValuePaint.setColor(Color.parseColor("#10234A"));
        textValuePaint.setTextSize(10.5f * density);
        textValuePaint.setFakeBoldText(true);
        textValuePaint.setTextAlign(Paint.Align.CENTER);

        badgeBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgeBgPaint.setColor(Color.parseColor("#0AA665"));
        badgeBgPaint.setStyle(Paint.Style.FILL);

        badgeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgeTextPaint.setColor(Color.WHITE);
        badgeTextPaint.setTextSize(11f * density);
        badgeTextPaint.setFakeBoldText(true);
        badgeTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float density = getResources().getDisplayMetrics().density;
        float paddingLeft = 40f * density;
        float paddingRight = 20f * density;
        float paddingTop = 40f * density;
        float paddingBottom = 30f * density;

        float chartW = w - paddingLeft - paddingRight;
        float chartH = h - paddingTop - paddingBottom;

        // Draw Y-axis grid lines (0%, 25%, 50%, 75%, 100%)
        int[] gridPercents = {0, 25, 50, 75, 100};
        for (int p : gridPercents) {
            float y = paddingTop + chartH * (1f - p / 100f);
            canvas.drawLine(paddingLeft, y, w - paddingRight, y, gridPaint);
            canvas.drawText(p + "%", paddingLeft - 6f * density, y + 4f * density, textYLabelPaint);
        }

        // Compute points
        int count = values.length;
        float[] pointsX = new float[count];
        float[] pointsY = new float[count];
        float stepX = chartW / (count - 1);

        for (int i = 0; i < count; i++) {
            pointsX[i] = paddingLeft + i * stepX;
            pointsY[i] = paddingTop + chartH * (1f - values[i] / 100f);
        }

        // Fill area gradient
        Path fillPath = new Path();
        fillPath.moveTo(pointsX[0], paddingTop + chartH);
        for (int i = 0; i < count; i++) {
            fillPath.lineTo(pointsX[i], pointsY[i]);
        }
        fillPath.lineTo(pointsX[count - 1], paddingTop + chartH);
        fillPath.close();

        LinearGradient shader = new LinearGradient(0, paddingTop, 0, paddingTop + chartH,
                Color.parseColor("#350AA665"), Color.parseColor("#050AA665"), Shader.TileMode.CLAMP);
        fillPaint.setShader(shader);
        canvas.drawPath(fillPath, fillPaint);

        // Draw smooth or segmented line
        Path linePath = new Path();
        linePath.moveTo(pointsX[0], pointsY[0]);
        for (int i = 1; i < count; i++) {
            linePath.lineTo(pointsX[i], pointsY[i]);
        }
        canvas.drawPath(linePath, linePaint);

        // Draw points, value labels, date labels
        for (int i = 0; i < count; i++) {
            float px = pointsX[i];
            float py = pointsY[i];

            // Point dot
            canvas.drawCircle(px, py, 4.5f * density, dotBgPaint);
            canvas.drawCircle(px, py, 4.5f * density, dotBorderPaint);

            // Percentage text above dot
            canvas.drawText(values[i] + "%", px, py - 8f * density, textValuePaint);

            // Date text below chart
            canvas.drawText(dates[i], px, h - 8f * density, textLabelPaint);
        }

        // Highlight badge at latest point (+26%)
        float lastX = pointsX[count - 1];
        float lastY = pointsY[count - 1];
        RectF badgeRect = new RectF(lastX - 22f * density, lastY - 32f * density,
                lastX + 22f * density, lastY - 14f * density);
        canvas.drawRoundRect(badgeRect, 10f * density, 10f * density, badgeBgPaint);
        canvas.drawText("+26%", lastX, badgeRect.centerY() + 4f * density, badgeTextPaint);
    }
}
