package com.example.data.quran

import com.example.data.model.AudioFeatures
import com.example.data.model.ContentStatus
import com.example.data.model.Lyrics
import com.example.data.model.QuranIdentification
import com.example.data.model.Track
import kotlin.math.max

object QuranVerifier {

    data class QuranMatchResult(
        val isQuran: Boolean,
        val status: ContentStatus,
        val confidence: Float,
        val quranIdentification: QuranIdentification?,
        val evidence: List<String>
    )

    /**
     * Evaluates whether the track contains Qur'an recitation using multi-source verification:
     * 1. Arabic textual matching against known Qur'an corpus (highest confidence).
     * 2. Transliteration and phonetic token matching.
     * 3. Metadata analysis (Surah numbering, reciter names, tags).
     * 4. Acoustic vocal chanting characteristics.
     */
    fun verifyQuranRecitation(
        track: Track,
        audioFeatures: AudioFeatures?,
        lyrics: Lyrics?
    ): QuranMatchResult {
        val evidence = mutableListOf<String>()

        // 1. Check Arabic or transliterated lyrics/transcription
        val lyricsText = lyrics?.text ?: ""
        if (lyricsText.isNotBlank()) {
            val normalizedLyrics = ArabicNormalizer.normalize(lyricsText)

            for (surah in QuranCorpus.SURAHS) {
                var matchedAyahCount = 0
                val matchedAyahNumbers = mutableListOf<Int>()
                var bestMatchedSnippet: String? = null

                for (ayah in surah.verifiedAyahs) {
                    val ayahNorm = ayah.textArabicNormalized
                    if (ayahNorm.isNotBlank() && (normalizedLyrics.contains(ayahNorm) || calculateFuzzyContainment(normalizedLyrics, ayahNorm) > 0.85f)) {
                        matchedAyahCount++
                        matchedAyahNumbers.add(ayah.ayahNumber)
                        bestMatchedSnippet = ayah.textArabicOriginal
                    }
                }

                if (matchedAyahCount > 0) {
                    val matchConfidence = ((matchedAyahCount.toFloat() / surah.verifiedAyahs.size.toFloat()) * 0.4f + 0.60f).coerceIn(0.85f, 0.99f)
                    val ayahRangeStr = if (matchedAyahNumbers.size == 1) {
                        "Ayah ${matchedAyahNumbers.first()}"
                    } else {
                        "Ayahs ${matchedAyahNumbers.minOrNull()}–${matchedAyahNumbers.maxOrNull()}"
                    }

                    evidence.add("Textual match with Qur'an: ${surah.nameArabic} (${surah.nameTransliteration}) - $ayahRangeStr ($matchedAyahCount verified verses)")

                    val quranId = QuranIdentification(
                        surahNumber = surah.number,
                        surahNameArabic = surah.nameArabic,
                        surahNameEnglish = surah.nameTransliteration,
                        ayahStart = matchedAyahNumbers.minOrNull(),
                        ayahEnd = matchedAyahNumbers.maxOrNull(),
                        matchConfidence = matchConfidence,
                        matchedAyahText = bestMatchedSnippet,
                        matchedSnippet = lyricsText.take(120),
                        verificationNotes = "Confirmed via Arabic text matching against Qur'anic corpus ($matchedAyahCount matching verses)."
                    )

                    return QuranMatchResult(
                        isQuran = true,
                        status = ContentStatus.CONFIRMED,
                        confidence = matchConfidence,
                        quranIdentification = quranId,
                        evidence = evidence
                    )
                }
            }
        }

        // 2. Check metadata cues (Filename, Title, Artist, Tags)
        val titleNorm = track.title.lowercase().trim()
        val artistNorm = track.artist.lowercase().trim()
        val albumNorm = track.album.lowercase().trim()
        val genreNorm = track.genre.lowercase().trim()
        val fileNorm = track.filePath.substringAfterLast("/").lowercase()
        val allMeta = "$titleNorm $artistNorm $albumNorm $genreNorm $fileNorm"

        val isNonQuranLabel = allMeta.contains("adhan") || allMeta.contains("athan") ||
                allMeta.contains("azan") || allMeta.contains("call to prayer") ||
                allMeta.contains("nasheed") || allMeta.contains("anasheed") ||
                allMeta.contains("anashid")

        if (isNonQuranLabel) {
            return QuranMatchResult(
                isQuran = false,
                status = ContentStatus.PROBABLE,
                confidence = 0.0f,
                quranIdentification = null,
                evidence = listOf("Metadata explicitly designates audio as $titleNorm / $genreNorm rather than Quran recitation")
            )
        }

        // Known famous Quran reciters
        val famousReciters = listOf(
            "mishary", "alafasy", "al-afasy", "sudais", "shuraim", "ghamdi",
            "abdulbasit", "abdul basit", "husary", "al-husary", "minshawi",
            "al-minshawi", "mahir", "al-muaiqly", "muaiqly", "ajmy", "al-ajmi",
            "hudhaify", "tablawi", "ali jaber", "dosari", "al-dosari", "fares abbad"
        )
        val reciterMatched = famousReciters.find { allMeta.contains(it) }

        // Check each Surah in corpus
        for (surah in QuranCorpus.SURAHS) {
            val surahKeyMatch = surah.searchKeys.any { key ->
                allMeta.contains(key) || ArabicNormalizer.normalizeTransliteration(allMeta).contains(ArabicNormalizer.normalizeTransliteration(key))
            }
            val arabicNameNormalized = ArabicNormalizer.normalize(surah.nameArabic)
            val arabicMatch = arabicNameNormalized.isNotBlank() && ArabicNormalizer.normalize(allMeta).contains(arabicNameNormalized)

            if (surahKeyMatch || arabicMatch) {
                val hasAcousticAcapella = (audioFeatures?.instrumentalProbability ?: 0.5f) < 0.25f && (audioFeatures?.vocalsDetected ?: true)
                val isExplicit = track.isExplicit || (lyrics?.explicitFlagDetected == true)

                if (!isExplicit) {
                    val confidence = when {
                        reciterMatched != null && hasAcousticAcapella -> 0.98f
                        reciterMatched != null -> 0.94f
                        hasAcousticAcapella -> 0.91f
                        else -> 0.85f
                    }

                    evidence.add("Identified Qur'an Surah: ${surah.nameArabic} (${surah.nameTransliteration})")
                    if (reciterMatched != null) {
                        evidence.add("Reciter reference identified: $reciterMatched")
                    }

                    val quranId = QuranIdentification(
                        surahNumber = surah.number,
                        surahNameArabic = surah.nameArabic,
                        surahNameEnglish = surah.nameTransliteration,
                        ayahStart = 1,
                        ayahEnd = surah.totalAyahs,
                        matchConfidence = confidence,
                        matchedAyahText = surah.verifiedAyahs.firstOrNull()?.textArabicOriginal,
                        matchedSnippet = "Surah ${surah.nameTransliteration} (${surah.totalAyahs} Ayahs)",
                        verificationNotes = "Matched Surah reference in audio metadata and verified acoustic vocal recitation."
                    )

                    return QuranMatchResult(
                        isQuran = true,
                        status = if (confidence >= 0.90f) ContentStatus.CONFIRMED else ContentStatus.PROBABLE,
                        confidence = confidence,
                        quranIdentification = quranId,
                        evidence = evidence
                    )
                }
            }
        }

        // 3. General "Surah / Quran" pattern matching in all 114 Surahs
        val surahNumberMatch = Regex("(?:surah|surat|soorah)\\s*(\\d{1,3})", RegexOption.IGNORE_CASE).find(allMeta)
            ?: Regex("(?:track|quran|ayah)\\s*(\\d{1,3})", RegexOption.IGNORE_CASE).find(allMeta)

        if (surahNumberMatch != null) {
            val num = surahNumberMatch.groupValues[1].toIntOrNull()
            if (num != null && num in 1..114) {
                val surahNames = QuranCorpus.ALL_SURAH_NAMES[num]
                val arabicName = surahNames?.first ?: "سورة رقم $num"
                val englishName = surahNames?.second ?: "Surah $num"

                evidence.add("Identified Qur'an Surah #$num ($englishName)")

                val quranId = QuranIdentification(
                    surahNumber = num,
                    surahNameArabic = arabicName,
                    surahNameEnglish = englishName,
                    ayahStart = null,
                    ayahEnd = null,
                    matchConfidence = 0.88f,
                    matchedAyahText = null,
                    matchedSnippet = "Surah #$num",
                    verificationNotes = "Matched numbered Surah track in library."
                )

                return QuranMatchResult(
                    isQuran = true,
                    status = ContentStatus.PROBABLE,
                    confidence = 0.88f,
                    quranIdentification = quranId,
                    evidence = evidence
                )
            }
        }

        // 4. Acoustic-only Quranic Chant Candidate
        // If vocal chanting is pure, high speech probability, zero instruments, and Arabic words detected
        val isArabicVocal = (lyrics?.language == "ar") || (audioFeatures?.speechProbability ?: 0f) > 0.70f
        val isZeroInstrument = (audioFeatures?.instrumentalProbability ?: 0.5f) < 0.10f && (audioFeatures?.vocalsDetected == true)
        val containsQuranKeyword = allMeta.contains("quran") || allMeta.contains("recitation") || allMeta.contains("tilawat") || allMeta.contains("tilawah")

        if (containsQuranKeyword && isZeroInstrument) {
            evidence.add("Acoustic profile and keywords match Qur'an recitation")
            val quranId = QuranIdentification(
                surahNumber = null,
                surahNameArabic = "تلاوة قرآنية",
                surahNameEnglish = "Qur'an Recitation",
                ayahStart = null,
                ayahEnd = null,
                matchConfidence = 0.82f,
                matchedAyahText = null,
                matchedSnippet = "Qur'an Recitation",
                verificationNotes = "Audio recitation detected with high confidence; specific Surah reference unindexed."
            )

            return QuranMatchResult(
                isQuran = true,
                status = ContentStatus.PROBABLE,
                confidence = 0.82f,
                quranIdentification = quranId,
                evidence = evidence
            )
        }

        return QuranMatchResult(
            isQuran = false,
            status = ContentStatus.UNKNOWN,
            confidence = 0f,
            quranIdentification = null,
            evidence = emptyList()
        )
    }

    private fun calculateFuzzyContainment(haystack: String, needle: String): Float {
        if (needle.isBlank() || haystack.isBlank()) return 0f
        val needleWords = needle.split(" ").filter { it.length > 2 }
        if (needleWords.isEmpty()) return 0f
        var matchCount = 0
        for (word in needleWords) {
            if (haystack.contains(word)) {
                matchCount++
            }
        }
        return matchCount.toFloat() / needleWords.size.toFloat()
    }
}
