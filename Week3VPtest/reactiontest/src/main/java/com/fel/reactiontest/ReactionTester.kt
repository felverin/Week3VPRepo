package com.fel.reactiontest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fel.reactiontest.ui.theme.Week3VPtestTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class ReactionTester : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Week3VPtestTheme {
                ReactionApp()
            }
        }
    }
}

@Composable
fun ReactionApp() {
    val totalTrials = 3
    var currentTrial by remember { mutableIntStateOf(1) }
    var gameState by remember { mutableStateOf(GameState.START) }
    val trialResults = remember { mutableStateListOf<TrialResult>() }

    var startTimeMs by remember { mutableLongStateOf(0L) }
    var lastReactionTimeMs by remember { mutableLongStateOf(0L) }

    val scope = rememberCoroutineScope()
    var waitingJob by remember { mutableStateOf<Job?>(null) }

    // Start a trial flow
    fun startTrial() {
        gameState = GameState.WAITING
        waitingJob?.cancel()
        waitingJob = scope.launch {
            val randomDelay = Random.nextLong(500, 4500) // 0.5s to 4.5s
            delay(randomDelay)
            startTimeMs = System.currentTimeMillis()
            gameState = GameState.READY
        }
    }

    // Handle user tap on screen
    fun handleScreenTap() {
        when (gameState) {
            GameState.START -> {
                trialResults.clear()
                currentTrial = 1
                startTrial()
            }
            GameState.WAITING -> {
                waitingJob?.cancel()
                trialResults.add(TrialResult.Failed)
                gameState = GameState.TOO_EARLY
            }
            GameState.READY -> {
                val endTimeMs = System.currentTimeMillis()
                val reactionTime = endTimeMs - startTimeMs
                lastReactionTimeMs = reactionTime
                trialResults.add(TrialResult.Success(reactionTime))
                gameState = GameState.TRIAL_COMPLETE
            }
            GameState.TOO_EARLY, GameState.TRIAL_COMPLETE -> {
                if (currentTrial < totalTrials) {
                    currentTrial += 1
                    startTrial()
                } else {
                    gameState = GameState.FINISHED
                }
            }
            GameState.FINISHED -> {
                // Reset game
                trialResults.clear()
                currentTrial = 1
                gameState = GameState.START
            }
        }
    }

    val successfulTrials = trialResults.filterIsInstance<TrialResult.Success>()
    val averageMs = if (successfulTrials.isNotEmpty()) {
        successfulTrials.map { it.timeMs }.average().toLong()
    } else {
        9999L
    }
    val category = ReactionCategory.fromAverageTime(averageMs)

    // Dynamic background color matching mockups
    val backgroundColor = when (gameState) {
        GameState.START -> Color(0xFF4AC3A7)
        GameState.WAITING -> Color(0xFFD0D0D0)
        GameState.READY -> Color(0xFF4CAF50)
        GameState.TOO_EARLY -> Color(0xFFFF1100)
        GameState.TRIAL_COMPLETE -> Color(0xFF4CAF50)
        GameState.FINISHED -> when (category) {
            ReactionCategory.RESPECTFUL -> Color(0xFF00C853)
            ReactionCategory.THUMBS_UP -> Color(0xFF2196F3)
            ReactionCategory.STANDARD -> Color(0xFFFF9800)
            ReactionCategory.SNAIL -> Color(0xFFFF3B1D)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                handleScreenTap()
            },
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (gameState) {
                GameState.START -> StartScreenContent()
                GameState.WAITING -> WaitingScreenContent()
                GameState.READY -> ReadyScreenContent()
                GameState.TOO_EARLY -> TooEarlyScreenContent(currentTrial, totalTrials, trialResults)
                GameState.TRIAL_COMPLETE -> TrialCompleteScreenContent(currentTrial, totalTrials, lastReactionTimeMs, trialResults)
                GameState.FINISHED -> FinishedScreenContent(
                    trialResults = trialResults,
                    averageMs = averageMs,
                    category = category
                )
            }
        }
    }
}

