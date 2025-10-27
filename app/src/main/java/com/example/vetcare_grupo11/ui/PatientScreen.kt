@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.vetcare_grupo11.ui



import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vetcare_grupo11.viewmodel.Patient


@Composable
fun PatientsScreen(
    patients: List<Patient>,              // ← recibe la lista del VM
    onAddPatient: () -> Unit,
    onPatientClick: (Patient) -> Unit = {},
    onGoReminders: () -> Unit = {},
    onGoHome: () -> Unit = {},
    onSettings: () -> Unit = {},
    onRemovePatient: (Patient) -> Unit,
    onGoPatients: () -> Unit = {},
    currentTab: MainTabPatients = MainTabPatients.PATIENTS
) {
    var toDelete: Patient? by remember { mutableStateOf(null) }
    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    // Título centrado como en tu login/registro
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "VetCare",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPatient,
                containerColor = MaterialTheme.colorScheme.secondary,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = MaterialTheme.colorScheme.onSecondary)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            AppBottomBar(
                current = currentTab,
                onReminders = onGoReminders,
                onHome = onGoHome,
                onPatients = onGoPatients
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Banda de bienvenida suave (opcional, mismo lenguaje visual)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Pacientes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Mis Mascotas",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Lista de pacientes
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 96.dp) // espacio para FAB/bottombar
            ) {
                items(patients, key = { it.id }) { p ->
                    PatientCard(
                        patient = p,
                        onClick = { onPatientClick(p) },
                                onLongPress = { toDelete = p }
                    )
                }

            }
        }
    }
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar paciente") },
            text  = { Text("¿Seguro que deseas eliminar a \"${toDelete!!.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onRemovePatient(toDelete!!)   // ← callback que borra en el VM
                    toDelete = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancelar") }
            }
        )
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PatientCard(
    patient: Patient,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(             // ← aquí el long-press
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar simple con icono de especie
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = patient.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${patient.especie} • ${patient.raza}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tutor: ${patient.tutor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }
}

/** Bottom bar estilo app (idéntico a tu Home, pero local para esta pantalla) */
@Composable
private fun AppBottomBar(
    current: MainTabPatients,
    onReminders: () -> Unit,
    onHome: () -> Unit,
    onPatients: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        NavigationBarItem(
            selected = current == MainTabPatients.REMINDERS,
            onClick = onReminders,
            icon = { Icon(Icons.Outlined.Notifications, contentDescription = "Citas") },
            label = { Text("Citas") }
        )
        NavigationBarItem(
            selected = current == MainTabPatients.HOME,
            onClick = onHome,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = "Home",
                    tint = if (current == MainTabPatients.HOME)
                        MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = {
                Text(
                    "Home",
                    color = if (current == MainTabPatients.HOME)
                        MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        NavigationBarItem(
            selected = current == MainTabPatients.PATIENTS,
            onClick = onPatients,
            icon = { Icon(Icons.Filled.Search, contentDescription = "Pacientes") },
            label = { Text("Pacientes") }
        )
    }
}

// Tabs para marcar el seleccionado en el bottom bar
enum class MainTabPatients { REMINDERS, HOME, PATIENTS }
