package vn.edu.mku.ecoscan.ui.scan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Locale;

/** A compact, accessible progress ring for the 15-second exercise. */
public class ExerciseTimerView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int seconds;

    public ExerciseTimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setContentDescription("Thời gian giữ đúng: còn 15 giây");
    }

    public void setSeconds(int value) {
        seconds = Math.max(0, Math.min(15, value));
        setContentDescription("Còn " + seconds + " giây giữ đúng tư thế");
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float d = getResources().getDisplayMetrics().density;
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float r = Math.min(cx, cy) - 8 * d;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, r + 6 * d, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8 * d);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.rgb(219, 237, 225));
        RectF arc = new RectF(cx - r + 5*d, cy - r + 5*d, cx + r - 5*d, cy + r - 5*d);
        canvas.drawArc(arc, -90, 360, false, paint);
        paint.setColor(Color.rgb(3, 165, 93));
        canvas.drawArc(arc, -90, (15 - seconds) * 24f, false, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.rgb(11, 35, 69));
        paint.setTextSize(11 * d);
        canvas.drawText("Giữ đúng", cx, cy - 23 * d, paint);
        paint.setFakeBoldText(true);
        paint.setColor(Color.rgb(0, 149, 80));
        paint.setTextSize(28 * d);
        canvas.drawText(String.format(Locale.getDefault(), "00:%02d", seconds), cx, cy + 8 * d, paint);
        paint.setFakeBoldText(false);
        paint.setColor(Color.rgb(96, 116, 146));
        paint.setTextSize(11 * d);
        canvas.drawText("/ 00:15", cx, cy + 29 * d, paint);
    }
}