@Composable
fun StartScreenContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Reaction",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(36.dp))
        Image(
            painter = painterResource(id = R.drawable.bolt),
            contentDescription = "Bolt",
            modifier = Modifier.size(110.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "Test",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Click to Start",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun WaitingScreenContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Get Ready",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(36.dp))
        Image(
            painter = painterResource(id = R.drawable.warning),
            contentDescription = "Warning",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "Wait for green light...",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "DON'T CLICK YET!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun ReadyScreenContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GO!",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(36.dp))
        Image(
            painter = painterResource(id = R.drawable.run),
            contentDescription = "Run",
            modifier = Modifier.size(140.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "CLICK NOW!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "TAP AS FAST AS YOU CAN!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun TooEarlyScreenContent(currentTrial: Int, totalTrials: Int, trialResults: List<TrialResult>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Too Early!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(36.dp))
        Image(
            painter = painterResource(id = R.drawable.thumbdown),
            contentDescription = "bad",
            modifier = Modifier.size(140.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "FAILED",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (currentTrial < totalTrials) "Continue to Trial ${currentTrial + 1}" else "View Results",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        TrialResultsCard(trialResults = trialResults)
    }
}

@Composable
fun TrialCompleteScreenContent(
    currentTrial: Int,
    totalTrials: Int,
    reactionTimeMs: Long,
    trialResults: List<TrialResult>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Trial $currentTrial Complete!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        GreenCheckmarkIcon()
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Time: ${reactionTimeMs}ms",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (currentTrial < totalTrials) "Continue to Trial ${currentTrial + 1}" else "View Results",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        TrialResultsCard(trialResults = trialResults)
    }
}

@Composable
fun FinishedScreenContent(
    trialResults: List<TrialResult>,
    averageMs: Long,
    category: ReactionCategory
) {
    val themeColor = when (category) {
        ReactionCategory.RESPECTFUL -> Color(0xFF00C853)
        ReactionCategory.THUMBS_UP -> Color(0xFF2196F3)
        ReactionCategory.STANDARD -> Color(0xFFFF9800)
        ReactionCategory.SNAIL -> Color(0xFFFF3B1D)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = category.message,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Category Person Illustration
        CategoryPersonVisual(category = category)

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = if (averageMs < 9000L) "Average: ${averageMs}ms" else "All Trials Failed!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Click to Start New Test",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Floating Results Card with Average Score
        TrialResultsCard(
            trialResults = trialResults,
            averageMs = if (averageMs < 9000L) averageMs else null,
            themeColor = themeColor
        )
    }
}

@Composable
fun GreenCheckmarkIcon() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(60.dp)) {
            val strokeWidth = 14f
            val path = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.5f)
                lineTo(size.width * 0.42f, size.height * 0.72f)
                lineTo(size.width * 0.82f, size.height * 0.28f)
            }
            drawPath(
                path = path,
                color = Color(0xFF4CAF50),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
fun TrialResultsCard(
    trialResults: List<TrialResult>,
    averageMs: Long? = null,
    themeColor: Color = Color(0xFF2196F3)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F8F5)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Trial Results",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Headers 1, 2, 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf(1, 2, 3).forEach { num ->
                    Text(
                        text = "$num",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Trial Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (i in 0..2) {
                    val resultText = if (i < trialResults.size) {
                        when (val r = trialResults[i]) {
                            is TrialResult.Success -> "${r.timeMs}ms"
                            is TrialResult.Failed -> "FAILED"
                        }
                    } else {
                        "-"
                    }
                    Text(
                        text = resultText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (resultText == "FAILED") Color.Red else Color(0xFF333333)
                    )
                }
            }

            // Average Score section for Final Results
            if (averageMs != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Average Score",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${averageMs}ms",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = themeColor
                )
            }
        }
    }
}

