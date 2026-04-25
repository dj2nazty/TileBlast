package com.tileblast.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tileblast.game.ads.AdManager
import com.tileblast.game.ui.GameScreen
import com.tileblast.game.ui.theme.Bg
import com.tileblast.game.ui.theme.TileBlastTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdManager.init(applicationContext)
        setContent {
            TileBlastTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Bg)
                        .padding(WindowInsets.safeDrawing.asPaddingValues()),
                    color = Bg,
                ) {
                    GameScreen()
                }
            }
        }
    }
}
