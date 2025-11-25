@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.vetcare_grupo11.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vetcare_grupo11.viewmodel.Appointment
import com.example.vetcare_grupo11.viewmodel.Patient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddAppointmentScreen(
    appointmentToEdit: Appointment? = null,
    patients: List<Patient>,
    onSave: (Appointment) -> Unit,
    onBack: () -> Unit
) {
    val isEditMode = appointmentToEdit != null
    val context = LocalContext.current

    var patientName by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf(appointmentToEdit?.motivo ?: "") }
    var fechaHoraTexto by remember { mutableStateOf("") }
    var fechaHoraMillis by remember { mutableStateOf<Long?>(null) }
    var notas by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf(appointmentToEdit?.estado ?: "Programada") }

    var patientMenuExpanded by remember { mutableStateOf(false) }
    var motivoMenuExpanded by remember { mutableStateOf(false) }
    val motivos = listOf("Consulta", "Vacuna")


    LaunchedEffect(key1 = appointmentToEdit) {
        if (isEditMode && appointmentToEdit != null) {
            patientName = appointmentToEdit.patientName
            motivo = appointmentToEdit.motivo
            fechaHoraTexto = appointmentToEdit.fechaHora
            fechaHoraMillis = appointmentToEdit.fechaHoraMillis
            notas = appointmentToEdit.notas
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Cita" else "Nueva Cita", fontWeight = FontWeight.SemiBold) },
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

            ExposedDropdownMenuBox(
                expanded = patientMenuExpanded,
                onExpandedChange = { patientMenuExpanded = !patientMenuExpanded }
            ) {
                OutlinedTextField(
                    value = patientName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Paciente") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientMenuExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = patientMenuExpanded,
                    onDismissRequest = { patientMenuExpanded = false }
                ) {
                    patients.forEach { patient ->
                        DropdownMenuItem(
                            text = { Text(patient.nombre) },
                            onClick = {
                                patientName = patient.nombre
                                patientMenuExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = motivoMenuExpanded,
                onExpandedChange = { motivoMenuExpanded = !motivoMenuExpanded }
            ) {
                OutlinedTextField(
                    value = motivo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Motivo de la cita") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = motivoMenuExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = motivoMenuExpanded,
                    onDismissRequest = { motivoMenuExpanded = false }
                ) {
                    motivos.forEach { motivoOption ->
                        DropdownMenuItem(
                            text = { Text(motivoOption) },
                            onClick = {
                                motivo = motivoOption
                                motivoMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = fechaHoraTexto,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha y hora de la cita") },
                trailingIcon = {
                    IconButton(onClick = { 
                        mostrarDateTimePicker(
                            context = context,
                            onFechaHoraSeleccionada = { millis, texto ->
                                fechaHoraMillis = millis
                                fechaHoraTexto = texto
                            }
                        )
                    }) {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = "Seleccionar fecha"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
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
                        appointmentToEdit!!.copy(
                            patientName = patientName.trim(),
                            motivo = motivo.trim(),
                            fechaHora = fechaHoraTexto.trim(),
                            fechaHoraMillis = fechaHoraMillis!!,
                            notas = notas.trim(),
                            estado = estado.trim(),
                            esVacuna = motivo == "Vacuna"
                        )
                    } else {
                        Appointment(
                            patientName = patientName.trim(),
                            motivo = motivo.trim(),
                            fechaHora = fechaHoraTexto.trim(),
                            fechaHoraMillis = fechaHoraMillis!!,
                            notas = notas.trim(),
                            estado = estado.trim(),
                            esVacuna = motivo == "Vacuna"
                        )
                    }
                    onSave(finalAppointment)
                },
                enabled = patientName.isNotBlank() && motivo.isNotBlank() && fechaHoraMillis != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}

fun mostrarDateTimePicker(
    context: Context,
    onFechaHoraSeleccionada: (Long, String) -> Unit
) {
    val calendario = Calendar.getInstance()

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendario.set(Calendar.YEAR, year)
            calendario.set(Calendar.MONTH, month)
            calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendario.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendario.set(Calendar.MINUTE, minute)
                    calendario.set(Calendar.SECOND, 0)
                    calendario.set(Calendar.MILLISECOND, 0)

                    val millis = calendario.timeInMillis
                    val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val texto = formato.format(Date(millis))

                    onFechaHoraSeleccionada(millis, texto)
                },
                calendario.get(Calendar.HOUR_OF_DAY),
                calendario.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendario.get(Calendar.YEAR),
        calendario.get(Calendar.MONTH),
        calendario.get(Calendar.DAY_OF_MONTH)
    ).show()
}
