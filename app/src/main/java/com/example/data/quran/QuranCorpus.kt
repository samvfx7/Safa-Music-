package com.example.data.quran

data class SurahInfo(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameTransliteration: String,
    val totalAyahs: Int,
    val searchKeys: List<String>,
    val verifiedAyahs: List<AyahEntry> = emptyList()
)

data class AyahEntry(
    val ayahNumber: Int,
    val textArabicOriginal: String,
    val textArabicNormalized: String,
    val transliteration: String = ""
)

object QuranCorpus {

    val SURAHS: List<SurahInfo> = listOf(
        SurahInfo(
            number = 1,
            nameArabic = "الفاتحة",
            nameEnglish = "The Opening",
            nameTransliteration = "Al-Fatihah",
            totalAyahs = 7,
            searchKeys = listOf("fatiha", "fatihah", "fateha", "opening", "hamd"),
            verifiedAyahs = listOf(
                AyahEntry(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", ArabicNormalizer.normalize("بسم الله الرحمن الرحيم")),
                AyahEntry(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", ArabicNormalizer.normalize("الحمد لله رب العالمين")),
                AyahEntry(3, "الرَّحْمَٰنِ الرَّحِيمِ", ArabicNormalizer.normalize("الرحمن الرحيم")),
                AyahEntry(4, "مَالِكِ يَوْمِ الدِّينِ", ArabicNormalizer.normalize("مالك يوم الدين")),
                AyahEntry(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", ArabicNormalizer.normalize("إياك نعبد وإياك نستعين")),
                AyahEntry(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", ArabicNormalizer.normalize("اهدنا الصراط المستقيم")),
                AyahEntry(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", ArabicNormalizer.normalize("صراط الذين انعمت عليهم غير المغضوب عليهم ولا الضالين"))
            )
        ),
        SurahInfo(
            number = 2,
            nameArabic = "البقرة",
            nameEnglish = "The Cow",
            nameTransliteration = "Al-Baqarah",
            totalAyahs = 286,
            searchKeys = listOf("baqara", "baqarah", "bakara", "cow", "ayatul kursi", "kursi"),
            verifiedAyahs = listOf(
                AyahEntry(255, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ", ArabicNormalizer.normalize("الله لا اله الا هو الحي القيوم لا تاخذه سنة ولا نوم له ما في السماوات وما في الارض"))
            )
        ),
        SurahInfo(
            number = 18,
            nameArabic = "الكهف",
            nameEnglish = "The Cave",
            nameTransliteration = "Al-Kahf",
            totalAyahs = 110,
            searchKeys = listOf("kahf", "cave", "alkahf")
        ),
        SurahInfo(
            number = 36,
            nameArabic = "يس",
            nameEnglish = "Ya-Sin",
            nameTransliteration = "Ya-Sin",
            totalAyahs = 83,
            searchKeys = listOf("yasin", "yaseen", "ya-sin", "ya seen")
        ),
        SurahInfo(
            number = 55,
            nameArabic = "الرحمن",
            nameEnglish = "The Beneficent",
            nameTransliteration = "Ar-Rahman",
            totalAyahs = 78,
            searchKeys = listOf("rahman", "ar-rahman", "beneficent")
        ),
        SurahInfo(
            number = 67,
            nameArabic = "الملك",
            nameEnglish = "The Sovereignty",
            nameTransliteration = "Al-Mulk",
            totalAyahs = 30,
            searchKeys = listOf("mulk", "al-mulk", "sovereignty", "tabarak")
        ),
        SurahInfo(
            number = 103,
            nameArabic = "العصر",
            nameEnglish = "The Declining Day",
            nameTransliteration = "Al-Asr",
            totalAyahs = 3,
            searchKeys = listOf("asr", "al-asr", "time"),
            verifiedAyahs = listOf(
                AyahEntry(1, "وَالْعَصْرِ", ArabicNormalizer.normalize("والعصر")),
                AyahEntry(2, "إِنَّ الْإِنْسَانَ لَفِي خُسْرٍ", ArabicNormalizer.normalize("ان الانسان لفي خسر")),
                AyahEntry(3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", ArabicNormalizer.normalize("الا الذين امنوا وعملوا الصالحات وتواصوا بالحق وتواصوا بالصبر"))
            )
        ),
        SurahInfo(
            number = 108,
            nameArabic = "الكوثر",
            nameEnglish = "Abundance",
            nameTransliteration = "Al-Kawthar",
            totalAyahs = 3,
            searchKeys = listOf("kawthar", "kauthar", "al-kawthar", "abundance"),
            verifiedAyahs = listOf(
                AyahEntry(1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", ArabicNormalizer.normalize("انا اعطيناك الكوثر")),
                AyahEntry(2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", ArabicNormalizer.normalize("فصل لربك وانحر")),
                AyahEntry(3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", ArabicNormalizer.normalize("ان شانئك هو الابتر"))
            )
        ),
        SurahInfo(
            number = 112,
            nameArabic = "الإخلاص",
            nameEnglish = "Purity / Sincerity",
            nameTransliteration = "Al-Ikhlas",
            totalAyahs = 4,
            searchKeys = listOf("ikhlas", "ikhlaas", "al-ikhlas", "sincerity", "purity", "tauhid", "tawhid", "qul huwa allahu ahad", "qul huwallahu ahad"),
            verifiedAyahs = listOf(
                AyahEntry(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", ArabicNormalizer.normalize("قل هو الله احد"), "Qul huwa Allahu ahad"),
                AyahEntry(2, "اللَّهُ الصَّمَدُ", ArabicNormalizer.normalize("الله الصمد"), "Allahu as-Samad"),
                AyahEntry(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", ArabicNormalizer.normalize("لم يلد ولم يولد"), "Lam yalid wa lam yoolad"),
                AyahEntry(4, "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", ArabicNormalizer.normalize("ولم يكن له كفوا احد"), "Wa lam yakun lahu kufuwan ahad")
            )
        ),
        SurahInfo(
            number = 113,
            nameArabic = "الفلق",
            nameEnglish = "The Daybreak",
            nameTransliteration = "Al-Falaq",
            totalAyahs = 5,
            searchKeys = listOf("falaq", "al-falaq", "daybreak", "dawn"),
            verifiedAyahs = listOf(
                AyahEntry(1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", ArabicNormalizer.normalize("قل اعوذ برب الفلق")),
                AyahEntry(2, "مِنْ شَرِّ مَا خَلَقَ", ArabicNormalizer.normalize("من شر ما خلق")),
                AyahEntry(3, "وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ", ArabicNormalizer.normalize("ومن شر غاسق اذا وقب")),
                AyahEntry(4, "وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", ArabicNormalizer.normalize("ومن شر النفاثات في العقد")),
                AyahEntry(5, "وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ", ArabicNormalizer.normalize("ومن شر حاسد اذا حسد"))
            )
        ),
        SurahInfo(
            number = 114,
            nameArabic = "الناس",
            nameEnglish = "Mankind",
            nameTransliteration = "An-Nas",
            totalAyahs = 6,
            searchKeys = listOf("nas", "an-nas", "mankind", "people"),
            verifiedAyahs = listOf(
                AyahEntry(1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", ArabicNormalizer.normalize("قل اعوذ برب الناس")),
                AyahEntry(2, "مَلِكِ النَّاسِ", ArabicNormalizer.normalize("ملك الناس")),
                AyahEntry(3, "إِلَٰهِ النَّاسِ", ArabicNormalizer.normalize("اله الناس")),
                AyahEntry(4, "مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", ArabicNormalizer.normalize("من شر الوسواس الخناس")),
                AyahEntry(5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", ArabicNormalizer.normalize("الذي يوسوس في صدور الناس")),
                AyahEntry(6, "مِنَ الْجِنَّةِ وَالنَّاسِ", ArabicNormalizer.normalize("من الجنة والناس"))
            )
        )
    )

    // Complete index of all 114 Surahs by number for quick lookup
    val ALL_SURAH_NAMES: Map<Int, Pair<String, String>> = mapOf(
        1 to ("الفاتحة" to "Al-Fatihah"), 2 to ("البقرة" to "Al-Baqarah"), 3 to ("آل عمران" to "Ali 'Imran"),
        4 to ("النساء" to "An-Nisa"), 5 to ("المائدة" to "Al-Ma'idah"), 6 to ("الأنعام" to "Al-An'am"),
        7 to ("الأعراف" to "Al-A'raf"), 8 to ("الأنفال" to "Al-Anfal"), 9 to ("التوبة" to "At-Tawbah"),
        10 to ("يونس" to "Yunus"), 11 to ("هود" to "Hud"), 12 to ("يوسف" to "Yusuf"),
        13 to ("الرعد" to "Ar-Ra'd"), 14 to ("إبراهيم" to "Ibrahim"), 15 to ("الحجر" to "Al-Hijr"),
        16 to ("النحل" to "An-Nahl"), 17 to ("الإسراء" to "Al-Isra"), 18 to ("الكهف" to "Al-Kahf"),
        19 to ("مريم" to "Maryam"), 20 to ("طه" to "Ta-Ha"), 21 to ("الأنبياء" to "Al-Anbiya"),
        22 to ("الحج" to "Al-Hajj"), 23 to ("المؤمنون" to "Al-Mu'minun"), 24 to ("النور" to "An-Nur"),
        25 to ("الفرقان" to "Al-Furqan"), 26 to ("الشعراء" to "Ash-Shu'ara"), 27 to ("النمل" to "An-Naml"),
        28 to ("القصص" to "Al-Qasas"), 29 to ("العنكبوت" to "Al-'Ankabut"), 30 to ("الروم" to "Ar-Rum"),
        31 to ("لقمان" to "Luqman"), 32 to ("السجدة" to "As-Sajdah"), 33 to ("الأحزاب" to "Al-Ahzab"),
        34 to ("سبأ" to "Saba"), 35 to ("فاطر" to "Fatir"), 36 to ("يس" to "Ya-Sin"),
        37 to ("الصافات" to "As-Saffat"), 38 to ("ص" to "Sad"), 39 to ("الزمر" to "Az-Zumar"),
        40 to ("غافر" to "Ghafir"), 41 to ("فصلت" to "Fussilat"), 42 to ("الشورى" to "Ash-Shura"),
        43 to ("الزخرف" to "Az-Zukhruf"), 44 to ("الدخان" to "Ad-Dukhan"), 45 to ("الجاثية" to "Al-Jathiyah"),
        46 to ("الأحقاف" to "Al-Ahqaf"), 47 to ("محمد" to "Muhammad"), 48 to ("الفتح" to "Al-Fath"),
        49 to ("الحجرات" to "Al-Hujurat"), 50 to ("ق" to "Qaf"), 51 to ("الذاريات" to "Adh-Dhariyat"),
        52 to ("الطور" to "At-Tur"), 53 to ("النجم" to "An-Najm"), 54 to ("القمر" to "Al-Qamar"),
        55 to ("الرحمن" to "Ar-Rahman"), 56 to ("الواقعة" to "Al-Waqi'ah"), 57 to ("الحديد" to "Al-Hadid"),
        58 to ("المجادلة" to "Al-Mujadila"), 59 to ("الحشر" to "Al-Hashr"), 60 to ("الممتحنة" to "Al-Mumtahanah"),
        61 to ("الصف" to "As-Saff"), 62 to ("الجمعة" to "Al-Jumu'ah"), 63 to ("المنافقون" to "Al-Munafiqun"),
        64 to ("التغابن" to "At-Taghabun"), 65 to ("الطلاق" to "At-Talaq"), 66 to ("التحريم" to "At-Tahrim"),
        67 to ("الملك" to "Al-Mulk"), 68 to ("القلم" to "Al-Qalam"), 69 to ("الحاقة" to "Al-Haqqah"),
        70 to ("المعارج" to "Al-Ma'arij"), 71 to ("نوح" to "Nuh"), 72 to ("الجن" to "Al-Jinn"),
        73 to ("المزمل" to "Al-Muzzammil"), 74 to ("المدثر" to "Al-Muddaththir"), 75 to ("القيامة" to "Al-Qiyamah"),
        76 to ("الإنسان" to "Al-Insan"), 77 to ("المرسلات" to "Al-Mursalat"), 78 to ("النبأ" to "An-Naba"),
        79 to ("النازعات" to "An-Nazi'at"), 80 to ("عبس" to "'Abasa"), 81 to ("التكوير" to "At-Takwir"),
        82 to ("الانفطار" to "Al-Infitar"), 83 to ("المطففين" to "Al-Mutaffifin"), 84 to ("الانشقاق" to "Al-Inshiqaq"),
        85 to ("البروج" to "Al-Buruj"), 86 to ("الطارق" to "At-Tariq"), 87 to ("الأعلى" to "Al-A'la"),
        88 to ("الغاشية" to "Al-Ghashiyah"), 89 to ("الفجر" to "Al-Fajr"), 90 to ("البلد" to "Al-Balad"),
        91 to ("الشمس" to "Ash-Shams"), 92 to ("الليل" to "Al-Layl"), 93 to ("الضحى" to "Ad-Duha"),
        94 to ("الشرح" to "Ash-Sharh"), 95 to ("التين" to "At-Tin"), 96 to ("العلق" to "Al-'Alaq"),
        97 to ("القدر" to "Al-Qadr"), 98 to ("البينة" to "Al-Bayyinah"), 99 to ("الزلزلة" to "Az-Zalzalah"),
        100 to ("العاديات" to "Al-'Adiyat"), 101 to ("القارعة" to "Al-Qari'ah"), 102 to ("التكاثر" to "At-Takathur"),
        103 to ("العصر" to "Al-'Asr"), 104 to ("الهمزة" to "Al-Humazah"), 105 to ("الفيل" to "Al-Fil"),
        106 to ("قريش" to "Quraysh"), 107 to ("الماعون" to "Al-Ma'un"), 108 to ("الكوثر" to "Al-Kawthar"),
        109 to ("الكافرون" to "Al-Kafirun"), 110 to ("النصر" to "An-Nasr"), 111 to ("المسد" to "Al-Masad"),
        112 to ("الإخلاص" to "Al-Ikhlas"), 113 to ("الفلق" to "Al-Falaq"), 114 to ("الناس" to "An-Nas")
    )
}
