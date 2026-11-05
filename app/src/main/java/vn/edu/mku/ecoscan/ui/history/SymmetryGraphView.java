package vn.edu.mku.ecoscan.ui.history;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SymmetryGraphView extends View {

    private Paint linePaint;
    private Paint fillPaint;
    private Paint dotPaint;
    private Paint dotInnerPaint;
    private Paint gridPaint;
    private Paint textPaint;

    private float[] dataPoints = new float[]{88f, 91f, 89f, 92f, 91f, 93f, 94f};
    private String startDate = "28 SEP";
    private String endDate = "05 OCT";

    public SymmetryGraphView(Context context) {
        super(context);
        init();
    }

    public SymmetryGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#00E5FF"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.parseColor("#00E5FF"));
        dotPaint.setStyle(Paint.Style.FILL);

        dotInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotInnerPaint.setColor(Color.parseColor("#0A0E17"));
        dotInnerPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#1F2B42"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#94A3B8"));
        textPaint.setTextSize(28f);
    }

    public void setData(float[] points, String start, String end) {
        if (points != null && points.length > 0) {
            this.dataPoints = points;
        }
        if (start != null) this.startDate = start;
        if (end != null) this.endDate = end;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float paddingLeft = 40f;
        float paddingRight = 40f;
        float paddingTop = 30f;
        float paddingBottom = 60f;

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        // Grid lines
        for (int i = 0; i <= 3; i++) {
            float y = paddingTop + (chartHeight / 3) * i;
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);
        }

        if (dataPoints.length == 0) return;

        float minVal = 80f;
        float maxVal = 100f;

        float stepX = chartWidth / (dataPoints.length - 1);

        Path linePath = new Path();
        Path fillPath = new Path();

        float[] xCoords = new float[dataPoints.length];
        float[] yCoords = new float[dataPoints.length];

        for (int i = 0; i < dataPoints.length; i++) {
            float x = paddingLeft + i * stepX;
            float val = Math.max(minVal, Math.min(maxVal, dataPoints[i]));
            float y = paddingTop + chartHeight - ((val - minVal) / (maxVal - minVal)) * chartHeight;

            xCoords[i] = x;
            yCoords[i] = y;

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, paddingTop + chartHeight);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }

        fillPath.lineTo(xCoords[dataPoints.length - 1], paddingTop + chartHeight);
        fillPath.close();

        // Gradient Fill
        LinearGradient gradient = new LinearGradient(
                0, paddingTop, 0, paddingTop + chartHeight,
                Color.parseColor("#3300E5FF"), Color.parseColor("#0000E5FF"),
                Shader.TileMode.CLAMP
        );
        fillPaint.setShader(gradient);

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // Draw data points
        for (int i = 0; i < dataPoints.length; i++) {
            canvas.drawCircle(xCoords[i], yCoords[i], 10f, dotPaint);
            if (i == dataPoints.length - 1) {
                canvas.drawCircle(xCoords[i], yCoords[i], 5f, dotInnerPaint);
            }
        }

        // X-Axis Labels
        canvas.drawText(startDate, paddingLeft, height - 15f, textPaint);

        float endTextWidth = textPaint.measureText(endDate);
        canvas.drawText(endDate, width - paddingRight - endTextWidth, height - 15f, textPaint);

        // Legend Text
        String legend = "— Torso symmetry";
        float legendWidth = textPaint.measureText(legend);
        canvas.drawText(legend, (width - legendWidth) / 2f, height - 15f, textPaint);
    }
}