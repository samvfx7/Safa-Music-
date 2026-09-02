package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.StatusAllowedGreen
import com.example.ui.theme.StatusInsufficientGray
import com.example.ui.theme.StatusNotAllowedRed
import com.example.ui.theme.StatusUnclearYellow

/**
 * High-level content categorization identifying the nature of the audio
 * BEFORE any religious music classification takes place.
 */
enum class ContentType(
    val id: String,
    val displayName: String,
    val badgeIcon: String,
    val isMusicCandidate: Boolean
) {
    QURAN_RECITATION("quran_recitation", "Qur'an Recitation", "📖", isMusicCandidate = false),
    ADHAN("adhan", "Adhan / Call to Prayer", "🕌", isMusicCandidate = false),
    ISLAMIC_SPEECH("islamic_speech", "Islamic Speech / Khutbah", "🎙️", isMusicCandidate = false),
    NASHEED("nasheed", "Nasheed / Islamic Vocal", "🌙", isMusicCandidate = true),
    MUSIC("music", "Music Track", "🎵", isMusicCandidate = true),
    SPOKEN_WORD("spoken_word", "Spoken Word / Audio", "🗣️", isMusicCandidate = false),
    PODCAST("podcast", "Podcast / Talk", "🎧", isMusicCandidate = false),
    SOUND_EFFECT("sound_effect", "Sound Effect / Ambient Nature", "🍃", isMusicCandidate = false),
    UNKNOWN("unknown", "Unclassified Audio", "❓", isMusicCandidate = false);

    val badgeColor: Color
        get() = when (this) {
            QURAN_RECITATION -> IslamicGoldLight
            ADHAN -> IslamicGold
            ISLAMIC_SPEECH -> EmeraldLight
            NASHEED -> EmeraldPrimary
            MUSIC -> Color(0xFF60A5FA)
            SPOKEN_WORD, PODCAST -> Color(0xFFA78BFA)
            SOUND_EFFECT -> Color(0xFF34D399)
            UNKNOWN -> StatusInsufficientGray
        }

    val badgeContainerColor: Color
        get() = when (this) {
            QURAN_RECITATION -> Color(0xFF2C2208)
            ADHAN -> Color(0xFF261D05)
            ISLAMIC_SPEECH -> Color(0xFF062E20)
            NASHEED -> Color(0xFF063323)
            MUSIC -> Color(0xFF1E293B)
            SPOKEN_WORD, PODCAST -> Color(0xFF2E1065)
            SOUND_EFFECT -> Color(0xFF064E3B)
            UNKNOWN -> Color(0xFF1F2937)
        }

    companion object {
        fun fromId(id: String?): ContentType {
            return when (id?.lowercase()?.trim()) {
                "quran", "quran_recitation", "quran_audio" -> QURAN_RECITATION
                "adhan", "athan", "azan" -> ADHAN
                "islamic_speech", "khutbah", "lecture", "bayan", "dawah" -> ISLAMIC_SPEECH
                "nasheed", "islamic_vocal", "anasheed" -> NASHEED
                "music", "song" -> MUSIC
                "spoken_word", "speech", "audiobook" -> SPOKEN_WORD
                "podcast" -> PODCAST
                "sound_effect", "ambient", "nature" -> SOUND_EFFECT
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Verification certainty level of the identified content.
 */
enum class ContentStatus(
    val id: String,
    val displayName: String
) {
    CONFIRMED("confirmed", "Confirmed"),
    PROBABLE("probable", "Probable"),
    CANDIDATE("candidate", "Candidate / Uncertain"),
    UNKNOWN("unknown", "Unknown");

    companion object {
        fun fromId(id: String?): ContentStatus {
            return when (id?.lowercase()?.trim()) {
                "confirmed", "verified" -> CONFIRMED
                "probable", "likely" -> PROBABLE
                "candidate", "possible" -> CANDIDATE
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Religious classification result.
 * NOT_APPLICABLE is mandatory for non-music content (Qur'an, Adhan, ordinary speech).
 */
enum class Classification(
    val id: String,
    val displayName: String,
    val badgeSymbol: String
) {
    ALLOWED("allowed", "Allowed", "🟢"),
    NOT_ALLOWED("not_allowed", "Not Allowed", "🔴"),
    UNCLEAR("unclear", "Unclear / Disputed", "🟡"),
    INSUFFICIENT_DATA("insufficient_data", "Insufficient Data", "⚪"),
    NOT_APPLICABLE("not_applicable", "Not Applicable", "📖");

    companion object {
        fun fromId(id: String?): Classification {
            return when (id?.lowercase()?.trim()) {
                "allowed", "halal" -> ALLOWED
                "not_allowed", "haram", "prohibited" -> NOT_ALLOWED
                "unclear", "disputed", "doubtful" -> UNCLEAR
                "insufficient_data", "insufficient" -> INSUFFICIENT_DATA
                "not_applicable", "na", "n/a", "quran", "skipped" -> NOT_APPLICABLE
                else -> INSUFFICIENT_DATA
            }
        }
    }
}

/**
 * Detailed content identification payload prior to any religious evaluation.
 */
data class ContentIdentification(
    val type: ContentType,
    val status: ContentStatus,
    val confidence: Float,
    val evidence: List<String> = emptyList(),
    val detectedLanguage: String? = null,
    val identifiedTitle: String? = null,
    val identifiedSurah: String? = null,
    val identifiedAyahRange: String? = null,
    val identificationMethod: String = "rule_and_text_corpus"
)

/**
 * Pure objective audio observation metrics.
 * Never directly converted into a religious ruling.
 */
data class AudioObservation(
    val vocalsDetected: Boolean?,
    val speechDetected: Boolean?,
    val vocalProbability: Float?,
    val speechProbability: Float?,
    val instrumentalProbability: Float?,
    val percussionProbability: Float?,
    val language: String?,
    val durationSeconds: Long,
    val rmsLoudnessDb: Float? = null
)

/**
 * Result of applying an Islamic methodology to verified music evidence.
 */
data class ReligiousAssessment(
    val classification: Classification,
    val confidence: Float,
    val reasoning: String,
    val evidence: List<EvidenceItem> = emptyList(),
    val missingInformation: List<String> = emptyList(),
    val limitations: List<String> = emptyList(),
    val methodologyId: String
)

data class QuranIdentification(
    val surahNumber: Int?,
    val surahNameArabic: String?,
    val surahNameEnglish: String?,
    val ayahStart: Int?,
    val ayahEnd: Int?,
    val matchConfidence: Float,
    val matchedAyahText: String?,
    val matchedSnippet: String?,
    val verificationNotes: String
)
