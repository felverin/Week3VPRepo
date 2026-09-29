package com.fel.reactiontest

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.fel.reactiontest.ui.theme.Week3VPtestTheme

class DisplayScreenScreenshotTest {

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun StartScreenPreviewTest() {
        Week3VPtestTheme {
            StartScreenContent()
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun WaitingScreenPreviewTest() {
        Week3VPtestTheme {
            WaitingScreenContent()
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun ReadyScreenPreviewTest() {
        Week3VPtestTheme {
            ReadyScreenContent()
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun TrialCompleteScreenPreviewTest() {
        Week3VPtestTheme {
            TrialCompleteScreenContent(
                currentTrial = 1,
                totalTrials = 3,
                reactionTimeMs = 1579,
                trialResults = listOf(TrialResult.Success(1579))
            )
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun FinishedScreenPreviewTest() {
        Week3VPtestTheme {
            FinishedScreenContent(
                trialResults = listOf(
                    TrialResult.Success(1579),
                    TrialResult.Success(557),
                    TrialResult.Success(460)
                ),
                averageMs = 865,
                category = ReactionCategory.SNAIL
            )
        }
    }
}
