package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.StatusAllowedGreen
import com.example.ui.theme.StatusAllowedGreenContainer
import com.example.ui.theme.StatusAnalyzingCyan
import com.example.ui.theme.StatusInsufficientGray
import com.example.ui.theme.StatusInsufficientGrayContainer
import com.example.ui.theme.StatusNotAllowedRed
import com.example.ui.theme.StatusNotAllowedRedContainer
import com.example.ui.theme.StatusUnclearYellow
import com.example.ui.theme.StatusUnclearYellowContainer

enum class ClassificationStatus(
    val id: String,
    val displayName: String,
    val badgeSymbol: String
) {
    ALLOWED("allowed", "Allowed", "🟢"),
    NOT_ALLOWED("not_allowed", "Not Allowed", "🔴"),
    UNCLEAR("unclear", "Unclear / Disputed", "🟡"),
    INSUFFICIENT_DATA("insufficient_data", "Insufficient Data", "⚪"),
    NOT_APPLICABLE("not_applicable", "Not Applicable", "📖"),
    UNANALYZED("unanalyzed", "Unanalyzed", "○"),
    ANALYZING("analyzing", "Analyzing...", "⟳"),
    FAILED("failed", "Analysis Failed", "⚠️");

    val color: Color
        get() = when (this) {
            ALLOWED -> StatusAllowedGreen
            NOT_ALLOWED -> StatusNotAllowedRed
            UNCLEAR -> StatusUnclearYellow
            INSUFFICIENT_DATA -> StatusInsufficientGray
            NOT_APPLICABLE -> IslamicGoldLight
            UNANALYZED -> Color(0xFF6B7280)
            ANALYZING -> StatusAnalyzingCyan
            FAILED -> StatusNotAllowedRed
        }

    val containerColor: Color
        get() = when (this) {
            ALLOWED -> StatusAllowedGreenContainer
            NOT_ALLOWED -> StatusNotAllowedRedContainer
            UNCLEAR -> StatusUnclearYellowContainer
            INSUFFICIENT_DATA -> StatusInsufficientGrayContainer
            NOT_APPLICABLE -> Color(0xFF2C2208)
            UNANALYZED -> Color(0xFF1F2937)
            ANALYZING -> Color(0xFF164E63)
            FAILED -> StatusNotAllowedRedContainer
        }

    fun toClassification(): Classification = when (this) {
        ALLOWED -> Classification.ALLOWED
        NOT_ALLOWED -> Classification.NOT_ALLOWED
        UNCLEAR -> Classification.UNCLEAR
        INSUFFICIENT_DATA -> Classification.INSUFFICIENT_DATA
        NOT_APPLICABLE -> Classification.NOT_APPLICABLE
        else -> Classification.INSUFFICIENT_DATA
    }

    companion object {
        fun fromClassification(classification: Classification): ClassificationStatus = when (classification) {
            Classification.ALLOWED -> ALLOWED
            Classification.NOT_ALLOWED -> NOT_ALLOWED
            Classification.UNCLEAR -> UNCLEAR
            Classification.INSUFFICIENT_DATA -> INSUFFICIENT_DATA
            Classification.NOT_APPLICABLE -> NOT_APPLICABLE
        }

        fun fromId(id: String?): ClassificationStatus {
            return when (id?.lowercase()?.trim()) {
                "allowed", "halal" -> ALLOWED
                "not_allowed", "haram", "prohibited" -> NOT_ALLOWED
                "unclear", "disputed", "doubtful" -> UNCLEAR
                "insufficient_data", "insufficient" -> INSUFFICIENT_DATA
                "not_applicable", "na", "n/a", "quran", "skipped" -> NOT_APPLICABLE
                "analyzing" -> ANALYZING
                "failed" -> FAILED
                else -> UNANALYZED
            }
        }
    }
}
