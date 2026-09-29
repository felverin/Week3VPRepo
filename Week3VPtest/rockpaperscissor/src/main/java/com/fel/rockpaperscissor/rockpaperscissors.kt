package com.fel.rockpaperscissor

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
import com.fel.rockpaperscissor.ui.theme.Week3VPtestTheme
import kotlinx.coroutines.delay

class rockpaperscissor : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Week3VPtestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DisplayRPS(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DisplayRPS(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("rps_prefs", Context.MODE_PRIVATE) }

    val targetWins = 3 // Best of 5 mode -> First to 3 wins

    var stage by remember { mutableStateOf(GameStage.INITIAL) }
    var playerScore by remember { mutableIntStateOf(0) }
    var cpuScore by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(sharedPrefs.getInt("best_score", 0)) }

    var moveOptions by remember { mutableStateOf(Move.entries.shuffled()) }
    var lastPlayerMove by remember { mutableStateOf<Move?>(null) }
    var lastCpuMove by remember { mutableStateOf<Move?>(null) }
    var lastRoundResult by remember { mutableStateOf<RoundResult?>(null) }
    var isProcessingRound by remember { mutableStateOf(false) }

    // Managed reveal timer so CPU win and Player win both transition smoothly
    LaunchedEffect(stage) {
        if (stage == GameStage.REVEAL) {
            delay(900)
            if (playerScore >= targetWins || cpuScore >= targetWins) {
                if (playerScore > bestScore) {
                    bestScore = playerScore
                    sharedPrefs.edit().putInt("best_score", bestScore).apply()
                }
                stage = GameStage.MATCH_END
            } else {
                moveOptions = Move.entries.shuffled()
                stage = GameStage.PICK
            }
            isProcessingRound = false
        }
    }

    fun playRound(playerMove: Move) {
        if (isProcessingRound || stage != GameStage.PICK) return
        isProcessingRound = true

        val cpuMove = Move.entries.random()
        val result = when {
            playerMove == cpuMove -> RoundResult.DRAW
            playerMove.beats(cpuMove) -> RoundResult.WIN
            else -> RoundResult.LOSE
        }

        lastPlayerMove = playerMove
        lastCpuMove = cpuMove
        lastRoundResult = result

        if (result == RoundResult.WIN) {
            playerScore += 1
        } else if (result == RoundResult.LOSE) {
            cpuScore += 1
        }

        stage = GameStage.REVEAL
    }

    fun startNewGame() {
        playerScore = 0
        cpuScore = 0
        isProcessingRound = false
        lastPlayerMove = null
        lastCpuMove = null
        lastRoundResult = null
        moveOptions = Move.entries.shuffled()
        stage = GameStage.PICK
    }

    fun resetToInitial() {
        playerScore = 0
        cpuScore = 0
        isProcessingRound = false
        lastPlayerMove = null
        lastCpuMove = null
        lastRoundResult = null
        stage = GameStage.INITIAL
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Score & Mode
            HeaderBar(
                playerScore = playerScore,
                cpuScore = cpuScore
            )

            // Content Body Based on Stage
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (stage) {
                    GameStage.INITIAL -> InitialContent(onStart = { startNewGame() })
                    GameStage.PICK -> PickContent(
                        moveOptions = moveOptions,
                        onMoveSelected = { playRound(it) }
                    )
                    GameStage.REVEAL -> RevealContent(
                        playerMove = lastPlayerMove,
                        cpuMove = lastCpuMove,
                        roundResult = lastRoundResult
                    )
                    GameStage.MATCH_END -> MatchEndContent(
                        playerScore = playerScore,
                        cpuScore = cpuScore,
                        bestScore = bestScore,
                        onRestart = { startNewGame() },
                        onExit = { resetToInitial() }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderBar(
    playerScore: Int,
    cpuScore: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🧑 $playerScore - $cpuScore 🤖",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
        Text(
            text = "Best of 5",
            fontSize = 15.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun InitialContent(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Rock • Paper • Scissors",
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF222222),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8CBD6)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .width(160.dp)
                .height(46.dp)
        ) {
            Text(
                text = "Start",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF222222)
            )
        }
    }
}

@Composable
fun PickContent(
    moveOptions: List<Move>,
    onMoveSelected: (Move) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Center VS Area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pick your move!",
                fontSize = 18.sp,
                color = Color(0xFF555555)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "❓  VS  ❓",
                fontSize = 36.sp,
                color = Color(0xFF888888)
            )
        }

        // 3 Randomized Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            moveOptions.forEach { move ->
                Button(
                    onClick = { onMoveSelected(move) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8CBD6)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .width(105.dp)
                        .height(44.dp)
                ) {
                    Text(
                        text = "${move.emoji} ${move.label}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF222222)
                    )
                }
            }
        }
    }
}

@Composable
fun RevealContent(
    playerMove: Move?,
    cpuMove: Move?,
    roundResult: RoundResult?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = playerMove?.emoji ?: "❓",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "VS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = cpuMove?.emoji ?: "❓",
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val resultText = when (roundResult) {
            RoundResult.WIN -> "You Win!"
            RoundResult.LOSE -> "You Lose!"
            RoundResult.DRAW -> "Draw"
            null -> ""
        }

        Text(
            text = resultText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF333333)
        )
    }
}

@Composable
fun MatchEndContent(
    playerScore: Int,
    cpuScore: Int,
    bestScore: Int,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    val titleText = if (playerScore > cpuScore) "You Win the Match!" else "CPU Wins the Match!"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = titleText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF222222),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Best Score: $bestScore",
            fontSize = 16.sp,
            color = Color(0xFF555555)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8CBD6)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .width(120.dp)
                    .height(46.dp)
            ) {
                Text(
                    text = "Restart",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF222222)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8CBD6)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .width(100.dp)
                    .height(46.dp)
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    Week3VPtestTheme {
        DisplayRPS()
    }
}
