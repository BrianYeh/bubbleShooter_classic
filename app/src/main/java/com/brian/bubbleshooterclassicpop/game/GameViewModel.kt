package com.brian.bubbleshooterclassicpop.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    private val engine = GameEngine()

    var state by mutableStateOf(engine.state)
        private set

    fun play() {
        engine.start()
        sync()
    }

    fun restart() {
        engine.restart()
        sync()
    }

    fun nextLevel() {
        engine.nextLevel()
        sync()
    }

    fun pauseOrResume() {
        engine.togglePause()
        sync()
    }

    fun aimAt(target: Vec2) {
        engine.setAimTarget(target)
        sync()
    }

    fun shoot() {
        engine.shoot()
        sync()
    }

    fun tick(deltaSeconds: Float) {
        engine.tick(deltaSeconds)
        sync()
    }

    private fun sync() {
        state = engine.state
    }
}
