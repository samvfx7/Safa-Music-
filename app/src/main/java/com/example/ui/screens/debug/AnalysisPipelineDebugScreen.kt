package com.example.ui.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.SafaApplication
import com.example.data.model.ClassificationResult
import com.example.data.model.ContentType
import com.example.data.model.Track
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.ContentTypeBadge
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.StatusAllowedGreen
import com.example.ui.theme.StatusNotAllowedRed
import com.example.ui.theme.TextEmerald
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisPipelineDebugScreen(
    trackId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = SafaApplication.instance
    val repository = app.musicRepository

    val trackFlow = remember(trackId) { repository.getTrackById(trackId) }
    val track by trackFlow.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pipeline Trace & Debug",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Content Identification & Reasoning Trace",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("debug_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBackground)
            )
        },
        containerColor = AmoledBackground,
        modifier = modifier
    ) { paddingValues ->
        val currentTrack = track
        if (currentTrack == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading track pipeline data...", color = TextSecondary)
            }
            return@Scaffold
        }

        val classification = currentTrack.classification
        val audio = currentTrack.audioFeatures
        val lyrics = currentTrack.lyrics

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("analysis_pipeline_debug_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Track Header Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = currentTrack.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = currentTrack.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (classification != null) {
                                ContentTypeBadge(
                                    contentType = classification.contentType,
                                    status = classification.contentStatus
                                )
                                ClassificationBadge(
                                    status = classification.status,
                                    confidence = classification.confidence,
                                    showConfidence = true
                                )
                            }
                        }
                    }
                }
            }

            // STAGE 1: Content Identification (Stage 1 of the new architecture)
            item {
                PipelineStageCard(
                    stageNumber = 1,
                    stageTitle = "Content Identification & Qur'an Verification",
                    stageIcon = "🔍",
                    statusColor = if (classification?.contentType == ContentType.QURAN_RECITATION) IslamicGoldLight else EmeraldLight
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DebugRow("Detected Content Type", classification?.contentType?.displayName ?: "Unknown")
                        DebugRow("Certainty Status", classification?.contentStatus?.displayName ?: "Unknown")
                        DebugRow("Identification Method", classification?.identificationMethod ?: "Acoustic + Text Corpus")

                        if (classification?.identifiedSurah != null) {
                            DebugRow("Identified Surah", classification.identifiedSurah ?: "")
                        }
                        if (classification?.identifiedAyahRange != null) {
                            DebugRow("Ayah Range", classification.identifiedAyahRange ?: "")
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Identification Evidence:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        val idEvidence = classification?.evidenceList?.filter { it.category == "content_identification" } ?: emptyList()
                        if (idEvidence.isEmpty()) {
                            Text(
                                text = "• Acoustic signatures and metadata pattern matching applied.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        } else {
                            idEvidence.forEach {
                                Text(
                                    text = "• ${it.finding}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldLight
                                )
                            }
                        }
                    }
                }
            }

            // STAGE 2: Pipeline Routing Decision
            item {
                PipelineStageCard(
                    stageNumber = 2,
                    stageTitle = "Architecture Routing Decision",
                    stageIcon = "🔀",
                    statusColor = if (classification?.contentType?.isMusicCandidate == false) IslamicGoldLight else Color(0xFF60A5FA)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isNonMusic = classification?.contentType?.isMusicCandidate == false
                        if (isNonMusic) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2C2208))
                                    .border(1.dp, IslamicGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "✓ NON-MUSIC ROUTE ENGAGED",
                                        fontWeight = FontWeight.Bold,
                                        color = IslamicGoldLight,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Content identified as ${classification?.contentType?.displayName}. Music halal/haram classifier BYPASSED completely. Religious assessment assigned: NOT_APPLICABLE.",
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "✓ MUSIC ANALYSIS ROUTE ENGAGED",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF93C5FD),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Audio contains musical characteristics. Sent to Gemini/Local Islamic evidence reasoning engine.",
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // STAGE 3: Audio Observation Metrics
            item {
                PipelineStageCard(
                    stageNumber = 3,
                    stageTitle = "Objective Audio Observations",
                    stageIcon = "📊",
                    statusColor = EmeraldLight
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (audio != null) {
                            DebugRow("Duration", "${audio.durationSeconds}s (${currentTrack.formattedDuration})")
                            DebugRow("Vocals Detected", "${audio.vocalsDetected} (Prob: ${(audio.vocalProbability * 100).toInt()}%)")
                            DebugRow("Speech Detected", "${audio.speechDetected} (Prob: ${(audio.speechProbability * 100).toInt()}%)")
                            DebugRow("Instrumental Probability", "${(audio.instrumentalProbability * 100).toInt()}%")
                            DebugRow("Percussion Probability", "${(audio.percussionProbability * 100).toInt()}%")
                            DebugRow("RMS Loudness", "${"%.1f".format(audio.rmsLoudnessDb)} dB")
                            DebugRow("Silence Sections", "${audio.silenceSectionsCount}")
                        } else {
                            Text(text = "Audio features not computed yet", color = TextSecondary)
                        }
                    }
                }
            }

            // STAGE 4: Lyrics & Text Extraction
            item {
                PipelineStageCard(
                    stageNumber = 4,
                    stageTitle = "Text & Lyrics Verification",
                    stageIcon = "📝",
                    statusColor = if (lyrics?.status == "available") EmeraldLight else TextSecondary
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DebugRow("Status", lyrics?.status ?: "unavailable")
                        DebugRow("Source", lyrics?.source ?: "none")
                        DebugRow("Language", lyrics?.language ?: "unknown")
                        DebugRow("Explicit Flag", "${lyrics?.explicitFlagDetected == true || currentTrack.isExplicit}")

                        if (lyrics != null && lyrics.text.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Extracted Text Preview:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = lyrics.text.take(200),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // STAGE 5: Islamic Reasoning & Assessment
            item {
                PipelineStageCard(
                    stageNumber = 5,
                    stageTitle = "Methodology Assessment & Reasoning",
                    stageIcon = "⚖️",
                    statusColor = classification?.status?.color ?: EmeraldPrimary
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DebugRow("Methodology ID", classification?.methodologyId ?: "moderate_permissive")
                        DebugRow("Reasoner Engine", classification?.geminiModel ?: "unknown")
                        DebugRow("Offline Evaluation", "${classification?.isOfflineResult == true}")

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scholarly Rationale:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = classification?.reasoning ?: "No reasoning recorded.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )

                        if (classification != null && classification.limitations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Limitations & Constraints:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGoldLight
                            )
                            classification.limitations.forEach {
                                Text(
                                    text = "• $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineStageCard(
    stageNumber: Int,
    stageTitle: String,
    stageIcon: String,
    statusColor: Color,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f))
                        .border(1.dp, statusColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$stageNumber",
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = stageTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.weight(1f))
                Text(text = stageIcon, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DebugRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
