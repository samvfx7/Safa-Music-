package com.example.data.quran

/**
 * Robust Arabic text normalizer designed for Qur'an and Islamic speech matching.
 * Strips diacritics (harakat/tashkeel), unifies orthographic variations (Hamza, Alif,
 * Taa Marbutah, Alif Maqsurah), and removes Quranic recitation annotation symbols.
 */
object ArabicNormalizer {

    // Unicode ranges and sets
    private val HARAKAT_REGEX = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    private val TATWEEL_REGEX = Regex("\\u0640")
    private val PUNCTUATION_REGEX = Regex("[\\p{Punct}،؛؟«»‹›\"'\\-–—()\\[\\]{}….]")
    private val MULTI_SPACE_REGEX = Regex("\\s+")

    /**
     * Normalizes an Arabic string into a canonical phonetic search form.
     */
    fun normalize(input: String?): String {
        if (input.isNullOrBlank()) return ""

        var text = input

        // 1. Remove Harakat / Tashkeel and Qur'anic pause/annotation signs
        text = HARAKAT_REGEX.replace(text, "")

        // 2. Remove Tatweel (kashida)
        text = TATWEEL_REGEX.replace(text, "")

        // 3. Normalize Alif variants (أ, إ, آ, ٱ, ٲ, ٳ -> ا)
        text = text.replace(Regex("[\\u0622\\u0623\\u0625\\u0671\\u0672\\u0673]"), "ا")

        // 4. Normalize Taa Marbutah (ة, ۃ -> ه or ت) -> normalize to ه for flexible matching
        text = text.replace(Regex("[\\u0629\\u06C3]"), "ه")

        // 5. Normalize Alif Maqsurah & Ya variants (ى, ۍ, ې, ئ -> ي)
        text = text.replace(Regex("[\\u0649\\u067E\\u06CC]"), "ي")

        // 6. Normalize Waw with Hamza (ؤ -> و)
        text = text.replace("ؤ", "و")

        // 7. Normalize standalone Hamza (ء -> "")
        text = text.replace("ء", "")

        // 8. Remove punctuation and special symbols
        text = PUNCTUATION_REGEX.replace(text, " ")

        // 9. Lowercase if mixed with Latin transliteration
        text = text.lowercase()

        // 10. Normalize whitespace
        text = MULTI_SPACE_REGEX.replace(text, " ").trim()

        return text
    }

    /**
     * Normalizes transliterated English/Latin Qur'an keywords (e.g., "Surat Al-Ikhlas", "Ikhlaas", "Ikhlas").
     */
    fun normalizeTransliteration(input: String?): String {
        if (input.isNullOrBlank()) return ""
        return input.lowercase()
            .replace("surah", "")
            .replace("surat", "")
            .replace("soorah", "")
            .replace("al-", "")
            .replace("el-", "")
            .replace("an-", "")
            .replace("ar-", "")
            .replace("as-", "")
            .replace("at-", "")
            .replace("az-", "")
            .replace("ash-", "")
            .replace("adh-", "")
            .replace("aa", "a")
            .replace("ee", "i")
            .replace("oo", "u")
            .replace("kh", "k")
            .replace("gh", "g")
            .replace("th", "t")
            .replace("sh", "s")
            .replace("dh", "d")
            .replace("zh", "z")
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }
}
