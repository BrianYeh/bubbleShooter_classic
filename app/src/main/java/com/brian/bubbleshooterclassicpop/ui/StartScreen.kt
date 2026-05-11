package com.brian.bubbleshooterclassicpop.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StartScreen(onPlay: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF7FAFF), Color(0xFFE9FFF7), Color(0xFFFFF6D7)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bubbles = listOf(
                Triple(Offset(size.width * 0.18f, size.height * 0.14f), 34f, Color(0xFFFF6B8A)),
                Triple(Offset(size.width * 0.82f, size.height * 0.18f), 28f, Color(0xFF55A7FF)),
                Triple(Offset(size.width * 0.14f, size.height * 0.78f), 24f, Color(0xFF35C9A3)),
                Triple(Offset(size.width * 0.78f, size.height * 0.72f), 38f, Color(0xFFFFD166)),
            )
            bubbles.forEach { (center, radius, color) ->
                drawCircle(color.copy(alpha = 0.22f), radius = radius, center = center)
                drawCircle(Color.White.copy(alpha = 0.5f), radius = radius * 0.24f, center = center - Offset(radius * 0.28f, radius * 0.32f))
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BubbleClusterLogo()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bubble Shooter\nClassic Pop",
                color = Color(0xFF162033),
                fontSize = 38.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Aim with a drag or tap, bounce shots from the walls, and match 3 or more bubbles to clear the board.",
                color = Color(0xFF44506A),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(onClick = onPlay) {
                Text(text = "Select Level")
            }
        }
    }
}

@Composable
private fun BubbleClusterLogo() {
    Canvas(modifier = Modifier.size(132.dp)) {
        val radius = size.minDimension * 0.18f
        val centers = listOf(
            Offset(size.width * 0.50f, size.height * 0.24f) to Color(0xFFFFD166),
            Offset(size.width * 0.34f, size.height * 0.47f) to Color(0xFFFF6B8A),
            Offset(size.width * 0.66f, size.height * 0.47f) to Color(0xFF55A7FF),
            Offset(size.width * 0.50f, size.height * 0.70f) to Color(0xFF35C9A3),
            Offset(size.width * 0.22f, size.height * 0.70f) to Color(0xFF9D7CFF),
            Offset(size.width * 0.78f, size.height * 0.70f) to Color(0xFFFF8E5E),
        )
        centers.forEach { (center, color) ->
            drawCircle(color = color, radius = radius, center = center)
            drawCircle(
                color = Color.White.copy(alpha = 0.65f),
                radius = radius * 0.28f,
                center = center - Offset(radius * 0.32f, radius * 0.36f),
            )
        }
    }
}
