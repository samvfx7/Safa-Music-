package com.example.data.model

data class Methodology(
    val id: String,
    val name: String,
    val shortDescription: String,
    val fullCriteria: String,
    val scholarlyContext: String,
    val isCustom: Boolean = false,
    val version: Int = 1
)

object DefaultMethodologies {
    val CONSERVATIVE = Methodology(
        id = "conservative",
        name = "Conservative (Traditional Scholarly)",
        shortDescription = "Strict prohibition of musical instruments (wind/strings). Nasheed with duff/vocal only.",
        fullCriteria = """
            1. All melodic and harmonic instruments (strings, woodwinds, brass, synth keys, electric guitars) are strictly prohibited.
            2. Only pure unassisted human vocalization or acoustic frame drum (duff) without cymbals is permitted.
            3. Lyrics must be entirely free from profane, hedonistic, shirk, romantic desire, or vulgar themes and must promote Islamic values, remembrance, or clean poetry.
            4. If melodic instrumentation is detected or lyrics contain impermissible themes, classify as 'not_allowed'.
            5. If instrumental status or lyrics are unavailable, classify as 'insufficient_data'.
        """.trimIndent(),
        scholarlyContext = "Reflects the majority position among traditional classical jurists (including majority of the four Madhhabs) holding that musical instruments with melodic accompaniment are prohibited, while vocal nasheeds and acoustic duff are permitted in permissible contexts.",
        isCustom = false,
        version = 1
    )

    val MODERATE_PERMISSIVE = Methodology(
        id = "moderate_permissive",
        name = "Moderate / Permissive (Content & Purpose Focus)",
        shortDescription = "Instruments permitted if content is virtuous, wholesome, and free of sinful themes.",
        fullCriteria = """
            1. Musical instrumentation is generally permissible as long as it does not arouse base desires, encourage immorality, or accompany sinful settings.
            2. The primary criteria is the message of the lyrics: songs promoting virtue, nature, social consciousness, national pride, clean romance, spirituality, or clean aesthetics are 'allowed'.
            3. Songs promoting illicit relationships, drug/alcohol abuse, vanity, violence, materialism, vulgar language, or blasphemy are strictly 'not_allowed'.
            4. Highly distorted or aggressive music intended for intoxicated/hedonistic club environments should be evaluated with caution or classified as 'not_allowed'/'unclear'.
            5. If lyrics cannot be verified, classify as 'unclear' or 'insufficient_data'.
        """.trimIndent(),
        scholarlyContext = "Follows the scholarly viewpoint held by contemporary scholars (e.g., Sheikh Yusuf al-Qaradawi, Ibn Hazm, al-Ghazali) who argue that music in its essence is sound whose permissibility is governed by its lyrical content, effect on the soul, and surrounding environment.",
        isCustom = false,
        version = 1
    )

    val VOCAL_ONLY = Methodology(
        id = "vocal_only",
        name = "Vocal-Only Nasheed (Acapella Strict)",
        shortDescription = "Zero instruments of any kind. Only human voice, harmonies, and organic hums.",
        fullCriteria = """
            1. Absolutely no instruments (melodic, electronic, synthesized, or percussion/drums) are permitted.
            2. Only pure human acapella, vocal harmonies, natural vocal hums, and vocal sound-effects are allowed.
            3. If any percussion, drums, or acoustic/synthetic instruments are detected above 15% probability, classify as 'not_allowed'.
            4. Lyrics must promote Islamic spirituality, nasheed poetry, or wholesome morals.
        """.trimIndent(),
        scholarlyContext = "Strict nasheed standard adopted by audiences and studios producing acapella-only Islamic recordings with complete exclusion of drums and digital instruments.",
        isCustom = false,
        version = 1
    )

    val ALL = listOf(CONSERVATIVE, MODERATE_PERMISSIVE, VOCAL_ONLY)

    fun getById(id: String?): Methodology {
        return ALL.find { it.id.equals(id, ignoreCase = true) } ?: CONSERVATIVE
    }
}
