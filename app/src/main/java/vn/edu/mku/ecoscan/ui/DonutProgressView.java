package vn.edu.mku.ecoscan.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DonutProgressView extends View {
    private Paint bgPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private final RectF rectF = new RectF();
    private int progress = 78;

    public DonutProgressView(Context context) {
        super(context);
        init();
    }

    public DonutProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#E5F4ED"));
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(5f * density);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(Color.parseColor("#0AA665"));
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(5f * density);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#10234A"));
        textPaint.setTextSize(15f * density);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float density = getResources().getDisplayMetrics().density;

        float padding = 6f * density;
        rectF.set(padding, padding, w - padding, h - padding);

        // Background ring
        canvas.drawArc(rectF, 0, 360, false, bgPaint);

        // Progress arc starting from top (-90 degrees)
        float sweep = (progress / 100f) * 360f;
        canvas.drawArc(rectF, -90, sweep, false, progressPaint);

        // Center text
        float centerY = h / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(progress + "%", w / 2f, centerY, textPaint);
    }
}
