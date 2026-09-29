package com.fel.reactiontest

enum class ReactionCategory(
    val maxMs: Long,
    val message: String
) {
    RESPECTFUL(
        maxMs = 180,
        message = "DANG YOU ARE SO FAST BRO!"
    ),
    THUMBS_UP(
        maxMs = 280,
        message = "YOUR REFLEX IS GOOD"
    ),
    STANDARD(
        maxMs = 450,
        message = "MEH LIKE OTHER PERSON"
    ),
    SNAIL(
        maxMs = Long.MAX_VALUE,
        message = "YOU LIKE A SNAIL BRO"
    );

    companion object {
        fun fromAverageTime(averageMs: Long): ReactionCategory {
            return entries.first { averageMs < it.maxMs }
        }
    }
}