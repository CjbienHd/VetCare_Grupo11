@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.vetcare_grupo11.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vetcare_grupo11.viewmodel.Appointment

@Composable
fun AddAppointmentScreen(
    appointmentToEdit: Appointment? = null,
    onSave: (Appointment) -> Unit,
    onBack: () -> Unit
) {
    val isEditMode = appointmentToEdit != null

    var patientName by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var fechaHora by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf(appointmentToEdit?.estado ?: "Programada") }

    // Rellena los campos si estamos editando
    LaunchedEffect(key1 = appointmentToEdit) {
        if (isEditMode) {
            patientName = appointmentToEdit?.patientName ?: ""
            motivo = appointmentToEdit?.motivo ?: ""
            fechaHora = appointmentToEdit?.fechaHora ?: ""
            notas = appointmentToEdit?.notas ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Editar Cita" else "Nueva Cita",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = patientName,
                onValueChange = { patientName = it },
                label = { Text("Nombre del paciente") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = motivo,
                onValueChange = { motivo = it },
                label = { Text("Motivo de la cita") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fechaHora,
                onValueChange = { fechaHora = it },
                label = { Text("Fecha y hora (ej: 2025-11-30 10:30)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado,
                onValueChange = { estado = it },
                label = { Text("Estado") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val finalAppointment = if (isEditMode) {
                        // Modo Edición: Copia el original y actualiza los campos
                        appointmentToEdit!!.copy(
                            patientName = patientName.trim(),
                            motivo = motivo.trim(),
                            fechaHora = fechaHora.trim(),
                            notas = notas.trim(),
                            estado = estado.trim()
                        )
                    } else {
                        // Modo Creación: Crea un objeto nuevo
                        Appointment(
                            // id es null, el ViewModel se encargará de él
                            patientName = patientName.trim(),
                            motivo = motivo.trim(),
                            fechaHora = fechaHora.trim(),
                            notas = notas.trim(),
                            estado = estado.trim()
                        )
                    }
                    onSave(finalAppointment)
                },
                enabled = patientName.isNotBlank() && motivo.isNotBlank() && fechaHora.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}
