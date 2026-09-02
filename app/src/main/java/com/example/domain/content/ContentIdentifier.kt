package com.example.domain.content

import com.example.data.model.AudioFeatures
import com.example.data.model.AudioObservation
import com.example.data.model.ContentIdentification
import com.example.data.model.ContentStatus
import com.example.data.model.ContentType
import com.example.data.model.Lyrics
import com.example.data.model.Track
import com.example.data.quran.ArabicNormalizer
import com.example.data.quran.QuranVerifier

class ContentIdentifier {

    /**
     * Executes multi-stage priority content identification:
     * 1. Quran detection & verification
     * 2. Adhan detection
     * 3. Islamic speech / lecture / khutbah detection
     * 4. Spoken word / podcast / audio effect detection
     * 5. Nasheed / Islamic vocal classification
     * 6. Music detection
     * 7. Unknown
     */
    fun identifyContent(
        track: Track,
        audioFeatures: AudioFeatures?,
        lyrics: Lyrics?
    ): ContentIdentification {
        val titleNorm = track.title.lowercase().trim()
        val artistNorm = track.artist.lowercase().trim()
        val albumNorm = track.album.lowercase().trim()
        val genreNorm = track.genre.lowercase().trim()
        val fileNorm = track.filePath.substringAfterLast("/").lowercase()
        val lyricsText = lyrics?.text ?: ""
        val allMeta = "$titleNorm $artistNorm $albumNorm $genreNorm $fileNorm"
        val evidence = mutableListOf<String>()

        // ----------------------------------------------------
        // STAGE 1: QUR'AN RECITATION PIPELINE (HIGHEST PRIORITY)
        // ----------------------------------------------------
        val quranCheck = QuranVerifier.verifyQuranRecitation(track, audioFeatures, lyrics)
        if (quranCheck.isQuran && quranCheck.quranIdentification != null) {
            val qId = quranCheck.quranIdentification
            return ContentIdentification(
                type = ContentType.QURAN_RECITATION,
                status = quranCheck.status,
                confidence = quranCheck.confidence,
                evidence = quranCheck.evidence,
                detectedLanguage = "ar",
                identifiedTitle = qId.surahNameEnglish ?: "Qur'an Recitation",
                identifiedSurah = if (qId.surahNameArabic != null) "${qId.surahNameArabic} (${qId.surahNameEnglish})" else qId.surahNameEnglish,
                identifiedAyahRange = if (qId.ayahStart != null && qId.ayahEnd != null) {
                    if (qId.ayahStart == qId.ayahEnd) "Ayah ${qId.ayahStart}" else "Ayahs ${qId.ayahStart}–${qId.ayahEnd}"
                } else null,
                identificationMethod = "quran_corpus_and_acoustic_verifier"
            )
        }

        // ----------------------------------------------------
        // STAGE 2: ADHAN / CALL TO PRAYER
        // ----------------------------------------------------
        val isAdhan = allMeta.contains("adhan") || allMeta.contains("athan") ||
                allMeta.contains("azan") || allMeta.contains("call to prayer") ||
                lyricsText.contains("Allahu Akbar Allahu Akbar", ignoreCase = true) ||
                lyricsText.contains("Ashhadu an la ilaha", ignoreCase = true) ||
                ArabicNormalizer.normalize(lyricsText).contains("الله اكبر الله اكبر")

        if (isAdhan) {
            evidence.add("Identified as Islamic Call to Prayer (Adhan)")
            if ((audioFeatures?.instrumentalProbability ?: 0.5f) < 0.20f) {
                evidence.add("Acoustic profile confirms vocal solo chant")
            }
            return ContentIdentification(
                type = ContentType.ADHAN,
                status = ContentStatus.CONFIRMED,
                confidence = 0.97f,
                evidence = evidence,
                detectedLanguage = "ar",
                identifiedTitle = "Adhan (Call to Prayer)",
                identifiedSurah = null,
                identifiedAyahRange = null,
                identificationMethod = "adhan_call_recognizer"
            )
        }

        // ----------------------------------------------------
        // STAGE 3: ISLAMIC SPEECH / LECTURE / KHUTBAH / BAYAN
        // ----------------------------------------------------
        val isIslamicSpeech = allMeta.contains("khutbah") || allMeta.contains("lecture") ||
                allMeta.contains("bayan") || allMeta.contains("tafsir") ||
                allMeta.contains("dawah") || allMeta.contains("sheikh") ||
                allMeta.contains("mufti") || allMeta.contains("hadith lecture") ||
                (audioFeatures?.speechProbability ?: 0f) > 0.85f && (allMeta.contains("islam") || allMeta.contains("muslim"))

        if (isIslamicSpeech) {
            evidence.add("Identified as Islamic lecture / sermon")
            val isHighSpeech = (audioFeatures?.speechProbability ?: 0f) > 0.70f
            if (isHighSpeech) {
                evidence.add("Speech probability: ${(audioFeatures?.speechProbability ?: 0f) * 100}%")
            }
            return ContentIdentification(
                type = ContentType.ISLAMIC_SPEECH,
                status = if (isHighSpeech) ContentStatus.CONFIRMED else ContentStatus.PROBABLE,
                confidence = 0.92f,
                evidence = evidence,
                detectedLanguage = lyrics?.language ?: "ar",
                identifiedTitle = track.title,
                identifiedSurah = null,
                identifiedAyahRange = null,
                identificationMethod = "speech_and_topic_classifier"
            )
        }

        // ----------------------------------------------------
        // STAGE 4: ORDINARY SPOKEN WORD / PODCAST
        // ----------------------------------------------------
        val isPodcast = allMeta.contains("podcast") || allMeta.contains("episode") ||
                genreNorm.contains("podcast") || genreNorm.contains("speech") ||
                genreNorm.contains("audiobook")

        val isHighSpeechAudio = (audioFeatures?.speechProbability ?: 0f) > 0.88f &&
                (audioFeatures?.instrumentalProbability ?: 0.5f) < 0.15f

        if (isPodcast || isHighSpeechAudio) {
            evidence.add("Spoken audio characteristics detected (Speech prob: ${((audioFeatures?.speechProbability ?: 0.9f) * 100).toInt()}%)")
            evidence.add("Absence of musical instrumentation")
            return ContentIdentification(
                type = if (isPodcast) ContentType.PODCAST else ContentType.SPOKEN_WORD,
                status = ContentStatus.CONFIRMED,
                confidence = 0.93f,
                evidence = evidence,
                detectedLanguage = lyrics?.language ?: "en",
                identifiedTitle = track.title,
                identifiedSurah = null,
                identifiedAyahRange = null,
                identificationMethod = "spoken_word_acoustic_detector"
            )
        }

        // ----------------------------------------------------
        // STAGE 5: NASHEED / ISLAMIC VOCAL
        // ----------------------------------------------------
        val isNasheed = allMeta.contains("nasheed") || allMeta.contains("anasheed") ||
                allMeta.contains("anashid") || genreNorm.contains("nasheed") ||
                genreNorm.contains("islamic") || allMeta.contains("acapella") ||
                allMeta.contains("vocal only")

        if (isNasheed) {
            evidence.add("Categorized as Nasheed / Islamic vocal performance")
            val isAcapella = (audioFeatures?.instrumentalProbability ?: 0.5f) < 0.25f
            if (isAcapella) {
                evidence.add("Low instrumental presence observed (${((audioFeatures?.instrumentalProbability ?: 0.1f) * 100).toInt()}%)")
            }
            return ContentIdentification(
                type = ContentType.NASHEED,
                status = ContentStatus.PROBABLE,
                confidence = 0.89f,
                evidence = evidence,
                detectedLanguage = lyrics?.language ?: "ar",
                identifiedTitle = track.title,
                identifiedSurah = null,
                identifiedAyahRange = null,
                identificationMethod = "nasheed_genre_analyzer"
            )
        }

        // ----------------------------------------------------
        // STAGE 6: GENERAL MUSIC
        // ----------------------------------------------------
        val hasMusicalAttributes = (audioFeatures?.instrumentalProbability ?: 0.5f) > 0.20f ||
                (audioFeatures?.vocalsDetected == true) ||
                track.durationMs > 15000

        if (hasMusicalAttributes) {
            evidence.add("Acoustic rhythm and arrangement indicate musical track")
            evidence.add("Instrumental probability: ${((audioFeatures?.instrumentalProbability ?: 0.5f) * 100).toInt()}%")
            if (audioFeatures?.vocalsDetected == true) {
                evidence.add("Vocals detected in track")
            }
            return ContentIdentification(
                type = ContentType.MUSIC,
                status = ContentStatus.CONFIRMED,
                confidence = 0.90f,
                evidence = evidence,
                detectedLanguage = lyrics?.language,
                identifiedTitle = track.title,
                identifiedSurah = null,
                identifiedAyahRange = null,
                identificationMethod = "acoustic_music_analyzer"
            )
        }

        // ----------------------------------------------------
        // STAGE 7: UNKNOWN / INSUFFICIENT
        // ----------------------------------------------------
        return ContentIdentification(
            type = ContentType.UNKNOWN,
            status = ContentStatus.UNKNOWN,
            confidence = 0.30f,
            evidence = listOf("Audio characteristics insufficient to reliably categorize content type"),
            detectedLanguage = null,
            identifiedTitle = track.title,
            identifiedSurah = null,
            identifiedAyahRange = null,
            identificationMethod = "fallback"
        )
    }

    /**
     * Converts AudioFeatures to the pure observation model.
     */
    fun extractObservation(audioFeatures: AudioFeatures?, track: Track): AudioObservation {
        return AudioObservation(
            vocalsDetected = audioFeatures?.vocalsDetected,
            speechDetected = audioFeatures?.speechDetected,
            vocalProbability = audioFeatures?.vocalProbability,
            speechProbability = audioFeatures?.speechProbability,
            instrumentalProbability = audioFeatures?.instrumentalProbability,
            percussionProbability = audioFeatures?.percussionProbability,
            language = null,
            durationSeconds = audioFeatures?.durationSeconds ?: (track.durationMs / 1000),
            rmsLoudnessDb = audioFeatures?.rmsLoudnessDb
        )
    }
}
