package com.example

import com.example.data.model.AudioFeatures
import com.example.data.model.ClassificationStatus
import com.example.data.model.ContentType
import com.example.data.model.DefaultMethodologies
import com.example.data.model.Lyrics
import com.example.data.model.Track
import com.example.data.remote.GeminiMusicClassifier
import com.example.domain.content.ContentIdentifier
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class InstrumentalTrackRegressionTest {

    private val contentIdentifier = ContentIdentifier()

    @Test
    fun testPureInstrumentalTrack_NotAutomaticallyForbiddenUnderModerateMethodology() = runBlocking {
        // Track: Clean instrumental piano composition
        val pianoTrack = Track(
            id = 201L,
            title = "Peaceful Piano Reflection",
            artist = "Acoustic Harmony",
            album = "Peaceful Moments",
            filePath = "/storage/emulated/0/Music/Instrumental/Piano.mp3",
            durationMs = 210000L,
            isExplicit = false
        )

        val audioFeatures = AudioFeatures(
            trackId = 201L,
            durationSeconds = 210L,
            vocalsDetected = false,
            speechDetected = false,
            vocalProbability = 0.05f,
            speechProbability = 0.02f,
            instrumentalProbability = 0.92f,
            percussionProbability = 0.15f,
            rmsLoudnessDb = -22.0f
        )

        // 1. Content identification identifies it as Music
        val contentId = contentIdentifier.identifyContent(pianoTrack, audioFeatures, null)
        assertEquals(ContentType.MUSIC, contentId.type)
        assertTrue(contentId.type.isMusicCandidate)

        // 2. Classify under Moderate / Content-Focused Methodology
        val classifier = GeminiMusicClassifier(RuntimeEnvironment.getApplication())
        val result = classifier.analyze(
            track = pianoTrack,
            audioFeatures = audioFeatures,
            lyrics = null,
            methodology = DefaultMethodologies.MODERATE_PERMISSIVE,
            contentIdentification = contentId
        )

        // RULE: Instrumental presence MUST NOT automatically produce a forbidden classification!
        assertNotEquals(
            "Instrumental track MUST NOT be automatically forbidden under moderate methodology",
            ClassificationStatus.NOT_ALLOWED,
            result.status
        )
        assertEquals(
            "Clean instrumental track should be ALLOWED under content-focused moderate methodology",
            ClassificationStatus.ALLOWED,
            result.status
        )
    }

    @Test
    fun testVocalOnlyNasheed_AllowedUnderVocalOnlyMethodology() = runBlocking {
        val nasheedTrack = Track(
            id = 202L,
            title = "Tala'al Badru 'Alayna (Vocal Acapella)",
            artist = "Traditional Nasheed Group",
            album = "Classic Nasheeds",
            filePath = "/storage/emulated/0/Music/Nasheed/TalaalBadru.mp3",
            durationMs = 180000L,
            isExplicit = false
        )

        val audioFeatures = AudioFeatures(
            trackId = 202L,
            durationSeconds = 180L,
            vocalsDetected = true,
            speechDetected = false,
            vocalProbability = 0.95f,
            speechProbability = 0.15f,
            instrumentalProbability = 0.05f,
            percussionProbability = 0.10f
        )

        val contentId = contentIdentifier.identifyContent(nasheedTrack, audioFeatures, null)
        assertEquals(ContentType.NASHEED, contentId.type)

        val classifier = GeminiMusicClassifier(RuntimeEnvironment.getApplication())
        val result = classifier.analyze(
            track = nasheedTrack,
            audioFeatures = audioFeatures,
            lyrics = null,
            methodology = DefaultMethodologies.VOCAL_ONLY,
            contentIdentification = contentId
        )

        assertEquals(
            "Vocal-only track must be ALLOWED under Vocal-Only methodology",
            ClassificationStatus.ALLOWED,
            result.status
        )
    }

    @Test
    fun testExplicitTrack_ClassifiedAsNotAllowedAcrossMethodologies() = runBlocking {
        val explicitTrack = Track(
            id = 203L,
            title = "Explicit Club Rap",
            artist = "Explicit Artist",
            album = "Explicit Album",
            filePath = "/storage/emulated/0/Music/Rap/Explicit.mp3",
            durationMs = 195000L,
            isExplicit = true
        )

        val audioFeatures = AudioFeatures(
            trackId = 203L,
            durationSeconds = 195L,
            vocalsDetected = true,
            vocalProbability = 0.85f,
            instrumentalProbability = 0.80f,
            percussionProbability = 0.90f
        )

        val explicitLyrics = Lyrics(
            trackId = 203L,
            text = "Vulgar lyrics with explicit content and profanity...",
            source = "embedded",
            confidence = 0.95f,
            status = "available",
            explicitFlagDetected = true
        )

        val contentId = contentIdentifier.identifyContent(explicitTrack, audioFeatures, explicitLyrics)
        val classifier = GeminiMusicClassifier(RuntimeEnvironment.getApplication())

        val resultModerate = classifier.analyze(
            track = explicitTrack,
            audioFeatures = audioFeatures,
            lyrics = explicitLyrics,
            methodology = DefaultMethodologies.MODERATE_PERMISSIVE,
            contentIdentification = contentId
        )

        assertEquals(
            "Explicit track MUST be NOT_ALLOWED even under Moderate methodology",
            ClassificationStatus.NOT_ALLOWED,
            resultModerate.status
        )
    }
}
