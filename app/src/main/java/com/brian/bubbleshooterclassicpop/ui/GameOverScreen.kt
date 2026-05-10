package com.brian.bubbleshooterclassicpop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameOverScreen(
    score: Int,
    level: Int,
    onRestart: () -> Unit,
) {
    EndStateScreen(
        title = "Game Over",
        subtitle = "Level $level",
        score = score,
        primaryText = "Restart",
        onPrimary = onRestart,
    )
}

@Composable
fun WinScreen(
    score: Int,
    level: Int,
    onNextLevel: () -> Unit,
    onRestart: () -> Unit,
) {
    EndStateScreen(
        title = "Board Cleared",
        subtitle = "Level $level Complete",
        score = score,
        primaryText = "Next Level",
        onPrimary = onNextLevel,
        secondaryText = "Restart",
        onSecondary = onRestart,
    )
}

@Composable
private fun EndStateScreen(
    title: String,
    subtitle: String,
    score: Int,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF7FAFF), Color(0xFFEAF3FF), Color(0xFFFFF5DD)),
                ),
            )
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            color = Color(0xFF162033),
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = Color(0xFF56627A),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "Score $score",
            color = Color(0xFF2E7BEF),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onPrimary) {
                Text(text = primaryText)
            }
            if (secondaryText != null && onSecondary != null) {
                OutlinedButton(onClick = onSecondary) {
                    Text(text = secondaryText)
                }
            }
        }
    }
}
