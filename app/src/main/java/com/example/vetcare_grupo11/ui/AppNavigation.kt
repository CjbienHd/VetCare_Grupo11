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
import com.example.vetcare_grupo11.data.SharedPrefsPatientsStore
import com.example.vetcare_grupo11.viewmodel.PatientsViewModelFactory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.vetcare_grupo11.viewmodel.AppointmentsViewModel
import androidx.compose.runtime.collectAsState


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
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val ctx = LocalContext.current
    val patientsVm: PatientsViewModel = viewModel(
        factory = PatientsViewModelFactory(SharedPrefsPatientsStore(ctx))
    )
    val appointmentsVm: AppointmentsViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "login"

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
        key(route) {
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("login") {
                    MaterialTheme(colorScheme = FixedLightColors) {
                        LoginVisualScreen(
                            onCreateAccount = { navController.navigate("register") },
                            {
                                navController.navigate("loading")
                            }
                        )
                    }
                }
                composable("register") {
                    MaterialTheme(colorScheme = FixedLightColors) {
                        RegistroScreenSimple(
                            goLogin = { navController.popBackStack() }
                        )
                    }
                }
                composable("loading") {
                    LoadingScreen(navController = navController)
                }
                composable("main") {
                    val patients by patientsVm.patients.collectAsState()
                    val appointments by appointmentsVm.appointments.collectAsState()

                    MainScreen(
                        pacientesActivos = patients.size,
                        onGoSettings = { navController.navigate("settings") },
                        onGoPatients = { navController.navigate("patients") },
                        onGoReminders = { navController.navigate("appointments") }, // Conectado
                        onFabClick = { navController.navigate("appointment_form") },      // Conectado
                        proximasCitas = appointments.size,
                        vacunasPendientes = 0
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        darkTheme = darkTheme,
                        onThemeChange = onThemeChange,
                        onGoHome = { navController.navigate("main") },
                        onGoPatients = { navController.navigate("patients") }
                    )
                }

                composable("patients") {
                    val patients by patientsVm.patients.collectAsState()
                    PatientsScreen(
                        patients = patients,
                        onAddPatient = { navController.navigate("patient_form") },
                        onPatientClick = { patient ->
                            navController.navigate("patient_form?id=${patient.id}")
                        },
                        onRemovePatient = { patientsVm.removePatient(it.id) },
                        onGoHome = { navController.navigate("main") },
                        onGoPatients = { },
                        onGoReminders = { navController.navigate("appointments") },
                        onSettings = { navController.navigate("settings") },
                        currentTab = MainTabPatients.PATIENTS
                    )
                }

                composable(
                    route = "patient_form?id={id}", // Ruta única para el formulario de pacientes
                    arguments = listOf(navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                    })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")
                    val patient = id?.let { patientsVm.getPatient(it) }

                    AddPatientScreen(
                        patientToEdit = patient,
                        onBack = { navController.popBackStack() },
                        onSave = { p ->
                            if (patient == null) {
                                patientsVm.addPatient(p)
                            } else {
                                patientsVm.updatePatient(p)
                            }
                            navController.popBackStack()
                        },
                        onGoHome = { navController.navigate("main") },
                        onGoPatients = { navController.navigate("patients") }
                    )
                }

                composable("appointments") {
                    val appointments by appointmentsVm.appointments.collectAsState()

                    AppointmentsScreen(
                        appointments = appointments,
                        onAddAppointment = { navController.navigate("appointment_form") },
                        onAppointmentClick = { cita ->
                            navController.navigate("appointment_form?id=${cita.id}")
                        },
                        onRemoveAppointment = { appointmentsVm.removeAppointment(it.id) },
                        onGoHome = { navController.navigate("main") }
                    )
                }

                composable(
                    route = "appointment_form?id={id}", // Ruta única para el formulario de citas
                    arguments = listOf(navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                    })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")
                    val cita = id?.let { appointmentsVm.getAppointment(it) }

                    AddAppointmentScreen(
                        appointmentToEdit = cita,
                        onSave = { a ->
                            if (cita == null) {
                                appointmentsVm.addAppointment(a)
                            } else {
                                appointmentsVm.updateAppointment(a)
                            }
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
