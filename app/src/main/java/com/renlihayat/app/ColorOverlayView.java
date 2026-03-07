package com.renlihayat.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class ColorOverlayView extends View {

    private Paint overlayPaint = new Paint();

    public ColorOverlayView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public void setPaint(Paint paint) {
        this.overlayPaint = paint;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w > 0 && h > 0) {
            canvas.drawRect(0, 0, w, h, overlayPaint);
        }
    }
}
