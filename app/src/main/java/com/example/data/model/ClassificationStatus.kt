package com.example.data.model

import androidx.compose.ui.graphics.Color
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
    UNANALYZED("unanalyzed", "Unanalyzed", "○"),
    ANALYZING("analyzing", "Analyzing...", "⟳"),
    FAILED("failed", "Analysis Failed", "⚠️");

    val color: Color
        get() = when (this) {
            ALLOWED -> StatusAllowedGreen
            NOT_ALLOWED -> StatusNotAllowedRed
            UNCLEAR -> StatusUnclearYellow
            INSUFFICIENT_DATA -> StatusInsufficientGray
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
            UNANALYZED -> Color(0xFF1F2937)
            ANALYZING -> Color(0xFF164E63)
            FAILED -> StatusNotAllowedRedContainer
        }

    companion object {
        fun fromId(id: String?): ClassificationStatus {
            return when (id?.lowercase()?.trim()) {
                "allowed", "halal" -> ALLOWED
                "not_allowed", "haram", "prohibited" -> NOT_ALLOWED
                "unclear", "disputed", "doubtful" -> UNCLEAR
                "insufficient_data", "insufficient" -> INSUFFICIENT_DATA
                "analyzing" -> ANALYZING
                "failed" -> FAILED
                else -> UNANALYZED
            }
        }
    }
}
