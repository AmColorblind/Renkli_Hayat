package com.renlihayat.app;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

public class ColorCorrectionService extends AccessibilityService {

    private static ColorCorrectionService instance;
    private WindowManager windowManager;
    private FrameLayout overlayLayout;
    private boolean isOverlayAdded = false;

    public static ColorCorrectionService getInstance() { return instance; }

    private static final float[] MATRIX_DEUTERANOPIA = {
        1.0f, 0.0f, 0.0f, 0, 0,
        0.4f, 0.0f, 0.6f, 0, 0,
        0.0f, 0.0f, 1.0f, 0, 0,
        0,    0,    0,    1, 0
    };
    private static final float[] MATRIX_PROTANOPIA = {
        0.0f, 1.05f, -0.05f, 0, 0,
        0.0f, 1.0f,   0.0f,  0, 0,
        0.0f, 0.0f,   1.0f,  0, 0,
        0,    0,      0,     1, 0
    };
    private static final float[] MATRIX_TRITANOPIA = {
        1.0f,  0.0f, 0.0f, 0, 0,
        0.0f,  1.0f, 0.0f, 0, 0,
        -0.4f, 0.5f, 0.9f, 0, 0,
        0,     0,    0,    1, 0
    };
    private static final float[] MATRIX_MONOCHROMACY = {
        0.5f, 0.5f, 0.0f, 0, 30,
        0.0f, 0.5f, 0.5f, 0, 30,
        0.5f, 0.0f, 0.5f, 0, 30,
        0,    0,    0,    1, 0
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        SharedPreferences prefs = getSharedPreferences("renkli_hayat", MODE_PRIVATE);
        if (prefs.getBoolean("filter_active", false)) addOverlay();
    }

    public void addOverlay() {
        if (isOverlayAdded) return;
        try {
            overlayLayout = new FrameLayout(this);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSPARENT
            );

            windowManager.addView(overlayLayout, params);
            isOverlayAdded = true;
            applyCurrentSettings();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void removeOverlay() {
        if (!isOverlayAdded || overlayLayout == null) return;
        try {
            overlayLayout.setLayerType(android.view.View.LAYER_TYPE_NONE, null);
            windowManager.removeView(overlayLayout);
            isOverlayAdded = false;
            overlayLayout = null;
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void applyCurrentSettings() {
        if (overlayLayout == null) return;
        SharedPreferences prefs = getSharedPreferences("renkli_hayat", MODE_PRIVATE);
        String type = prefs.getString("color_type", "deuteranopia");
        float strength = prefs.getFloat("strength", 0.7f);

        float[] blended = blendWithIdentity(getMatrix(type), strength);
        ColorMatrix cm = new ColorMatrix(blended);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        overlayLayout.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, paint);
        overlayLayout.invalidate();
    }

    private float[] getMatrix(String type) {
        switch (type) {
            case "protanopia":   return MATRIX_PROTANOPIA;
            case "tritanopia":   return MATRIX_TRITANOPIA;
            case "monochromacy": return MATRIX_MONOCHROMACY;
            default:             return MATRIX_DEUTERANOPIA;
        }
    }

    private float[] blendWithIdentity(float[] matrix, float strength) {
        float[] id = {1,0,0,0,0, 0,1,0,0,0, 0,0,1,0,0, 0,0,0,1,0};
        float[] r = new float[20];
        for (int i = 0; i < 20; i++) r[i] = id[i] + (matrix[i] - id[i]) * strength;
        return r;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent e) {}
    @Override public void onInterrupt() { removeOverlay(); }
    @Override public void onDestroy() { instance = null; removeOverlay(); super.onDestroy(); }
}
