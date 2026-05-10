package com.brian.bubbleshooterclassicpop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brian.bubbleshooterclassicpop.game.GamePhase
import com.brian.bubbleshooterclassicpop.game.GameViewModel
import com.brian.bubbleshooterclassicpop.ui.GameOverScreen
import com.brian.bubbleshooterclassicpop.ui.GameScreen
import com.brian.bubbleshooterclassicpop.ui.StartScreen
import com.brian.bubbleshooterclassicpop.ui.WinScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BubbleShooterApp()
        }
    }
}

@Composable
private fun BubbleShooterApp(viewModel: GameViewModel = viewModel()) {
    val state = viewModel.state

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2E7BEF),
            secondary = Color(0xFF35C9A3),
            tertiary = Color(0xFFFF6B8A),
            background = Color(0xFFF7FAFF),
            surface = Color(0xFFFFFFFF),
            onPrimary = Color.White,
            onSecondary = Color(0xFF08362E),
            onBackground = Color(0xFF162033),
            onSurface = Color(0xFF162033),
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (state.phase) {
                GamePhase.Start -> StartScreen(onPlay = viewModel::play)
                GamePhase.Running,
                GamePhase.Paused,
                -> GameScreen(
                    state = state,
                    onAim = viewModel::aimAt,
                    onShoot = viewModel::shoot,
                    onTick = viewModel::tick,
                    onPause = viewModel::pauseOrResume,
                    onRestart = viewModel::restart,
                )

                GamePhase.Lost -> GameOverScreen(
                    score = state.score,
                    level = state.level,
                    onRestart = viewModel::restart,
                )

                GamePhase.Won -> WinScreen(
                    score = state.score,
                    level = state.level,
                    onNextLevel = viewModel::nextLevel,
                    onRestart = viewModel::restart,
                )
            }
        }
    }
}
