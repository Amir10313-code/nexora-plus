package com.mahirex.nexoraplus.data.occasions

import com.mahirex.nexoraplus.data.local.entity.OccasionEntity

/**
 * «امروز در تقویم» — the bundled occasions database.
 * calType: 0 = lunar (Hijri) religious occasions, 1 = solar (Jalali) national ones.
 * kind: 0 = religious, 1 = national, 2 = international/cultural.
 *
 * This dataset works 100% offline. The Settings screen offers
 * «بروزرسانی تقویم مناسبت‌ها» which refreshes it for ALL accounts.
 */
object DefaultOccasions {

    const val VERSION = 1

    private fun entity(
        calType: Int, month: Int, day: Int, kind: Int,
        title: String, description: String, isHoliday: Boolean,
        emoji: String, colorHex: String
    ) = OccasionEntity(
        calType = calType, month = month, day = day, kind = kind,
        title = title, description = description, isHoliday = isHoliday,
        emoji = emoji, colorHex = colorHex, version = VERSION
    )

    private const val LUNAR = 0
    private const val SOLAR = 1
    private const val RELIGIOUS = 0
    private const val NATIONAL = 1
    private const val INTERNATIONAL = 2
    private const val PURPLE = "#5B3FE0"
    private const val EMERALD = "#059669"
    private const val CYAN = "#0891B2"
    private const val SLATE = "#475569"

    val all: List<OccasionEntity> by lazy { build() }

