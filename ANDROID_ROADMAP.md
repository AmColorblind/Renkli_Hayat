# 📱 Renkli Hayat — Android Native Uygulama Yol Haritası
## Android App Roadmap

---

## 🎯 Hedef / Goal

Sistem geneli renk düzeltme filtresi.
Instagram, oyunlar, haritalar — her uygulama, her ekran.

System-wide color correction filter that works on top of every app.

---

## 🔧 Teknik Yaklaşım / Technical Approach

### Kullanılacak Android API

**`TYPE_ACCESSIBILITY_OVERLAY`** — Android'in resmi erişilebilirlik katmanı.

Bu API:
- Tüm uygulamaların **üstüne** şeffaf bir katman çizer
- Oyunlar dahil her şeyin üstünde çalışır
- Google Play Store'a yüklenebilir (özel izin gerektirmez)
- Android 7.0+ destekler (dünya genelinde cihazların %98'i)

### Renk Düzeltme Algoritması
**LMS Daltonization** — Web versiyonuyla aynı algoritma, native Java/Kotlin'e porte edilecek.

### Performans Hedefi
- 60 FPS'de çalışmalı, pil tüketimi minimum olmalı
- OpenGL ES shader kullanılarak GPU'da işlenecek (CPU yerine)

---

## 📋 Geliştirme Aşamaları / Development Phases

### Faz 1 — Temel Overlay (2-3 hafta)
- [ ] Android Studio projesi kur
- [ ] AccessibilityService oluştur
- [ ] Tüm ekranı kaplayan şeffaf View ekle
- [ ] Basit renk matrisi uygula (ColorFilter)
- [ ] Aç/kapat butonu (bildirim çubuğundan)

### Faz 2 — LMS Algoritması (1-2 hafta)
- [ ] Deuteranopi matrisi
- [ ] Protanopi matrisi  
- [ ] Tritanopi matrisi
- [ ] Akromatopsi matrisi
- [ ] Filtre gücü kaydırıcısı

### Faz 3 — Kullanıcı Arayüzü (1-2 hafta)
- [ ] Kurulum ekranı (renk türü seçimi)
- [ ] Hızlı erişim (bildirim paneli tile)
- [ ] Ayarlar ekranı
- [ ] Türkçe + İngilizce dil desteği

### Faz 4 — Play Store (1 hafta)
- [ ] APK imzalama
- [ ] Store görselleri ve açıklaması
- [ ] Ücretsiz yayın

---

## 🛠️ Geliştirici İçin Başlangıç / For Developers

### Gereksinimler
- Android Studio Hedgehog veya üzeri
- Java 17 veya Kotlin 1.9+
- Android SDK 24+ (minimum), 34 (target)

### Temel Yapı

```xml
<!-- AndroidManifest.xml -->
<service
    android:name=".ColorCorrectionService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

```java
// ColorCorrectionService.java
public class ColorCorrectionService extends AccessibilityService {
    @Override
    protected void onServiceConnected() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // Tam ekran şeffaf overlay
        View overlay = new ColorOverlayView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        );
        wm.addView(overlay, params);
    }
}
```

```java
// ColorOverlayView.java — LMS Daltonization
public class ColorOverlayView extends View {
    private Paint paint = new Paint();
    
    // Deuteranopi düzeltme matrisi
    private float[] DEUTERANOPIA_MATRIX = {
        1.0f,  0.0f, 0.0f, 0, 0,
        0.4f,  0.0f, 0.6f, 0, 0,
        0.0f,  0.0f, 1.0f, 0, 0,
        0,     0,    0,    1, 0
    };
    
    public ColorOverlayView(Context context) {
        super(context);
        ColorMatrix cm = new ColorMatrix(DEUTERANOPIA_MATRIX);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        // Filtre tüm ekrana uygulanır
        canvas.drawPaint(paint);
    }
}
```

---

## 🤝 Katkı Sağlamak İsteyenler / How to Contribute

1. Bu repoyu fork et
2. Yukarıdaki teknik yapıyı temel al
3. Geliştirmeni yap
4. Pull request gönder

Her seviyeden geliştirici katkı sağlayabilir.
Tasarımcılar, çevirmenler, test kullanıcıları da bekliyoruz.

---

## 📞 İletişim / Contact

GitHub Issues üzerinden her türlü soru ve öneri için ulaşabilirsiniz.

---

*Bu proje GPL v3 lisansı ile korunmaktadır. Kimse sahiplenemez, kimse satamazam. İnsanlığa aittir.*

*This project is protected under GPL v3. It belongs to humanity.*
