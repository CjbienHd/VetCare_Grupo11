@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.vetcare_grupo11.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vetcare_grupo11.viewmodel.Appointment

@Composable
fun AppointmentsScreen(
    appointments: List<Appointment>,
    onAddAppointment: () -> Unit,
    onAppointmentClick: (Appointment) -> Unit,
    onRemoveAppointment: (Appointment) -> Unit,
    onGoHome: () -> Unit
) {

    // Estado para controlar qué cita se quiere borrar y mostrar el diálogo
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
        }
    ) { padding ->

        // --- Diálogo de Confirmación de Borrado ---
        if (appointmentToDelete != null) {
            AlertDialog(
                onDismissRequest = { appointmentToDelete = null }, // Cierra el diálogo si se pulsa fuera
                title = { Text("Confirmar Eliminación") },
                text = { Text("¿Estás seguro de que quieres eliminar la cita para '${appointmentToDelete!!.patientName}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onRemoveAppointment(appointmentToDelete!!)
                            appointmentToDelete = null // Cierra el diálogo
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

        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay citas registradas")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(appointments, key = { it.id ?: it.hashCode() }) { cita ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onAppointmentClick(cita) },
                                onLongClick = { appointmentToDelete = cita } // En pulsación larga, seleccionamos la cita para borrar
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = cita.patientName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Motivo: ${cita.motivo}")
                            Text("Fecha y hora: ${cita.fechaHora}")
                            if (cita.notas.isNotBlank()) {
                                Text("Notas: ${cita.notas}")
                            }
                            Text("Estado: ${cita.estado}")
                        }
                    }
                }
            }
        }
    }
}