    private fun build(): List<OccasionEntity> {
        val list = ArrayList<OccasionEntity>(100)

        // ─────────────── مناسبت‌های مذهبی (مبنی بر تقویم قمری) ───────────────
        // محرم
        list += entity(LUNAR, 1, 9, RELIGIOUS, "تاسوعای حسینی", "نهم محرم، روز اباعبدالله الحسین (ع) و یادآور وفا و ایثار برادرش حضرت عباس (ع).", true, "🏴", SLATE)
        list += entity(LUNAR, 1, 10, RELIGIOUS, "عاشورای حسینی", "دهم محرم، سالروز شهادت امام حسین (ع) و یاران باوفایش در کربلا؛ روز عزای عمومی.", true, "🏴", SLATE)
        list += entity(LUNAR, 1, 12, RELIGIOUS, "شهادت امام سجاد (ع)", "یادبود امام زین‌العابدین (ع)، زینت ساجدین و گوینده‌ی دعاهای عاشورا.", false, "🖤", PURPLE)
        // صفر
        list += entity(LUNAR, 2, 20, RELIGIOUS, "اربعین حسینی", "چهلم شهادت امام حسین (ع) و کاروان اسرا؛ پیاده‌روی بزرگ اربعین نماد وفاداری است.", true, "🚶", SLATE)
        list += entity(LUNAR, 2, 28, RELIGIOUS, "رحلت پیامبر (ص) و شهادت امام حسن (ع)", "سالروز درگذشت حضرت محمد (ص) و شهادت امام حسن مجتبی (ع).", true, "🕊", SLATE)
        list += entity(LUNAR, 2, 30, RELIGIOUS, "شهادت امام رضا (ع)", "آخر صفر، سالروز شهادت امام رضا (ع)، امام هشتم شیعیان در خراسان.", true, "🕌", SLATE)
        // ربیع‌الاول
        list += entity(LUNAR, 3, 8, RELIGIOUS, "شهادت امام حسن عسکری (ع)", "یادبود امام یازدهم شیعیان؛ آغاز دوران غیبت امام زمان (عج).", false, "🖤", PURPLE)
        list += entity(LUNAR, 3, 17, RELIGIOUS, "میلاد پیامبر اکرم (ص) و امام صادق (ع)", "هفدهم ربیع‌الاول، جشن میلاد حضرت محمد (ص) و امام جعفر صادق (ع)؛ هفته وحدت.", true, "🎉", PURPLE)
        // ربیع‌الثانی
        list += entity(LUNAR, 4, 10, RELIGIOUS, "شهادت حضرت فاطمه معصومه (س)", "یادبود کریمه اهل‌بیت (س)، خواهر امام رضا (ع).", false, "🕌", PURPLE)
        // جمادی‌الثانی
        list += entity(LUNAR, 6, 3, RELIGIOUS, "شهادت حضرت فاطمه زهرا (س)", "سالروز شهادت صدیقه کبری (س)، دختر پیامبر (ص) و الگوی ایثار.", true, "🖤", SLATE)
        list += entity(LUNAR, 6, 20, RELIGIOUS, "میلاد حضرت فاطمه زهرا (س)", "جشن میلاد حضرت زهرا (س)؛ روز زن و روز مادر در ایران.", true, "🌸", PURPLE)
        // رجب
        list += entity(LUNAR, 7, 1, RELIGIOUS, "ولادت امام محمد باقر (ع)", "میلاد امام پنجم شیعیان، شکافنده‌ی علوم.", false, "🌙", PURPLE)
        list += entity(LUNAR, 7, 6, RELIGIOUS, "لیلةالرغائب", "شب آرزوها؛ جمعه‌ی اول ماه رجب، از شب‌های پربرکت و دعا.", false, "🌙", PURPLE)
        list += entity(LUNAR, 7, 10, RELIGIOUS, "ولادت امام محمد تقی (جواد) (ع)", "میلاد امام نهم شیعیان، جواد الائمه (ع).", false, "🌙", PURPLE)
        list += entity(LUNAR, 7, 13, RELIGIOUS, "میلاد امام علی (ع)", "سیزدهم رجب، جشن میلاد مولود کعبه (ع)؛ روز پدر در ایران.", true, "👨", PURPLE)
        list += entity(LUNAR, 7, 15, RELIGIOUS, "وفات حضرت زینب (س)", "یادبود حضرت زینب کبری (س)، قهرمان واقعه‌ی کربلا و پیام‌آور عاشورا.", false, "🖤", PURPLE)
        list += entity(LUNAR, 7, 25, RELIGIOUS, "شهادت امام موسی کاظم (ع)", "یادبود امام هفتم شیعیان، باب‌الحوائج (ع).", true, "🖤", SLATE)
        list += entity(LUNAR, 7, 27, RELIGIOUS, "مبعث رسول اکرم (ص)", "سالروز مبعث حضرت محمد (ص)؛ آغاز رسالت پیامبر مهربانی.", true, "🌟", PURPLE)
        // شعبان
        list += entity(LUNAR, 8, 3, RELIGIOUS, "ولادت امام حسین (ع)", "جشن میلاد اباعبدالله الحسین (ع)، سید و سالار شهیدان.", true, "🎉", PURPLE)
        list += entity(LUNAR, 8, 4, RELIGIOUS, "ولادت حضرت ابوالفضل العباس (ع)", "میلاد قمر بنی‌هاشم (ع)، نماد وفا و ایثار.", false, "🎉", PURPLE)
        list += entity(LUNAR, 8, 5, RELIGIOUS, "ولادت امام سجاد (ع)", "میلاد امام زین‌العابدین (ع)، عابد اهل‌بیت.", false, "🌙", PURPLE)
        list += entity(LUNAR, 8, 15, RELIGIOUS, "نیمه شعبان؛ میلاد امام زمان (عج)", "میلاد مهدی موعود (عج)، امید آخرین روزهای زمین؛ شادترین شب سال شیعیان.", true, "🎉", PURPLE)
        // رمضان
        list += entity(LUNAR, 9, 1, RELIGIOUS, "آغاز ماه مبارک رمضان", "ماه میهمانی خدا؛ ماه روزه، قرآن و بندگی.", false, "🌙", PURPLE)
        list += entity(LUNAR, 9, 10, RELIGIOUS, "وفات حضرت خدیجه (س)", "یادبود ام‌المومنین خدیجه (س)، اولین بانوی اسلام و یاور پیامبر (ص).", false, "🖤", PURPLE)
        list += entity(LUNAR, 9, 15, RELIGIOUS, "ولادت امام حسن مجتبی (ع)", "میلاد امام دوم شیعیان، کریم اهل‌بیت (ع).", false, "🎉", PURPLE)
        list += entity(LUNAR, 9, 19, RELIGIOUS, "شب قدر (نوزدهم)", "اولین شب‌های قدر؛ شب نزول قرآن و اضطراب دعا.", false, "🌙", PURPLE)
        list += entity(LUNAR, 9, 21, RELIGIOUS, "شهادت امام علی (ع)", "شب بیست‌ویکم رمضان، سالروز ضربت‌خوردن و شهادت امیرالمومنین (ع).", true, "🕌", SLATE)
        list += entity(LUNAR, 9, 23, RELIGIOUS, "شب قدر (بیست‌وسوم)", "لیلةالقدر؛ شب تقدیر و بهترین شب دعا و احیا.", false, "🌙", PURPLE)
        // شوال
        list += entity(LUNAR, 10, 1, RELIGIOUS, "عید سعید فطر", "عید بندگی پس از یک ماه عبادت؛ عید شکستن روزه.", true, "🎉", PURPLE)
        list += entity(LUNAR, 10, 2, RELIGIOUS, "تعطیل عید سعید فطر", "روز دوم جشن و شادمانی عید فطر.", true, "🎉", PURPLE)
        list += entity(LUNAR, 10, 25, RELIGIOUS, "شهادت امام جعفر صادق (ع)", "یادبود امام ششم شیعیان، بنیان‌گذار مذهب جعفری (ع).", true, "🖤", SLATE)
        // ذی‌القعده
        list += entity(LUNAR, 11, 1, RELIGIOUS, "ولادت حضرت فاطمه معصومه (س)", "میلاد کریمه اهل‌بیت (س)؛ روز دختران در ایران.", false, "🌸", PURPLE)
        list += entity(LUNAR, 11, 11, RELIGIOUS, "ولادت امام رضا (ع)", "میلاد امام هشتم شیعیان، امام مهربان خراسان.", false, "🎉", PURPLE)
        // ذی‌الحجه
        list += entity(LUNAR, 12, 9, RELIGIOUS, "روز عرفه", "روز دعا و مناجات عاشقان در عرفات؛ شبیه‌ترین روز به عید برای دعاکنندگان.", false, "🤲", PURPLE)
        list += entity(LUNAR, 12, 10, RELIGIOUS, "عید سعید قربان", "عید قربانی؛ یادآور ایثار ابراهیم و اسماعیل (ع).", true, "🐑", PURPLE)
        list += entity(LUNAR, 12, 15, RELIGIOUS, "ولادت امام هادی (ع)", "میلاد امام دهم شیعیان، علی النقی (ع).", false, "🌙", PURPLE)
        list += entity(LUNAR, 12, 18, RELIGIOUS, "عید سعید غدیر خم", "بزرگ‌ترین عید شیعه؛ سالروز نصب امام علی (ع) به ولایت توسط پیامبر (ص).", true, "🎉", PURPLE)
        list += entity(LUNAR, 12, 24, RELIGIOUS, "روز مباهله", "سالروز ماجرای مباهله پیامبر (ص) با مسیحیان نجران.", false, "🕌", PURPLE)

        // ─────────────── مناسبت‌های ملی و رسمی ایران (شمسی) ───────────────
        list += entity(SOLAR, 1, 1, NATIONAL, "جشن نوروز", "آغاز سال نو و جشن بزرگ تحول طبیعت؛ سال نو مبارک!", true, "🌱", EMERALD)
        list += entity(SOLAR, 1, 2, NATIONAL, "عید نوروز", "روز دوم نوروز؛ روز مهمانی و شادی خانوادگی.", true, "🌱", EMERALD)
        list += entity(SOLAR, 1, 3, NATIONAL, "عید نوروز", "سومین روز نوروز؛ روز سفر و دید و بازدید.", true, "🌱", EMERALD)
        list += entity(SOLAR, 1, 4, NATIONAL, "عید نوروز", "چهارمین روز نوروز؛ روز وصال خویشان و لبخندهای تازه.", true, "🌱", EMERALD)
        list += entity(SOLAR, 1, 12, NATIONAL, "روز جمهوری اسلامی ایران", "سالروز همه‌پرسی ۱۲ فروردین ۱۳۵۸ و آغاز رسمی نظام جمهوری اسلامی.", true, "🇮🇷", EMERALD)
        list += entity(SOLAR, 1, 13, NATIONAL, "روز طبیعت (سیزده‌بدر)", "پایان جشن‌های نوروز؛ روز سنت‌شکنی طبیعت و سبزی و نشاط.", true, "🌳", EMERALD)
        list += entity(SOLAR, 1, 21, INTERNATIONAL, "روز جهانی نوروز", "نوروز جهانی؛ روز ثبت‌شده‌ی نوروز در فهرست میراث معنوی یونسکو.", false, "🌍", CYAN)
        list += entity(SOLAR, 1, 18, INTERNATIONAL, "روز جهانی بهداشت", "سالروز تأسیس سازمان جهانی بهداشت؛ سلامت برای همه.", false, "⚕️", CYAN)
        list += entity(SOLAR, 1, 25, NATIONAL, "روز بزرگداشت عطار نیشابوری", "یادبود عطار، پیر عرفان و شعر حماسی ایران.", false, "📜", EMERALD)
        list += entity(SOLAR, 2, 2, INTERNATIONAL, "روز زمین پاک", "یادآوری مسئولیت همه‌ی ما در نگهداری از کره‌ی زمین.", false, "🌍", CYAN)
        list += entity(SOLAR, 2, 10, NATIONAL, "روز ملی خلیج فارس", "سالروز اخراج پرتغالی‌ها از تنگه هرمز به فرماندهی رئیس‌علی دلواری.", false, "🌊", EMERALD)
        list += entity(SOLAR, 2, 11, NATIONAL, "روز جهانی کارگر", "روز کارگر و بزرگداشت تلاش دست‌های سازنده.", true, "🛠", EMERALD)
        list += entity(SOLAR, 2, 12, NATIONAL, "روز معلم", "بزرگداشت شهید استاد مرتضی مطهری و گرامیداشت جایگاه معلم.", false, "📚", EMERALD)
        list += entity(SOLAR, 2, 25, NATIONAL, "روز بزرگداشت فردوسی", "پاسداشت زبان فارسی و بزرگداشت حکیم ابوالقاسم فردوسی.", false, "📜", EMERALD)
        list += entity(SOLAR, 3, 10, INTERNATIONAL, "روز جهانی بدون دخانیت", "یادآوری اهمیت هوای پاک و سلامت ریه‌ها.", false, "🚭", CYAN)
        list += entity(SOLAR, 3, 14, NATIONAL, "رحلت امام خمینی (ره)", "سالروز درگذشت بنیان‌گذار جمهوری اسلامی ایران.", true, "🕊", SLATE)
        list += entity(SOLAR, 3, 15, NATIONAL, "قیام ۱۵ خرداد", "سالروز قیام مردم ایران در ۱۵ خرداد ۱۳۴۲.", false, "🇮🇷", EMERALD)
        list += entity(SOLAR, 3, 15, INTERNATIONAL, "روز جهانی محیط‌زیست", "برنامه‌ریزی برای زمینِ پاک‌تر از همین امروز.", false, "🌍", CYAN)
        list += entity(SOLAR, 6, 1, NATIONAL, "روز پزشک", "بزرگداشت ابوعلی سینا و تقدیر از پزشکان ایران.", false, "⚕️", EMERALD)
        list += entity(SOLAR, 6, 5, NATIONAL, "روز داروسازی", "بزرگداشت محمد بن زکریای رازی، کاشف الکل و بزرگ داروساز.", false, "💊", EMERALD)
        list += entity(SOLAR, 6, 13, NATIONAL, "روز تعاون", "روز همکاری و کار دسته‌جمعی در اقتصاد ایران.", false, "🤝", EMERALD)
        list += entity(SOLAR, 6, 27, NATIONAL, "روز شعر و ادب فارسی", "بزرگداشت استاد شهریار، شاعر حیدر بابا و حیدر بابای دل‌ها.", false, "🖋", EMERALD)
        list += entity(SOLAR, 6, 30, INTERNATIONAL, "روز جهانی صلح", "امروز فرصتی برای قدم‌های کوچک صلح از خودمان.", false, "🕊", CYAN)
        list += entity(SOLAR, 6, 31, NATIONAL, "آغاز هفته دفاع مقدس", "یادآوری آغاز جنگ تحمیلی و بزرگداشت رزمندگان دفاع مقدس.", false, "🎖", EMERALD)
        list += entity(SOLAR, 7, 8, NATIONAL, "روز بزرگداشت مولوی", "یادبود مولانا جلال‌الدین بلخی، پیر آواز دف‌ها و دوستی.", false, "📖", EMERALD)
        list += entity(SOLAR, 7, 13, NATIONAL, "روز دانش‌آموز", "سالروز استقبال از امام خمینی (ره) در مهر ۱۳۵۷ و روز دانش‌آموز.", false, "🎒", EMERALD)
        list += entity(SOLAR, 7, 20, NATIONAL, "روز بزرگداشت حافظ", "گرامیداشت خواجه شیراز، لسان‌الغیب؛ شب‌شعر و فال حافظ.", false, "📖", EMERALD)
        list += entity(SOLAR, 7, 24, INTERNATIONAL, "روز جهانی غذا", "سازمان جهانی خواربار (فائو)؛ یادآوری اهمیت امنیت غذایی.", false, "🌾", CYAN)
        list += entity(SOLAR, 8, 13, NATIONAL, "روز دانشجو", "سالروز ۱۳ آبان ۱۳۳۲ و مبارزات دانشجویی؛ روز دانشجو.", false, "🎓", EMERALD)
        list += entity(SOLAR, 8, 23, INTERNATIONAL, "روز جهانی دیابت", "آگاهی‌بخشی درباره‌ی سبک زندگی سالم و پیشگیری.", false, "🩺", CYAN)
        list += entity(SOLAR, 8, 24, NATIONAL, "روز کتاب و کتابخوانی", "گرامیداشت روز دفاع مقدس و تشویق کتاب و کتابخوانی.", false, "📚", EMERALD)
        list += entity(SOLAR, 8, 29, INTERNATIONAL, "روز جهانی کودک", "یادآوری حقوق کودکان؛ کودکانه بودن حق همه‌ی ماست.", false, "🧒", CYAN)
        list += entity(SOLAR, 9, 10, INTERNATIONAL, "روز جهانی مبارزه با ایدز", "آگاهی، همراهی و مهرورزی؛ نه طرد.", false, "🎗", CYAN)
        list += entity(SOLAR, 9, 22, NATIONAL, "روز پدافند غیرعامل", "یادآوری اهمیت آمادگی و مقاوم‌سازی در برابر تهدیدها.", false, "🛡", EMERALD)
        list += entity(SOLAR, 9, 26, NATIONAL, "روز ملی حمل و نقل و رانندگان", "تقدیر از رانندگان؛ دست‌هایی که راه‌های ایران را به هم می‌رسانند.", false, "🚌", EMERALD)
        list += entity(SOLAR, 9, 30, NATIONAL, "شب یلدا (چله)", "درازترین شب سال؛ میوه، خنده‌های بزرگترها و شاهنامه‌خوانی؛ ثبت جهانی یونسکو.", false, "🍉", EMERALD)
        list += entity(SOLAR, 10, 6, INTERNATIONAL, "روز جهانی زبان فارسی", "به مناسبت بزرگداشت سعدی شیرازی در تقویم یونسکو.", false, "📜", CYAN)
        list += entity(SOLAR, 10, 9, NATIONAL, "روز بصیرت و میثاق امت با ولایت", "سالروز حماسه‌ی ۹ دی ۱۳۸۸.", false, "🇮🇷", EMERALD)
        list += entity(SOLAR, 10, 24, NATIONAL, "ملی شدن صنعت نفت", "سالروز ملی شدن صنعت نفت ایران در ۲۴ دی ۱۳۲۹.", false, "🛢", EMERALD)
        list += entity(SOLAR, 11, 19, NATIONAL, "روز نیروی هوایی", "تقدیر از خلبانان و کارکنان نیروی هوایی.", false, "✈️", EMERALD)
        list += entity(SOLAR, 11, 22, NATIONAL, "پیروزی انقلاب اسلامی", "سالروز ۲۲ بهمن ۱۳۵۷؛ پیروزی انقلاب اسلامی ایران.", true, "🇮🇷", EMERALD)
        list += entity(SOLAR, 11, 24, INTERNATIONAL, "روز جهانی رادیو", "جشن صدای همه‌ی جهان در قاب امواج رادیو.", false, "📻", CYAN)
        list += entity(SOLAR, 12, 5, NATIONAL, "روز مهندسی", "بزرگداشت خواجه نصیرالدین طوسی و تقدیر از مهندسان.", false, "📐", EMERALD)
        list += entity(SOLAR, 12, 14, NATIONAL, "روز احسان و نیکوکاری", "روز خوبی‌ها؛ یک قدم کوچک برای دیگران بگذار.", false, "🤲", EMERALD)
        list += entity(SOLAR, 12, 15, NATIONAL, "روز درختکاری", "روز آشتی با طبیعت و کاشتن امیدهای سبز.", false, "🌳", EMERALD)
        list += entity(SOLAR, 12, 17, INTERNATIONAL, "روز جهانی زن", "سالروز مبارزات زنان برای برابری؛ تکریم حضور و تلاش زنان.", false, "🌷", CYAN)
        list += entity(SOLAR, 12, 25, NATIONAL, "روز ملی صنعت", "بزرگداشت زحمتکشان صنعت ایران.", false, "🏭", EMERALD)
        list += entity(SOLAR, 12, 29, NATIONAL, "ملی شدن صنعت نفت", "سالروز ملی شدن صنعت نفت (۲۹ اسفند)؛ تعطیل رسمی.", true, "🛢", EMERALD)

        return list
    }
}
