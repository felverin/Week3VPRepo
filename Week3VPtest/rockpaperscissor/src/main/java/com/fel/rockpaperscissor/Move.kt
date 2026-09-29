package com.fel.rockpaperscissor

enum class Move(val label: String, val emoji: String) {
    ROCK("Rock", "✊"),
    PAPER("Paper", "✋"),
    SCISSORS("Scissor", "✌️");

    fun beats(other: Move): Boolean {
        return when (this) {
            ROCK -> other == SCISSORS
            PAPER -> other == ROCK
            SCISSORS -> other == PAPER
        }
    }
}