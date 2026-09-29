package com.fel.kittyyyyyyy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fel.kittyyyyyyy.ui.theme.Week3VPtestTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class kitty : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Week3VPtestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DisplayGame(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

fun formatVal(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else if ((value * 10) % 1.0 == 0.0) {
        String.format(Locale.US, "%.1f", value)
    } else {
        String.format(Locale.US, "%.2f", value)
    }
}

@Composable
fun DisplayGame(
    modifier: Modifier = Modifier
) {
    var coins by remember { mutableDoubleStateOf(0.0) }
    var coinsPerTap by remember { mutableDoubleStateOf(1.0) }
    var upgradeCost by remember { mutableDoubleStateOf(10.0) }
    var isCatOpen by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var closeMouthJob by remember { mutableStateOf<Job?>(null) }

    fun tapCat() {
        coins += coinsPerTap
        isCatOpen = true
        // If spammed, cancel previous timer so cat stays open
        closeMouthJob?.cancel()
        closeMouthJob = scope.launch {
            delay(400) // Returns to closed mouth only after 400ms without clicks
            isCatOpen = false
        }
    }

    val nextCoinsPerTap = coinsPerTap * 1.5
    val canUpgrade = coins >= upgradeCost
    val coinsNeeded = (upgradeCost - coins).coerceAtLeast(0.0)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Full screen background
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Kitty Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Dark scrim overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Coins Counter Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Coins",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatVal(coins),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFEB3B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatVal(coinsPerTap)} coins per tap",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Middle Section: Tap the Cat
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tap the Cat!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .size(190.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            tapCat()
                        }
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isCatOpen) R.drawable.legasp else R.drawable.shutup
                        ),
                        contentDescription = "Cat",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Purr~",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            // Bottom Section: Upgrade Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Give Me Your Coin",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Next upgrade: ${formatVal(nextCoinsPerTap)} coins per tap",
                        fontSize = 14.sp,
                        color = Color(0xFF777777)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (canUpgrade) {
                                coins -= upgradeCost
                                coinsPerTap *= 1.5
                                upgradeCost *= 2.0
                            }
                        },
                        enabled = canUpgrade,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color(0xFFCCCCCC)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (canUpgrade) "Upgrade to ${formatVal(nextCoinsPerTap)} coins/tap" else "Find ${formatVal(coinsNeeded)} more coins",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canUpgrade) Color.White else Color(0xFF777777),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    Week3VPtestTheme {
        DisplayGame()
    }
}
