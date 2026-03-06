package com.renlihayat.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.provider.Settings;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;
import java.util.List;

public class MainActivity extends Activity {

    private SharedPreferences prefs;
    private TextView tvStatus;
    private Button btnToggle;
    private RadioGroup rgType;
    private SeekBar sbStrength;
    private TextView tvStrength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("renkli_hayat", MODE_PRIVATE);
        tvStatus   = findViewById(R.id.tv_status);
        btnToggle  = findViewById(R.id.btn_toggle);
        rgType     = findViewById(R.id.rg_type);
        sbStrength = findViewById(R.id.sb_strength);
        tvStrength = findViewById(R.id.tv_strength);

        loadSavedSettings();

        findViewById(R.id.btn_accessibility).setOnClickListener(v ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        );

        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            prefs.edit().putString("color_type", getTypeFromCheckedId(checkedId)).apply();
            applyIfActive();
        });

        sbStrength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvStrength.setText(progress + "%");
                prefs.edit().putFloat("strength", progress / 100f).apply();
                applyIfActive();
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        btnToggle.setOnClickListener(v -> toggleFilter());
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void toggleFilter() {
        if (!isAccessibilityServiceEnabled()) {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }
        boolean newState = !prefs.getBoolean("filter_active", false);
        prefs.edit().putBoolean("filter_active", newState).apply();
        ColorCorrectionService service = ColorCorrectionService.getInstance();
        if (service != null) {
            if (newState) service.addOverlay();
            else service.removeOverlay();
        }
        updateUI();
    }

    private void applyIfActive() {
        ColorCorrectionService service = ColorCorrectionService.getInstance();
        if (service != null && prefs.getBoolean("filter_active", false)) {
            service.applyCurrentSettings();
        }
    }

    private void updateUI() {
        boolean enabled = isAccessibilityServiceEnabled();
        boolean active  = prefs.getBoolean("filter_active", false) && enabled;
        if (!enabled) {
            tvStatus.setText("⚠️ Erişilebilirlik izni gerekli");
            tvStatus.setTextColor(0xFFF7A26A);
            btnToggle.setText("İzin Ver ve Başlat");
        } else if (active) {
            tvStatus.setText("🟢 Filtre Aktif — Her uygulamada çalışıyor");
            tvStatus.setTextColor(0xFF6AF7A2);
            btnToggle.setText("Filtreyi Kapat");
        } else {
            tvStatus.setText("⭕ Filtre Kapalı");
            tvStatus.setTextColor(0xFFE8E8F0);
            btnToggle.setText("Filtreyi Aç");
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> services =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo s : services) {
            if (s.getId().contains(getPackageName())) return true;
        }
        return false;
    }

    private String getTypeFromCheckedId(int id) {
        if (id == R.id.rb_protanopia)   return "protanopia";
        if (id == R.id.rb_tritanopia)   return "tritanopia";
        if (id == R.id.rb_monochromacy) return "monochromacy";
        return "deuteranopia";
    }

    private void loadSavedSettings() {
        String type = prefs.getString("color_type", "deuteranopia");
        switch (type) {
            case "protanopia":   rgType.check(R.id.rb_protanopia); break;
            case "tritanopia":   rgType.check(R.id.rb_tritanopia); break;
            case "monochromacy": rgType.check(R.id.rb_monochromacy); break;
            default:             rgType.check(R.id.rb_deuteranopia);
        }
        int strength = Math.round(prefs.getFloat("strength", 0.7f) * 100);
        sbStrength.setProgress(strength);
        tvStrength.setText(strength + "%");
    }
}
