@file:OptIn(ExperimentalAnimationApi::class)

package com.example.vetcare_grupo11.ui

// Nota: tengo imports de viewModel dos veces más abajo; funciona igual, pero podría limpiar.
// Idea clave: esta pantalla define navegación + transiciones animadas + tema fijo claro + VM compartido.

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vetcare_grupo11.viewmodel.PatientsViewModel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel // (duplicado, recordar limpieza)
import com.example.vetcare_grupo11.data.SharedPrefsPatientsStore
import com.example.vetcare_grupo11.viewmodel.PatientsViewModelFactory

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Paleta fija para todo el flujo de Auth/Main. Mantengo consistencia visual.
private val Teal = Color(0xFF00A9B9)
private val TealDark = Color(0xFF0093A2)
private val Coral = Color(0xFFFF6F61)
private val Soft = Color(0xFFE6F4F1)
private val Soft2 = Color(0xFFE5F1F1)

private val FixedLightColors = lightColorScheme(
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

@Composable
fun AppNavigation(
    navController: NavHostController,
    darkTheme: Boolean,                 // estado externo (para Settings)
    onThemeChange: (Boolean) -> Unit    // callback para cambiar tema
) {
    // Creo el ViewModel compartido de Pacientes con un Store basado en SharedPreferences.
    // Clave para MVVM: la UI navega; el VM mantiene datos y persiste.
    val ctx = LocalContext.current
    val patientsVm: PatientsViewModel = viewModel(
        factory = PatientsViewModelFactory(SharedPrefsPatientsStore(ctx))
    )

    // Ruta actual : animamos según hacia dónde navego
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "login"

    // Decido sentido de animación entre login y register
    var lastRoute by remember { mutableStateOf(currentRoute) }
    val forward = remember(currentRoute, lastRoute) {
        lastRoute == "login" && currentRoute == "register"
    }
    LaunchedEffect(currentRoute) { lastRoute = currentRoute }

    // AnimatedContent: transiciones entre rutas con slide + fade + leve scale
    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            val dur = 650
            val ease = FastOutSlowInEasing
            if (forward) {
                // Login -> Register: entra desde la derecha, sale hacia la izquierda
                (slideInHorizontally(
                    initialOffsetX = { full -> (full * 0.9f).toInt() },
                    animationSpec = tween(dur, easing = ease)
                ) + fadeIn(
                    animationSpec = tween(dur, easing = ease),
                    initialAlpha = 0.0f
                ) + scaleIn(
                    initialScale = 0.98f,
                    animationSpec = tween((dur * 0.9f).toInt(), easing = ease)
                )) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 2 },
                            animationSpec = tween(250)
                        ) + fadeOut())
            } else {
                // Register -> Login: entra desde la izquierda, sale a la derecha
                (slideInHorizontally(
                    initialOffsetX = { full -> -(full * 0.9f).toInt() },
                    animationSpec = tween(dur, easing = ease)
                ) + fadeIn(
                    animationSpec = tween(dur, easing = ease),
                    initialAlpha = 0.0f
                ) + scaleIn(
                    initialScale = 0.98f,
                    animationSpec = tween((dur * 0.9f).toInt(), easing = ease)
                )) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth / 2 },
                            animationSpec = tween(250)
                        ) + fadeOut(animationSpec = tween(dur, easing = ease)))
            }.using(SizeTransform(clip = false))
        },
        modifier = Modifier.fillMaxSize(),
        label = "NavTransitions"
    ) { route ->
        // key(route): asegura recomposición limpia por pantalla
        key(route) {
            // NavHost: defino rutas. startDestination = "login"
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.fillMaxSize()
            ) {
                // Ruta: Login. Envuelvo en MaterialTheme(FixedLightColors) para tema consistente.
                composable("login") {
                    MaterialTheme(colorScheme = FixedLightColors) {
                        LoginVisualScreen(
                            onCreateAccount = { navController.navigate("register") },
                            {
                                // Cuando el login ok: muestro pantalla intermedia "loading" (feedback)
                                navController.navigate("loading")
                            }
                        )
                    }
                }
                // Ruta: Registro(vuelve a login).
                composable("register") {
                    MaterialTheme(colorScheme = FixedLightColors) {
                        RegistroScreenSimple(
                            goLogin = { navController.popBackStack() }
                        )
                    }
                }
                // Ruta: Loading. Pantalla de carga que luego redirige a main
                composable("loading") {
                    LoadingScreen(navController = navController)
                }
                // Ruta: Main (home). Leo pacientes del VM compartido
                composable("main") {
                    val patients by patientsVm.patients.collectAsState()
                    MainScreen(
                        pacientesActivos = patients.size,             // métrica visible
                        onGoSettings = { navController.navigate("settings") },
                        onGoPatients = { navController.navigate("patients") },
                        proximasCitas = 0,
                        vacunasPendientes = 0
                    )
                }
                // Ruta: Settings. Paso darkTheme + onThemeChange
                composable("settings") {
                    SettingsScreen(
                        darkTheme = darkTheme,
                        onThemeChange = onThemeChange,
                        onGoHome = { navController.navigate("main") },
                        onGoPatients = { navController.navigate("patients") }
                    )
                }
                // Ruta: Patients. Lista reactiva + acciones del VM + navegación a add_patient
                composable("patients") {
                    val patients by patientsVm.patients.collectAsState()
                    PatientsScreen(
                        patients = patients,
                        onAddPatient = { navController.navigate("add_patient") },
                        onPatientClick = { /* detalle, si lo implemento luego */ },
                        onRemovePatient = { patientsVm.removePatient(it.id) }, // eliminación directa en VM
                        onGoHome = { navController.navigate("main") },
                        onGoPatients = { /* ya estoy aquí */ },
                        onGoReminders = { /* pendiente si agrego recordatorios */ },
                        onSettings = { navController.navigate("settings") },
                        currentTab = MainTabPatients.PATIENTS
                    )
                }
                // Ruta: Add patient. onSave agrega al VM y vuelve a Patients
                composable("add_patient") {
                    AddPatientScreen(
                        onBack = { navController.popBackStack() },
                        onSave = { p ->
                            patientsVm.addPatient(p)       // persiste a través del Store (en el VM)
                            navController.popBackStack()   // regreso a la lista
                        },
                        onGoHome = { navController.navigate("main") },
                        onGoPatients = { navController.navigate("patients") }
                    )
                }
            }
        }
    }
}
