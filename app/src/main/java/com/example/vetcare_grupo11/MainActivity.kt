package com.example.vetcare_grupo11

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import androidx.navigation.compose.rememberNavController
import com.example.vetcare_grupo11.ui.AppNavigation

private val Teal = Color(0xFF00A9B9)
private val TealDark = Color(0xFF0093A2)
private val Coral = Color(0xFFFF6F61)
private val Soft = Color(0xFFE6F4F1)
private val Soft2 = Color(0xFFE5F1F1)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    secondary = Coral,
    onSecondary = Color.White,
    background = Soft,
    onBackground = TealDark,
    surface = Color.White, // Cards are white
    onSurface = TealDark,
    surfaceVariant = Soft2, // Bottom bar
    onSurfaceVariant = TealDark
)

private val DarkColors = darkColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    secondary = Coral,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E), // Slightly lighter than background for cards
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF242424), // Bottom bar
    onSurfaceVariant = Color(0xFFE0E0E0)
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sharedPreferences = getSharedPreferences("datos_app", MODE_PRIVATE)
            val isDarkTheme = sharedPreferences.getBoolean("tema_oscuro", false)
            val darkTheme = remember { mutableStateOf(isDarkTheme) }

            val colors = if (darkTheme.value) {
                DarkColors
            } else {
                LightColors
            }

            MaterialTheme(
                colorScheme = colors
            ) {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    darkTheme = darkTheme.value,
                    onThemeChange = {
                        darkTheme.value = it
                        sharedPreferences.edit {
                            putBoolean("tema_oscuro", it)
                            apply()
                        }
                    }
                )
            }
        }
    }
}
