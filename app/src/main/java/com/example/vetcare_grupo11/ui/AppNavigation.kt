@file:OptIn(ExperimentalAnimationApi::class)

package com.example.vetcare_grupo11.ui

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.vetcare_grupo11.data.SharedPrefsPatientsStore
import com.example.vetcare_grupo11.viewmodel.AppointmentsViewModel
import com.example.vetcare_grupo11.viewmodel.PatientsViewModel
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
    val appointmentsVm: AppointmentsViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "login"

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                fadeOut(animationSpec = tween(300))
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
                        onLoginOk = { userEmail ->
                            ctx.getSharedPreferences("datos_app", Context.MODE_PRIVATE).edit {
                                putString("logged_in_email", userEmail)
                            }
                            navController.navigate("loading")
                        }
                    )
                }
                composable("register") {
                    RegistroScreenSimple(
                        goLogin = { navController.popBackStack() }
                    )
                }
                composable("loading") {
                    LoadingScreen(navController = navController)
                }
                composable("main") {
                    val patients by patientsVm.patients.collectAsState()
                    val appointments by appointmentsVm.appointments.collectAsState()
                    val vacunasPendientes = appointments.count { it.motivo.equals("Vacuna", ignoreCase = true) && it.estado == "Programada" }
                    val userName = getUserName(ctx) // Obtener el nombre de usuario

                    MainScreen(
                        navController = navController,
                        userName = userName,
                        pacientesActivos = patients.size,
                        onGoSettings = { navController.navigate("settings") },
                        onGoPatients = { navController.navigate("patients") },
                        onGoReminders = { navController.navigate("appointments") },
                        onFabClick = { navController.navigate("appointment_form") },
                        proximasCitas = appointments.size,
                        vacunasPendientes = vacunasPendientes
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        darkTheme = darkTheme,
                        onThemeChange = onThemeChange,
                        onGoHome = { navController.navigate("main") },
                        onGoPatients = { navController.navigate("patients") },
                        onGoReminders = { navController.navigate("appointments") },
                        onLogout = {
                            ctx.getSharedPreferences("datos_app", Context.MODE_PRIVATE).edit {
                                remove("logged_in_email")
                            }
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
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
                        currentTab = MainTab.PATIENTS
                    )
                }

                composable(
                    route = "patient_form?id={id}",
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
                                patientsVm.addPatient(p, ctx)
                            } else {
                                patientsVm.updatePatient(p, ctx)
                            }
                            navController.popBackStack()
                        },
                        onGoHome = { navController.navigate("main") },
                        onGoPatients = { navController.navigate("patients") }
                    )
                }

                composable("appointments") {
                    val patients by patientsVm.patients.collectAsState()

                    AppointmentsScreen(
                        appointmentsVm = appointmentsVm,
                        patients = patients,
                        onAddAppointment = { navController.navigate("appointment_form") },
                        onAppointmentClick = { cita ->
                            navController.navigate("appointment_form?id=${cita.id}")
                        },
                        onGoHome = { navController.navigate("main") },
                        onGoPatients = { navController.navigate("patients") }
                    )
                }

                composable("clima_clinica") {
                    PantallaClimaClinica(volver = { navController.popBackStack() })
                }

                composable(
                    route = "appointment_form?id={id}",
                    arguments = listOf(navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                    })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")
                    val cita = id?.let { appointmentsVm.getAppointment(it) }
                    val patients by patientsVm.patients.collectAsState()

                    AddAppointmentScreen(
                        appointmentToEdit = cita,
                        patients = patients, 
                        onSave = { a ->
                            if (cita == null) {
                                appointmentsVm.addAppointment(a, ctx) // <-- CONTEXTO PASADO
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

private fun getUserName(context: Context): String {
    val sp = context.getSharedPreferences("datos_app", Context.MODE_PRIVATE)
    val loggedInEmail = sp.getString("logged_in_email", null)
    if (loggedInEmail != null) {
        val allUsers = sp.getString("usuarios", "") ?: ""
        val userEntry = allUsers.split(";").find { it.startsWith("$loggedInEmail|") }
        if (userEntry != null) {
            val parts = userEntry.split("|")
            if (parts.size == 3) {
                return parts[2]
            }
        }
    }
    return ""
}
