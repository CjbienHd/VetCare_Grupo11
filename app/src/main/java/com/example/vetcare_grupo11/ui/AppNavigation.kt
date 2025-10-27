@file:OptIn(ExperimentalAnimationApi::class)

package com.example.vetcare_grupo11.ui


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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vetcare_grupo11.ui.LoadingScreen
import com.example.vetcare_grupo11.ui.MainScreen
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vetcare_grupo11.data.SharedPrefsPatientsStore
import com.example.vetcare_grupo11.viewmodel.PatientsViewModelFactory


@Composable
fun AppNavigation(
    navController: NavHostController,
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val ctx = LocalContext.current
    val patientsVm: PatientsViewModel = viewModel(
        factory = PatientsViewModelFactory(SharedPrefsPatientsStore(ctx))
    )
    // Ruta actual
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "login"

    // Recordar ruta anterior para decidir sentido de la animación
    var lastRoute by remember { mutableStateOf(currentRoute) }
    val forward = remember(currentRoute, lastRoute) {
        lastRoute == "login" && currentRoute == "register"
    }
    LaunchedEffect(currentRoute) { lastRoute = currentRoute }

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            val dur = 650
            val ease = FastOutSlowInEasing
            if (forward) {
                // Animación: Login -> Registro (la nueva pantalla entra desde la derecha)
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
                // Animación: Registro -> Login (la nueva pantalla entra desde la izquierda)
                (slideInHorizontally(
                    initialOffsetX = { full -> -(full * 0.9f).toInt() }, // Cambiado a negativo para que entre desde la izquierda
                    animationSpec = tween(dur, easing = ease)
                ) + fadeIn(
                    animationSpec = tween(dur, easing = ease),
                    initialAlpha = 0.0f
                ) + scaleIn(
                    initialScale = 0.98f,
                    animationSpec = tween((dur * 0.9f).toInt(), easing = ease)
                )) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth / 2 }, // Cambiado a positivo para que salga a la derecha
                            animationSpec = tween(250)
                        ) + fadeOut(animationSpec = tween(dur, easing = ease)))
            }.using(SizeTransform(clip = false))
        },
        modifier = Modifier.fillMaxSize(),
        label = "NavTransitions"
    ) { route ->
        key(route) {
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("login") {
                    LoginVisualScreen(
                        onCreateAccount = { navController.navigate("register") },
                        {
                            navController.navigate("loading")
                        }
                    )
                }
                composable("register") {

                    // Usamos el nombre correcto de la función y el parámetro correcto.
                    RegistroScreenSimple(
                        goLogin = { navController.popBackStack() }
                    )
                }
                composable("loading") {
                    LoadingScreen(navController = navController)
                }
                composable("main") {
                    val patients by patientsVm.patients.collectAsState()
                    MainScreen(
                        pacientesActivos = patients.size,             // ← el MetricCard leerá esto
                        onGoSettings = { navController.navigate("settings") },
                        onGoPatients = { navController.navigate("patients") },
                        proximasCitas = 0,
                        vacunasPendientes = 0
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        darkTheme = darkTheme,
                        onThemeChange = onThemeChange,
                        onGoHome = { navController.navigate("main") }
                    )


                }

                composable("patients") {
                    val patients by patientsVm.patients.collectAsState()
                    PatientsScreen(
                        patients = patients,
                        onAddPatient = { navController.navigate("add_patient") },
                        onPatientClick = { /* detalle si quieres */ },
                        onRemovePatient = { patientsVm.removePatient(it.id) },   // ← AQUÍ BORRA
                        onGoHome = { navController.navigate("main") },
                        onGoPatients = { /* ya estás */ },
                        onGoReminders = { /* ... */ },
                        onSettings = { /* ... */ },
                        currentTab = MainTabPatients.PATIENTS
                    )
                }
                composable("add_patient") {
                    AddPatientScreen(
                        onBack = { navController.popBackStack() },
                        onSave = { p ->
                            patientsVm.addPatient(p)       // agrega al VM compartido
                            navController.popBackStack()   // vuelve a Patients
                        }
                    )
                }
            }
        }
    }
}
