# نکسورا پلاس (Nexora Plus) — v2.5

اپلیکیشن جامع برنامه‌ریزی شخصی/تحصیلی/حرفه‌ای — کاملاً آفلاین، فارسی و RTL، چند‌حسابی.

**توسعه‌دهنده:** MAHIREX · **تکنولوژی:** Kotlin · Jetpack Compose · Material 3 · Room · Clean Architecture (ViewModel + Repository)

---

## ✨ قابلیت‌ها

| بخش | توضیح |
|---|---|
| 🏠 خانه | هدر گرادیانی با سلام ساعت‌محور، تاریخ کامل، فال حافظ و جمله انگیزشی (هماهنگ با صفحه فال) |
| 📅 امروز در تقویم | ~۸۵ مناسبت مذهبی/ملی/بین‌المللی آفلاین + بروزرسانی آنلاین برای همه حساب‌ها |
| 🗓 برنامه هفتگی | جدول رنگی اکسل‌مانند؛ بازه‌های پیش‌فرض و کاربر کاملاً قابل ویرایش/حذف/جابه‌جایی + ذخیره چیدمان به‌عنوان پیش‌فرض |
| ✅ وظایف/اهداف/عادت‌ها | اولویت، سررسید، سنجاق (Pin)، streak، نقطه‌های ۷ روزه |
| 📝 لیست‌ها | چک‌لیست با آیتم، رنگ و ایموجی + شورتکات سریع در خانه |
| 🔮 فال روزانه | ۱۶ فال حافظ با تفسیر + حالت روز (عالی/خوب/متوسط/احتیاط) + راهنمای ۵ حوزه + تاریخچه |
| ⏱ تمرکز عمیق | تایمر حلقه‌ای ۱۵–۹۰ دقیقه + سکوت هوشمند اعلان‌ها (DND) |
| 👥 چندحسابی | تا ۳ حساب با سوئیچر لوکس، شخصی‌سازی (نام/آواتار/جنسیت/رنگ) و حذف امن |
| 💾 پشتیبان‌گیری | خروجی/بازیابی JSON (حساب فعال یا همه) |
| 🌍 زبان | ۱۶ زبان با نام بومی + «زبان پیش‌فرض دستگاه»، RTL/LTR خودکار |
| 📆 تقویم | شمسی/میلادی کاملاً مستقل از زبان؛ DatePicker با پرش سال و ماه |
| 🕐 ساعت | قالب ۲۴/۱۲ ساعته؛ TimePicker دایره‌ای با تایپ مستقیم |
| 🔍 جست‌وجو | سراسری در رویداد/وظیفه/هدف/عادت/یادداشت/لیست |
| 📊 آمار | نرخ انجام، روند ۷ روز، مجموع تمرکز، بهترین عادت |
| 🧩 ویجت | برنامه امروز + فال روزانه روی صفحه اصلی |

## 🏗 ساخت پروژه

نیازمندی: JDK 17، Android Studio Ladybug+ (یا Gradle 8.9+).

```bash
# در Android Studio: Open → پوشه پروژه → Sync → Run
# یا خط فرمان:
gradle assembleDebug          # APK تست
gradle assembleRelease        # APK انتشار (نیاز به keystore)
gradle bundleRelease          # AAB برای کافه‌بازار/گوگل‌پلی
```

### امضای Release
سه راه:
1. **GitHub Actions (پیشنهادی):** در Settings → Secrets چهار مقدار را بسازید:
   - `NEXORA_KEYSTORE_BASE64` (خروجی `base64 -w0 nexora-release.jks`)
   - `NEXORA_KEYSTORE_PASSWORD`
   - `NEXORA_KEY_ALIAS`
   - `NEXORA_KEY_PASSWORD`
   سپس هر push، خودکار **APK + AAB** به‌عنوان Artifact می‌سازد (`.github/workflows/android.yml`).
2. فایل `keystore/nexora-release.jks` کنار `app/` بگذارید و متغیرهای محیطی NEXORA_* را ست کنید.
3. Android Studio → Build → Generate Signed Bundle/APK.

ساخت keystore جدید:
```bash
keytool -genkeypair -v -keystore nexora-release.jks -alias nexora \
  -keyalg RSA -keysize 2048 -validity 10000
```

### انتشار در کافه‌بازار
- فایل `app-release.aab` یا `app-release.apk` امضاشده را آپلود کنید.
- `applicationId = com.mahirex.nexoraplus`، `versionCode 25`، `versionName 2.5.0`.
- آیکون adaptive با پالت سازمانی از قبل تنظیم شده است.

## 🧱 معماری

```
app/src/main/java/com/mahirex/nexoraplus/
├── core/            # موتور تقویم (جلالی/قمری/میلادی) + ابزارها
├── data/
│   ├── local/       # Room: 16 Entity + 11 DAO + Database
│   ├── repo/        # Repositoryها + Settings(DataStore) + BackupManager
│   ├── fortune/     # موتور فال (حافظ + انگیزشی + راهنما)
│   └── occasions/   # دیتاست مناسبت‌ها + همگام‌ساز آنلاین
├── di/              # AppContainer (DI سبک و بدون کدجنریشن)
├── ui/
│   ├── theme/       # Material3 + انیمیشن 350ms روشن/تاریک
│   ├── nav/         # NavHost + BottomBar
│   ├── pickers/     # DatePicker شمسی/میلادی + TimePicker دایره‌ای
│   ├── components/  # اجزای مشترک
│   └── screens/     # 15 صفحه با ViewModel مستقل
├── widget/          # ویجت امروز + فال
└── worker/          # یادآور شبانه (WorkManager)
```

- **100% آفلاین**؛ تنها «بروزرسانی تقویم مناسبت‌ها» اختیاری و آنلاین است.
- داده‌ها با Room روی دستگاه ذخیره می‌شوند؛ حذف‌ها همیشه با دیالوگ تأیید.
- RTL کامل با `TextDirection.Content` و RLM برای متن‌های ترکیبی فارسی/انگلیسی.

## 📄 لایسنس

MIT — با افتخار برای ایرانیان تلاشگر 🌱
