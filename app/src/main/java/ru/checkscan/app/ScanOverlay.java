package ru.checkscan.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Draws a guide over the camera preview; recognition still covers the full frame. */
public class ScanOverlay extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path shade = new Path();
    private final RectF guide = new RectF();

    public ScanOverlay(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float size = Math.min(getWidth() * 0.72f, 300 * density);
        float left = (getWidth() - size) / 2;
        float top = (getHeight() - size) / 2;
        guide.set(left, top, left + size, top + size);

        shade.reset();
        shade.setFillType(Path.FillType.EVEN_ODD);
        shade.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        shade.addRoundRect(guide, 24 * density, 24 * density, Path.Direction.CW);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x990B1711);
        canvas.drawPath(shade, paint);

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3 * density);
        paint.setStrokeCap(Paint.Cap.ROUND);
        float inset = 2 * density;
        float length = 27 * density;
        float l = guide.left + inset, r = guide.right - inset;
        float t = guide.top + inset, b = guide.bottom - inset;
        Path corners = new Path();
        corners.moveTo(l, t + length);
        corners.lineTo(l, t + 10 * density);
        corners.quadTo(l, t, l + 10 * density, t);
        corners.lineTo(l + length, t);
        corners.moveTo(r - length, t);
        corners.lineTo(r - 10 * density, t);
        corners.quadTo(r, t, r, t + 10 * density);
        corners.lineTo(r, t + length);
        corners.moveTo(l, b - length);
        corners.lineTo(l, b - 10 * density);
        corners.quadTo(l, b, l + 10 * density, b);
        corners.lineTo(l + length, b);
        corners.moveTo(r - length, b);
        corners.lineTo(r - 10 * density, b);
        corners.quadTo(r, b, r, b - 10 * density);
        corners.lineTo(r, b - length);
        canvas.drawPath(corners, paint);
    }
}
