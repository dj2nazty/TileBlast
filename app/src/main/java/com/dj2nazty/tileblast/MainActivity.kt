package com.dj2nazty.tileblast

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
import com.dj2nazty.tileblast.ads.AdManager
import com.dj2nazty.tileblast.ui.GameScreen
import com.dj2nazty.tileblast.ui.theme.Bg
import com.dj2nazty.tileblast.ui.theme.TileBlastTheme

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
