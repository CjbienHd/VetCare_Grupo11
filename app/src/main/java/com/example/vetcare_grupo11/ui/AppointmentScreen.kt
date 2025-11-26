@file:OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.example.vetcare_grupo11.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.vetcare_grupo11.viewmodel.Appointment
import com.example.vetcare_grupo11.viewmodel.AppointmentsViewModel
import com.example.vetcare_grupo11.viewmodel.Patient

@Composable
fun AppointmentsScreen(
    appointmentsVm: AppointmentsViewModel = viewModel(),
    patients: List<Patient>,
    onAddAppointment: () -> Unit,
    onAppointmentClick: (Appointment) -> Unit,
    onGoHome: () -> Unit,
    onGoPatients: () -> Unit,
    currentTab: MainTab = MainTab.REMINDERS
) {
    val appointments by appointmentsVm.appointments.collectAsState()
    val isLoading by appointmentsVm.isLoading.collectAsState()
    val errorMessage by appointmentsVm.errorMessage.collectAsState()
    var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Citas") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAppointment) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva cita")
            }
        },
        bottomBar = {
            MainBottomBar(
                current = currentTab,
                onReminders = {},
                onHome = onGoHome,
                onPatients = onGoPatients
            )
        }
    ) { padding ->

        if (appointmentToDelete != null) {
            AlertDialog(
                onDismissRequest = { appointmentToDelete = null },
                title = { Text("Confirmar Eliminación") },
                text = { Text("¿Estás seguro de que quieres eliminar la cita para '${appointmentToDelete!!.patientName}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            appointmentsVm.deleteAppointment(appointmentToDelete!!.id!!)
                            appointmentToDelete = null
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { appointmentToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Ocurrió un error inesperado.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { appointmentsVm.loadFromBackend() }) {
                            Text("Reintentar")
                        }
                    }
                }

                appointments.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay citas registradas", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(appointments, key = { it.id ?: it.hashCode() }) { cita ->
                            val patient = patients.find { it.nombre == cita.patientName }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onAppointmentClick(cita) },
                                        onLongClick = { appointmentToDelete = cita }
                                    )
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (patient?.fotoUri != null) {
                                        AsyncImage(
                                            model = patient.fotoUri,
                                            contentDescription = "Foto de ${patient.nombre}",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        val icon = if (cita.esVacuna) Icons.Default.Vaccines else Icons.Outlined.Event
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = cita.patientName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Motivo: ${cita.motivo}")
                                        Text("Fecha: ${cita.fechaHora}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
