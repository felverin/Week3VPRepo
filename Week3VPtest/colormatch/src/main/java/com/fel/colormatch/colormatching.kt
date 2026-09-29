package com.fel.colormatch

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fel.colormatch.ui.theme.Week3VPtestTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val availableColors = listOf(
    ColorItem("RED", Color(0xFFE53935)),
    ColorItem("BLUE", Color(0xFF1E88E5)),
    ColorItem("GREEN", Color(0xFF43A047)),
    ColorItem("ORANGE", Color(0xFFFB8C00)),
    ColorItem("YELLOW", Color(0xFFFBC02D)),
    ColorItem("PURPLE", Color(0xFF8E24AA))
)

fun generateQuestion(): Question {
    val mode = MatchMode.entries.random()
    val wordItem = availableColors.random()
    // Choose a color item distinct from the word item to create a valid Stroop choice
    val colorItem = availableColors.filter { it.name != wordItem.name }.random()

    val correctAnswer = when (mode) {
        MatchMode.COLOR -> colorItem.name
        MatchMode.TEXT -> wordItem.name
    }

    // Both the word name and the ink color name will always be present in options
    val options = listOf(wordItem.name, colorItem.name).shuffled()

    return Question(
        mode = mode,
        wordItem = wordItem,
        colorItem = colorItem,
        options = options,
        correctAnswer = correctAnswer
    )
}

class colormatching : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Week3VPtestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DisplayColormatch(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DisplayColormatch(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("colormatch_prefs", Context.MODE_PRIVATE) }

    var stage by remember { mutableStateOf(GameStage.WELCOME) }
    var countdownText by remember { mutableStateOf("3") }

    var score by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(sharedPrefs.getInt("best_score", 0)) }

    var currentQuestion by remember { mutableStateOf<Question?>(null) }
    var questionIndex by remember { mutableIntStateOf(0) }
    var secondsLeft by remember { mutableIntStateOf(4) }
    var isAnswering by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Keyed on questionIndex and stage so Compose GUARANTEES a new timer for every single question
    LaunchedEffect(questionIndex, stage) {
        if (stage == GameStage.RUNNING && currentQuestion != null) {
            secondsLeft = 4
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft -= 1
            }
            // Timer expired without an answer
            if (!isAnswering && stage == GameStage.RUNNING) {
                isAnswering = true
                mistakes += 1
                if (mistakes >= 3) {
                    if (score > bestScore) {
                        bestScore = score
                        sharedPrefs.edit().putInt("best_score", bestScore).apply()
                    }
                    stage = GameStage.GAME_OVER
                } else {
                    isAnswering = false
                    questionIndex += 1
                    currentQuestion = generateQuestion()
                }
            }
        }
    }

    fun handleAnswer(selectedOption: String) {
        if (!isAnswering && stage == GameStage.RUNNING) {
            isAnswering = true
            val q = currentQuestion ?: return

            if (selectedOption == q.correctAnswer) {
                score += 1
            } else {
                mistakes += 1
            }

            if (mistakes >= 3) {
                if (score > bestScore) {
                    bestScore = score
                    sharedPrefs.edit().putInt("best_score", bestScore).apply()
                }
                stage = GameStage.GAME_OVER
            } else {
                isAnswering = false
                questionIndex += 1
                currentQuestion = generateQuestion()
            }
        }
    }

    fun startCountdownAndGame() {
        stage = GameStage.COUNTDOWN
        score = 0
        mistakes = 0
        isAnswering = false
        questionIndex = 0

        scope.launch {
            countdownText = "3"
            delay(1000)
            countdownText = "2"
            delay(1000)
            countdownText = "1"
            delay(1000)
            countdownText = "Start!"
            delay(800)

            stage = GameStage.RUNNING
            questionIndex = 1
            currentQuestion = generateQuestion()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        when (stage) {
            GameStage.WELCOME -> WelcomeScreen(onStartGame = { startCountdownAndGame() })
            GameStage.COUNTDOWN -> CountdownScreen(countdownText = countdownText)
            GameStage.RUNNING -> RunningGameScreen(
                question = currentQuestion,
                secondsLeft = secondsLeft,
                score = score,
                mistakes = mistakes,
                onOptionSelected = { handleAnswer(it) }
            )
            GameStage.GAME_OVER -> GameOverScreen(
                score = score,
                bestScore = bestScore,
                onRestart = { startCountdownAndGame() },
                onExit = { stage = GameStage.WELCOME }
            )
        }
    }
}

@Composable
fun WelcomeScreen(onStartGame: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome\nto\nColor Word Matching",
            fontSize = 26.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF222222),
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onStartGame,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8C3CE)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .width(160.dp)
                .height(46.dp)
        ) {
            Text(
                text = "Start Game",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF222222)
            )
        }
    }
}

@Composable
fun CountdownScreen(countdownText: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = countdownText,
            fontSize = 36.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF222222),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RunningGameScreen(
    question: Question?,
    secondsLeft: Int,
    score: Int,
    mistakes: Int,
    onOptionSelected: (String) -> Unit
) {
    val q = question ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Mode: ${q.mode.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = if (q.mode == MatchMode.COLOR) "(Match Ink Color)" else "(Match Word Text)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✅ $score",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "❌ $mistakes/3",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF333333)
                )
            }
        }

        // Center Word & Timer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$secondsLeft s",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = q.wordItem.name,
                fontSize = 54.sp,
                fontWeight = FontWeight.Normal,
                color = q.colorItem.color,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Option Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            q.options.forEach { option ->
                Button(
                    onClick = { onOptionSelected(option) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8C3CE)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .width(130.dp)
                        .height(52.dp)
                ) {
                    Text(
                        text = option,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF222222)
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    bestScore: Int,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Over!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF222222)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "You're Score",
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF444444)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$score",
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF222222)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Best Score",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$bestScore",
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF222222)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onRestart,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8C3CE)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .width(170.dp)
                .height(46.dp)
        ) {
            Text(
                text = "Restart Game",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF222222)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onExit,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8C3CE)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .width(100.dp)
                .height(40.dp)
        ) {
            Text(
                text = "Exit",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF222222)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    Week3VPtestTheme {
        DisplayColormatch()
    }
}
