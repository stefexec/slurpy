package dev.gurk.slurpy

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import dev.gurk.slurpy.ui.HomeScreen
import dev.gurk.slurpy.ui.SettingsScreen
import dev.gurk.slurpy.ui.SlurpyTheme
import dev.gurk.slurpy.ui.TimerScreen

enum class Screen {
    Home, Settings
}

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val timerState = viewModel.timer

            // Keep screen on while timer is active
            DisposableEffect(timerState) {
                if (timerState != null) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            SlurpyTheme(theme = viewModel.theme) {
                var currentScreen by remember { mutableStateOf(Screen.Home) }

                if (!viewModel.hasSeenSupportPrompt) {
                    dev.gurk.slurpy.ui.SupportDialog(
                        onDismiss = { viewModel.dismissSupportPrompt() }
                    )
                }

                if (timerState != null) {
                    BackHandler {
                        viewModel.stopTimer()
                    }
                    TimerScreen(
                        viewModel = viewModel,
                        timerState = timerState,
                    )
                } else {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screenTransition"
                    ) { screen ->
                        when (screen) {
                            Screen.Home -> HomeScreen(
                                viewModel = viewModel,
                                onOpenSettings = { currentScreen = Screen.Settings },
                            )
                            Screen.Settings -> {
                                BackHandler {
                                    currentScreen = Screen.Home
                                }
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.Home },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
