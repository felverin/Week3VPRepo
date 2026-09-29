package com.fel.reactiontest

sealed class TrialResult {
    data class Success(val timeMs: Long) : TrialResult()
    object Failed : TrialResult()
}