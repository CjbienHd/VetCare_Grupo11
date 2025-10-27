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

// ----------------------
// Paleta de colores global
// ----------------------
// colores principales (Teal y Coral) y dos tonos suaves de fondo.
// La app tiene versión clara y oscura para mostrar adaptación visual (Settings -> Cambiar tema).
//
private val Teal = Color(0xFF00A9B9)
private val TealDark = Color(0xFF0093A2)
private val Coral = Color(0xFFFF6F61)
private val Soft = Color(0xFFE6F4F1)
private val Soft2 = Color(0xFFE5F1F1)

// Tema claro (por defecto)
private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    secondary = Coral,
    onSecondary = Color.White,
    background = Soft,
    onBackground = TealDark,
    surface = Color.White,
    onSurface = TealDark,
    surfaceVariant = Soft2,
    onSurfaceVariant = TealDark
)

// Tema oscuro (activable desde SettingsScreen)
private val DarkColors = darkColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    secondary = Coral,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFFE0E0E0)
)


// ----------------------
// Punto de entrada: MainActivity
// ----------------------

// - Es el punto de arranque de la aplicación
// - Aquí inicializo el tema (claro u oscuro) y el controlador de navegación.
// - La función setContent() carga la interfaz usando Jetpack Compose.
//
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent: reemplaza el XML tradicional. Todo el contenido se compone desde aquí.
        setContent {
            // Leo si el usuario dejó guardado el modo oscuro en SharedPreferences
            val sharedPreferences = getSharedPreferences("datos_app", MODE_PRIVATE)
            val isDarkTheme = sharedPreferences.getBoolean("tema_oscuro", false)

            // Variable reactiva que mantiene el estado del tema (oscuro / claro)
            val darkTheme = remember { mutableStateOf(isDarkTheme) }

            // Selecciono el esquema de color según la preferencia actual
            val colors = if (darkTheme.value) {
                DarkColors
            } else {
                LightColors
            }

            // Aplico el tema elegido a toda la app
            MaterialTheme(
                colorScheme = colors
            ) {
                // Inicializo el controlador de navegación de Compose (maneja las rutas entre pantallas)
                val navController = rememberNavController()

                // Llamo a AppNavigation, que contiene el NavHost con todas las pantallas (login, main, settings, etc.)
                AppNavigation(
                    navController = navController,
                    darkTheme = darkTheme.value,
                    onThemeChange = {
                        // Esta función se pasa a SettingsScreen para alternar el tema desde ahí
                        darkTheme.value = it

                        // Guardo la preferencia para que se mantenga entre sesiones
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
