package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.MezmurMainScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.ChurchBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MezmurViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: MezmurViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var showSplash by remember { mutableStateOf(true) }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = ChurchBackground
        ) {
          if (showSplash) {
            SplashScreen(onTimeout = { showSplash = false })
          } else {
            MezmurMainScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}

