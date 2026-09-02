package com.example

import com.example.data.model.AudioFeatures
import com.example.data.model.ClassificationStatus
import com.example.data.model.ContentType
import com.example.data.model.DefaultMethodologies
import com.example.data.model.Lyrics
import com.example.data.model.Track
import com.example.data.quran.ArabicNormalizer
import com.example.data.remote.GeminiMusicClassifier
import com.example.domain.content.ContentIdentifier
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class QuranClassificationRegressionTest {

    private val contentIdentifier = ContentIdentifier()

    @Test
    fun testSurahAlIkhlas_IdentifiedAsQuranRecitation_NotForbidden() = runBlocking {
        val ikhlasTrack = Track(
            id = 101L,
            title = "Surah Al-Ikhlas (The Sincerity)",
            artist = "Mishary Rashid Alafasy",
            album = "Holy Quran Recitations",
            filePath = "/storage/emulated/0/Music/Quran/112_Al_Ikhlas.mp3",
            durationMs = 45000L
        )

        val audioFeatures = AudioFeatures(
            trackId = 101L,
            durationSeconds = 45L,
            vocalsDetected = true,
            speechDetected = true,
            vocalProbability = 0.98f,
            speechProbability = 0.90f,
            instrumentalProbability = 0.01f,
            percussionProbability = 0.01f,
            rmsLoudnessDb = -18.0f
        )

        val quranLyrics = Lyrics(
            trackId = 101L,
            text = "قُلْ هُوَ اللَّهُ أَحَدٌ . اللَّهُ الصَّمَدُ . لَمْ يَلِدْ وَلَمْ يُولَدْ . وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
            source = "verified_mushaf",
            confidence = 1.0f,
            status = "available"
        )

        // 1. Verify content identification stage
        val contentId = contentIdentifier.identifyContent(ikhlasTrack, audioFeatures, quranLyrics)

        assertEquals(ContentType.QURAN_RECITATION, contentId.type)
        assertFalse("Qur'an recitation MUST NOT be a music candidate", contentId.type.isMusicCandidate)
        assertNotNull("Identified Surah must be populated", contentId.identifiedSurah)
        assertTrue(contentId.identifiedSurah!!.contains("Al-Ikhlas") || contentId.identifiedSurah!!.contains("الإخلاص"))

        // 2. Verify Classifier Output with Offline Engine (as in app runtime)
        val classifier = GeminiMusicClassifier(RuntimeEnvironment.getApplication())
        val result = classifier.analyze(
            track = ikhlasTrack,
            audioFeatures = audioFeatures,
            lyrics = quranLyrics,
            methodology = DefaultMethodologies.MODERATE_PERMISSIVE,
            contentIdentification = contentId
        )

        // Must be NOT_APPLICABLE, NEVER NOT_ALLOWED / HARAM
        assertEquals(
            "Surah Al-Ikhlas recitation must be marked as NOT_APPLICABLE, never forbidden",
            ClassificationStatus.NOT_APPLICABLE,
            result.status
        )
        assertEquals(ContentType.QURAN_RECITATION, result.contentType)
        assertTrue(result.reasoning.contains("Qur'an recitation is divine revelation", ignoreCase = true) ||
                result.reasoning.contains("Holy Qur'an", ignoreCase = true))
    }

    @Test
    fun testSurahAlFatiha_IdentifiedAsQuranRecitation() = runBlocking {
        val fatihaTrack = Track(
            id = 102L,
            title = "001 Al-Fatiha",
            artist = "Abdul Basit Abdus Samad",
            album = "Murattal Quran",
            filePath = "/storage/emulated/0/Music/Quran/001_Al_Fatiha.mp3",
            durationMs = 90000L
        )

        val audioFeatures = AudioFeatures(
            trackId = 102L,
            durationSeconds = 90L,
            vocalsDetected = true,
            speechDetected = true,
            vocalProbability = 0.99f,
            speechProbability = 0.85f,
            instrumentalProbability = 0.00f,
            percussionProbability = 0.00f,
            rmsLoudnessDb = -16.0f
        )

        val fatihaLyrics = Lyrics(
            trackId = 102L,
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ . الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ . الرَّحْمَٰنِ الرَّحِيمِ . مَالِكِ يَوْمِ الدِّينِ",
            source = "verified_mushaf",
            confidence = 1.0f,
            status = "available"
        )

        val contentId = contentIdentifier.identifyContent(fatihaTrack, audioFeatures, fatihaLyrics)
        assertEquals(ContentType.QURAN_RECITATION, contentId.type)

        val classifier = GeminiMusicClassifier(RuntimeEnvironment.getApplication())
        val result = classifier.analyze(
            track = fatihaTrack,
            audioFeatures = audioFeatures,
            lyrics = fatihaLyrics,
            methodology = DefaultMethodologies.CONSERVATIVE,
            contentIdentification = contentId
        )

        assertEquals(ClassificationStatus.NOT_APPLICABLE, result.status)
        assertEquals(ContentType.QURAN_RECITATION, result.contentType)
    }

    @Test
    fun testAdhanAudio_IdentifiedAsAdhan_ExemptFromMusicClassifier() = runBlocking {
        val adhanTrack = Track(
            id = 103L,
            title = "Adhan Makkah Fajr",
            artist = "Ali Ahmed Mullah",
            album = "Islamic Calls to Prayer",
            filePath = "/storage/emulated/0/Music/Adhan/Makkah.mp3",
            durationMs = 180000L
        )

        val audioFeatures = AudioFeatures(
            trackId = 103L,
            durationSeconds = 180L,
            vocalsDetected = true,
            speechDetected = true,
            vocalProbability = 0.95f,
            speechProbability = 0.60f,
            instrumentalProbability = 0.01f,
            percussionProbability = 0.01f
        )

        val contentId = contentIdentifier.identifyContent(adhanTrack, audioFeatures, null)
        assertEquals(ContentType.ADHAN, contentId.type)
        assertFalse(contentId.type.isMusicCandidate)

        val classifier = GeminiMusicClassifier(RuntimeEnvironment.getApplication())
        val result = classifier.analyze(
            track = adhanTrack,
            audioFeatures = audioFeatures,
            lyrics = null,
            methodology = DefaultMethodologies.MODERATE_PERMISSIVE,
            contentIdentification = contentId
        )

        assertEquals(ClassificationStatus.NOT_APPLICABLE, result.status)
        assertEquals(ContentType.ADHAN, result.contentType)
    }

    @Test
    fun testArabicNormalizer_CorrectlyRemovesDiacriticsAndTashkeel() {
        val raw = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
        val normalized = ArabicNormalizer.normalize(raw)
        assertEquals("بسم الله الرحمن الرحيم", normalized)
    }
}
