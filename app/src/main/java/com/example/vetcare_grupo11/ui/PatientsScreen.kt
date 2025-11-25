@file:OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)

package com.example.vetcare_grupo11.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.vetcare_grupo11.viewmodel.Patient

@Composable
fun PatientsScreen(
    patients: List<Patient>,
    onAddPatient: () -> Unit,
    onPatientClick: (Patient) -> Unit,
    onRemovePatient: (Patient) -> Unit,
    onGoHome: () -> Unit,
    onGoPatients: () -> Unit,
    onGoReminders: () -> Unit,
    onSettings: () -> Unit,
    currentTab: MainTab = MainTab.PATIENTS,
) {
    var patientToDelete by remember { mutableStateOf<Patient?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pacientes") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPatient) {
                Icon(Icons.Default.Add, contentDescription = "Agregar paciente")
            }
        },
        bottomBar = {
            MainBottomBar(
                current = currentTab,
                onHome = onGoHome,
                onPatients = onGoPatients,
                onReminders = onGoReminders
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp) // Added padding for the list
        ) {
            if (patients.isEmpty()) {
                item {
                    Text(
                        "No hay pacientes registrados",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(patients, key = { it.id ?: it.hashCode() }) { patient ->
                    PatientListItem(
                        patient = patient,
                        onClick = { onPatientClick(patient) },
                        onLongClick = { patientToDelete = patient }
                    )
                }
            }
        }
    }

    if (patientToDelete != null) {
        AlertDialog(
            onDismissRequest = { patientToDelete = null },
            title = { Text("Eliminar Paciente") },
            text = { Text("¿Estás seguro de que quieres eliminar a '${patientToDelete?.nombre}'?") },
            confirmButton = {
                Button(onClick = {
                    patientToDelete?.let { onRemovePatient(it) }
                    patientToDelete = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { patientToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PatientListItem(
    patient: Patient,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with Photo or Icon
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (patient.fotoUri != null) {
                    AsyncImage(
                        model = patient.fotoUri,
                        contentDescription = "Foto de ${patient.nombre}",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patient.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tutor: ${patient.tutor}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Especie: ${patient.especie}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