@Composable
fun CategoryPersonVisual(category: ReactionCategory) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(
                when (category) {
                    ReactionCategory.RESPECTFUL -> Color(0xFF00C853)
                    ReactionCategory.THUMBS_UP -> Color(0xFF2196F3)
                    ReactionCategory.STANDARD -> Color(0xFFFF9800)
                    ReactionCategory.SNAIL -> Color(0xFFFF3B1D)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            when (category) {
                ReactionCategory.RESPECTFUL -> {
                    // Respectful / Saluting Person
                    drawCircle(
                        color = Color.White,
                        radius = canvasWidth * 0.25f,
                        center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f)
                    )
                    // Closed respectful eyes
                    drawArc(
                        color = Color(0xFF00C853),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(canvasWidth * 0.38f, canvasHeight * 0.40f),
                        size = Size(canvasWidth * 0.08f, canvasHeight * 0.08f),
                        style = Stroke(width = 4f)
                    )
                    drawArc(
                        color = Color(0xFF00C853),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(canvasWidth * 0.54f, canvasHeight * 0.40f),
                        size = Size(canvasWidth * 0.08f, canvasHeight * 0.08f),
                        style = Stroke(width = 4f)
                    )
                    // Smile
                    drawArc(
                        color = Color(0xFF00C853),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(canvasWidth * 0.42f, canvasHeight * 0.52f),
                        size = Size(canvasWidth * 0.16f, canvasHeight * 0.10f),
                        style = Stroke(width = 4f)
                    )
                    // Salute Arm
                    val salutePath = Path().apply {
                        moveTo(canvasWidth * 0.65f, canvasHeight * 0.42f)
                        lineTo(canvasWidth * 0.85f, canvasHeight * 0.30f)
                        lineTo(canvasWidth * 0.70f, canvasHeight * 0.28f)
                    }
                    drawPath(
                        salutePath,
                        Color.White,
                        style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                ReactionCategory.THUMBS_UP -> {
                    // Thumbs Up Person
                    drawCircle(
                        color = Color.White,
                        radius = canvasWidth * 0.25f,
                        center = Offset(canvasWidth * 0.42f, canvasHeight * 0.45f)
                    )
                    drawCircle(Color(0xFF2196F3), radius = 5f, center = Offset(canvasWidth * 0.35f, canvasHeight * 0.42f))
                    drawCircle(Color(0xFF2196F3), radius = 5f, center = Offset(canvasWidth * 0.49f, canvasHeight * 0.42f))
                    drawArc(
                        color = Color(0xFF2196F3),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(canvasWidth * 0.35f, canvasHeight * 0.50f),
                        size = Size(canvasWidth * 0.14f, canvasHeight * 0.12f),
                        style = Stroke(width = 5f)
                    )
                    // Thumbs Up Icon
                    val thumbPath = Path().apply {
                        moveTo(canvasWidth * 0.72f, canvasHeight * 0.45f)
                        lineTo(canvasWidth * 0.72f, canvasHeight * 0.25f)
                        lineTo(canvasWidth * 0.80f, canvasHeight * 0.25f)
                        lineTo(canvasWidth * 0.80f, canvasHeight * 0.45f)
                        lineTo(canvasWidth * 0.90f, canvasHeight * 0.52f)
                        lineTo(canvasWidth * 0.90f, canvasHeight * 0.75f)
                        lineTo(canvasWidth * 0.68f, canvasHeight * 0.75f)
                        lineTo(canvasWidth * 0.68f, canvasHeight * 0.52f)
                        close()
                    }
                    drawPath(thumbPath, Color(0xFFFEF08A))
                }

                ReactionCategory.STANDARD -> {
                    // Standard Person (Flat hand gesture)
                    drawCircle(
                        color = Color.White,
                        radius = canvasWidth * 0.28f,
                        center = Offset(canvasWidth * 0.50f, canvasHeight * 0.48f)
                    )
                    drawCircle(Color(0xFFFF9800), radius = 6f, center = Offset(canvasWidth * 0.40f, canvasHeight * 0.44f))
                    drawCircle(Color(0xFFFF9800), radius = 6f, center = Offset(canvasWidth * 0.60f, canvasHeight * 0.44f))
                    drawLine(
                        color = Color(0xFFFF9800),
                        start = Offset(canvasWidth * 0.42f, canvasHeight * 0.58f),
                        end = Offset(canvasWidth * 0.58f, canvasHeight * 0.58f),
                        strokeWidth = 5f
                    )
                }

                ReactionCategory.SNAIL -> {
                    // Snail Person with Thumbs Down
                    drawCircle(
                        color = Color.White,
                        radius = canvasWidth * 0.28f,
                        center = Offset(canvasWidth * 0.45f, canvasHeight * 0.48f)
                    )
                    drawCircle(Color(0xFFFF3B1D), radius = 6f, center = Offset(canvasWidth * 0.38f, canvasHeight * 0.44f))
                    drawCircle(Color(0xFFFF3B1D), radius = 6f, center = Offset(canvasWidth * 0.52f, canvasHeight * 0.44f))
                    // Playful/Sassy tongue
                    drawArc(
                        color = Color(0xFFFF3B1D),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(canvasWidth * 0.40f, canvasHeight * 0.54f),
                        size = Size(canvasWidth * 0.12f, canvasHeight * 0.10f)
                    )
                    // Thumbs Down Icon
                    val thumbDownPath = Path().apply {
                        moveTo(canvasWidth * 0.75f, canvasHeight * 0.55f)
                        lineTo(canvasWidth * 0.75f, canvasHeight * 0.75f)
                        lineTo(canvasWidth * 0.83f, canvasHeight * 0.75f)
                        lineTo(canvasWidth * 0.83f, canvasHeight * 0.55f)
                        lineTo(canvasWidth * 0.93f, canvasHeight * 0.48f)
                        lineTo(canvasWidth * 0.93f, canvasHeight * 0.25f)
                        lineTo(canvasWidth * 0.71f, canvasHeight * 0.25f)
                        lineTo(canvasWidth * 0.71f, canvasHeight * 0.48f)
                        close()
                    }
                    drawPath(thumbDownPath, Color(0xFFFEF08A))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DisplayScreenPreview() {
    Week3VPtestTheme {
        ReactionApp()
    }
}
