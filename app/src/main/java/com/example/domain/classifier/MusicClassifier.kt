package com.example.domain.classifier

import com.example.data.model.AudioFeatures
import com.example.data.model.ClassificationResult
import com.example.data.model.Lyrics
import com.example.data.model.Methodology
import com.example.data.model.Track

interface MusicClassifier {
    suspend fun analyze(
        track: Track,
        audioFeatures: AudioFeatures,
        lyrics: Lyrics?,
        methodology: Methodology
    ): ClassificationResult
}
