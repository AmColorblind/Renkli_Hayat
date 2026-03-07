package com.renlihayat.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class ColorOverlayView extends View {

    private Paint overlayPaint;

    public ColorOverlayView(Context context) {
        super(context);
        overlayPaint = new Paint();
        setLayerType(LAYER_TYPE_HARDWARE, overlayPaint);
    }

    public void setPaint(Paint paint) {
        this.overlayPaint = paint;
        setLayerType(LAYER_TYPE_HARDWARE, overlayPaint);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Boş bırak — layer paint her şeyi halleder
    }
}
