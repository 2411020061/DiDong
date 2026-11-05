package vn.edu.mku.ecoscan.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Arrays;

/** Seven-day bar chart of completed exercise sessions with exercise name on top. */
public class TrackingChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int[] counts = new int[7];
    private final String[] days = new String[7];
    private final String[] exerciseNames = new String[7];
    private int maximum = 1;

    public TrackingChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(int[] values, String[] labels, String[] names) {
        maximum = 1;
        for (int i = 0; i < 7; i++) {
            counts[i] = values[i];
            days[i] = labels[i];
            if (names != null && i < names.length) exerciseNames[i] = names[i];
            maximum = Math.max(maximum, counts[i]);
        }
        setContentDescription("Biểu đồ cột lượt tập trong 7 ngày: " + Arrays.toString(counts));
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float left = 36 * density;
        float right = getWidth() - 15 * density;
        float top = 40 * density; // extra top space for exercise name text
        float bottom = getHeight() - 32 * density;
        float chartHeight = bottom - top;
        if (right <= left || chartHeight <= 0) return;
        boolean hasSession = false;
        for (int count : counts) if (count > 0) { hasSession = true; break; }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(density);
        paint.setColor(0xFFE0E9EF);
        for (int row = 0; row <= 3; row++) {
            float y = bottom - chartHeight * row / 3f;
            canvas.drawLine(left, y, right, y, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(11 * density);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(0xFF71839B);
        if (hasSession) {
            canvas.drawText(String.valueOf(maximum), left - 8 * density, top + 4 * density, paint);
            canvas.drawText("0", left - 8 * density, bottom + 4 * density, paint);
        }

        if (!hasSession) {
            paint.setColor(0xFF607492);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(12 * density);
            canvas.drawText("Chưa có lượt tập", getWidth() / 2f,
                    top + chartHeight / 2f, paint);
        }

        float step = (right - left) / 6f;
        float barWidth = step * 0.45f;

        for (int i = 0; i < 7; i++) {
            float x = left + i * step;
            float barHeight = hasSession && maximum > 0 ? chartHeight * counts[i] / maximum : 0;
            float barTop = bottom - barHeight;
            float barLeft = x - barWidth / 2f;
            float barRight = x + barWidth / 2f;

            if (counts[i] > 0) {
                // Draw bar column
                paint.setStyle(Paint.Style.FILL);
                paint.setShader(new LinearGradient(barLeft, barTop, barLeft, bottom,
                        0xFF31C889, 0xFF08A364, Shader.TileMode.CLAMP));
                RectF rect = new RectF(barLeft, barTop, barRight, bottom);
                canvas.drawRoundRect(rect, 6 * density, 6 * density, paint);
                paint.setShader(null);

                // Draw exercise name on top of column
                if (exerciseNames[i] != null && !exerciseNames[i].isEmpty()) {
                    paint.setColor(0xFF08A364);
                    paint.setTextSize(10 * density);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(exerciseNames[i], x, barTop - 8 * density, paint);
                }
            } else {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0xFFE0E9EF);
                canvas.drawCircle(x, bottom - 4 * density, 3 * density, paint);
            }

            // Day label
            paint.setColor(0xFF607492);
            paint.setTextSize(10 * density);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(days[i] == null ? "" : days[i], x, bottom + 18 * density, paint);
        }
    }
}
