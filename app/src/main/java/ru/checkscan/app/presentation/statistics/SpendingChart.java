package ru.checkscan.app.presentation.statistics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import ru.checkscan.app.domain.model.SpendingReport;

import java.util.List;
import java.util.function.Consumer;

/** Scrollable bar chart; each bar opens the receipts behind it. */
public class SpendingChart extends View {
    private final List<SpendingReport.Bucket> buckets;
    private final Consumer<SpendingReport.Bucket> onSelect;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int selected = -1;
    private final float density;

    public SpendingChart(
            Context context,
            List<SpendingReport.Bucket> buckets,
            Consumer<SpendingReport.Bucket> onSelect) {
        super(context);
        this.buckets = buckets;
        this.onSelect = onSelect;
        density = getResources().getDisplayMetrics().density;
        setContentDescription("График расходов. Нажмите на столбец, чтобы открыть чеки за период.");
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int wanted = (int) (Math.max(300, buckets.size() * 42) * density);
        setMeasuredDimension(resolveSize(wanted, widthSpec), (int) (205 * density));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (buckets.isEmpty()) return;
        float top = 12 * density, bottom = getHeight() - 32 * density, height = bottom - top;
        long max = 0;
        for (SpendingReport.Bucket bucket : buckets) max = Math.max(max, bucket.total);
        float cell = (float) getWidth() / buckets.size();
        paint.setColor(0xffe7eee8);
        paint.setStrokeWidth(density);
        canvas.drawLine(0, bottom, getWidth(), bottom, paint);
        paint.setTextSize(10 * density);
        paint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < buckets.size(); i++) {
            SpendingReport.Bucket bucket = buckets.get(i);
            float center = cell * (i + .5f);
            float barHeight = max > 0 ? height * Math.max(0, bucket.total) / (float) max : 0;
            if (bucket.total > 0) {
                paint.setColor(i == selected ? 0xff173f31 : 0xff4f8b70);
                float half = Math.min(13 * density, cell * .29f);
                canvas.drawRoundRect(
                        new RectF(
                                center - half,
                                bottom - Math.max(3 * density, barHeight),
                                center + half,
                                bottom),
                        5 * density,
                        5 * density,
                        paint);
            }
            paint.setColor(0xff6c7a70);
            canvas.drawText(bucket.label, center, bottom + 19 * density, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && !buckets.isEmpty()) {
            int index =
                    Math.min(
                            buckets.size() - 1,
                            Math.max(0, (int) (event.getX() / getWidth() * buckets.size())));
            selected = index;
            invalidate();
            performClick();
            onSelect.accept(buckets.get(index));
            return true;
        }
        return event.getAction() == MotionEvent.ACTION_DOWN || super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
