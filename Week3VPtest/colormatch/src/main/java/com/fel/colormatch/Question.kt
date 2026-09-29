package com.fel.colormatch

data class Question(
    val mode: MatchMode,
    val wordItem: ColorItem,
    val colorItem: ColorItem,
    val options: List<String>,
    val correctAnswer: String
)
