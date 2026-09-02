package com.example.ui.screens.result

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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.SafaApplication
import com.example.data.model.ClassificationResult
import com.example.data.model.ClassificationStatus
import com.example.data.model.ContentType
import com.example.data.model.DefaultMethodologies
import com.example.data.model.EvidenceItem
import com.example.data.model.Track
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.ContentTypeBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.IslamicDisclaimerBanner
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.StatusAllowedGreen
import com.example.ui.theme.TextEmerald
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongAnalysisDetailScreen(
    trackId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToDebug: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val app = SafaApplication.instance
    val repository = app.musicRepository
    val playerManager = app.playerManager
    val scannerManager = app.libraryScannerManager
    val preferencesRepository = app.preferencesRepository
    val scope = rememberCoroutineScope()

    val trackFlow = remember(trackId) { repository.getTrackById(trackId) }
    val track by trackFlow.collectAsStateWithLifecycle(initialValue = null)

    val methodologiesFlow = remember { repository.getMethodologies() }
    val methodologies by methodologiesFlow.collectAsStateWithLifecycle(initialValue = DefaultMethodologies.ALL)

    var showMethodologyDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Evidence & Rationale",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (onNavigateToDebug != null) {
                        IconButton(
                            onClick = { onNavigateToDebug(trackId) },
                            modifier = Modifier.testTag("detail_debug_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Pipeline Debug",
                                tint = IslamicGoldLight
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            track?.let { scannerManager.scanSingleTrack(it.id) }
                        },
                        modifier = Modifier.testTag("reanalyze_detail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Re-analyze",
                            tint = EmeraldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBackground)
            )
        },
        containerColor = AmoledBackground,
        modifier = modifier
    ) { paddingValues ->
        if (track == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        } else {
            val nonNullTrack = track!!
            val classification = nonNullTrack.classification
            val audio = nonNullTrack.audioFeatures
            val lyrics = nonNullTrack.lyrics
            val isQuran = classification?.contentType == ContentType.QURAN_RECITATION

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Track Info Card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nonNullTrack.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${nonNullTrack.artist} • ${nonNullTrack.album}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }

                                IconButton(
                                    onClick = { playerManager.playTrack(nonNullTrack) },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary)
                                        .testTag("detail_play_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = AmoledBackground
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
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

                                Text(
                                    text = "Duration: ${nonNullTrack.formattedDuration}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // DEDICATED QUR'AN RECITATION VERIFICATION HERO CARD
                if (isQuran) {
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, IslamicGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = "Quran",
                                        tint = IslamicGoldLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Holy Qur'an Recitation",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = IslamicGoldLight
                                    )
                                }

                                if (classification.identifiedSurah != null) {
                                    Text(
                                        text = classification.identifiedSurah ?: "",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                if (classification.identifiedAyahRange != null) {
                                    Text(
                                        text = classification.identifiedAyahRange ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = IslamicGoldLight
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusAllowedGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Qur'an detected",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextPrimary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusAllowedGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Recitation verified",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2C2208))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Music Classification: Not Applicable (Holy Qur'an is divine revelation, not subject to music rulings)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = IslamicGoldLight,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Methodology Switcher Card (Only for Music Candidates)
                if (classification?.contentType?.isMusicCandidate != false) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Balance,
                                            contentDescription = null,
                                            tint = IslamicGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Evaluation Criteria / Madhhab",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = IslamicGoldLight
                                        )
                                    }

                                    Box {
                                        OutlinedButton(
                                            onClick = { showMethodologyDropdown = true },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("switch_methodology_button")
                                        ) {
                                            Text(
                                                text = "Change Criteria",
                                                fontSize = 11.sp,
                                                color = IslamicGold
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showMethodologyDropdown,
                                            onDismissRequest = { showMethodologyDropdown = false },
                                            modifier = Modifier.background(DarkSurfaceElevated)
                                        ) {
                                            methodologies.forEach { meth ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(
                                                                text = meth.name,
                                                                color = TextPrimary,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            Text(
                                                                text = meth.shortDescription,
                                                                color = TextSecondary,
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        showMethodologyDropdown = false
                                                        scope.launch {
                                                            preferencesRepository.setActiveMethodologyId(meth.id)
                                                            scannerManager.scanSingleTrack(nonNullTrack.id)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = "Current: ${DefaultMethodologies.getById(classification?.methodologyId).name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // AI Scholarly Reasoning / Assessment Card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = if (isQuran) "Content Identification Summary" else "Detailed Scholarly Rationale",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )

                            Text(
                                text = classification?.reasoning ?: "No analysis recorded yet. Tap 'Re-analyze' to run multi-stage inspection.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Itemized Evidence
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Itemized Evidence (${classification?.evidenceList?.size ?: 0})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (classification?.evidenceList.isNullOrEmpty()) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "No itemized evidence entries recorded.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        } else {
                            classification!!.evidenceList.forEach { evidence ->
                                EvidenceCard(evidence = evidence)
                            }
                        }
                    }
                }

                // Pipeline Debug Action Button
                if (onNavigateToDebug != null) {
                    item {
                        OutlinedButton(
                            onClick = { onNavigateToDebug(nonNullTrack.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("view_pipeline_debug_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = IslamicGoldLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Pipeline Trace & Verification Logs",
                                color = TextPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // Audio Features Card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Acoustic Signal Characteristics",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            if (audio != null) {
                                WaveformVisualizer(
                                    waveformPoints = audio.waveformPoints,
                                    height = 40.dp,
                                    activeColor = EmeraldPrimary
                                )

                                AudioMetricBar(
                                    label = "Vocal Dominance",
                                    value = audio.vocalProbability,
                                    color = EmeraldPrimary
                                )

                                AudioMetricBar(
                                    label = "Instrumental Presence",
                                    value = audio.instrumentalProbability,
                                    color = IslamicGold
                                )

                                AudioMetricBar(
                                    label = "Percussion Energy",
                                    value = audio.percussionProbability,
                                    color = Color(0xFF38BDF8)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Sample Rate: ${audio.sampleRate} Hz",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "RMS Loudness: ${"%.1f".format(audio.rmsLoudnessDb)} dB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "Bitrate: ${audio.bitrateKbps} kbps",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Islamic Disclaimer
                item {
                    IslamicDisclaimerBanner()
                }
            }
        }
    }
}

@Composable
private fun EvidenceCard(evidence: EvidenceItem) {
    val categoryColor = when (evidence.category) {
        "content_identification" -> IslamicGoldLight
        "lyrics" -> EmeraldLight
        "audio" -> Color(0xFF38BDF8)
        "metadata" -> Color(0xFFA78BFA)
        else -> TextSecondary
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(categoryColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = evidence.category.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evidence.finding,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Importance: ${evidence.importance}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun AudioMetricBar(label: String, value: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = DarkSurfaceElevated,
        )
    }
}
