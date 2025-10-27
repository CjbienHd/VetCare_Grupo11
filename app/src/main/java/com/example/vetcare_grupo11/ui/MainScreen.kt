@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.vetcare_grupo11.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pantalla principal con:
 * - Header teal (VetCare + engrane)
 * - 3 tarjetas con contador naranja
 * - FAB central +
 * - Bottom bar (campana / pata activa / lupa)
 */
@Composable
fun MainScreen(
    pacientesActivos: Int = 0,
    proximasCitas: Int = 0,
    vacunasPendientes: Int = 0,
    currentTab: MainTab = MainTab.HOME,
    onGoSettings: () -> Unit = {},
    onGoReminders: () -> Unit = {},
    onGoHome: () -> Unit = {},
    onGoPatients: () -> Unit = {},
    onFabClick: () -> Unit = {} // por ejemplo, crear nueva cita/paciente
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "VetCare",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onGoSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Ajustes"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = MaterialTheme.colorScheme.secondary,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, CircleShape)
            ) {
                Text("+", color = MaterialTheme.colorScheme.onSecondary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            MainBottomBar(
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            MetricCard(
                title = "Pacientes Activos",
                value = pacientesActivos,
                modifier = Modifier.fillMaxWidth()
            )

            MetricCard(
                title = "Citas",
                value = proximasCitas,
                modifier = Modifier.fillMaxWidth()
            )

            MetricCard(
                title = "Vacunas Pendientes",
                value = vacunasPendientes,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(72.dp)) // espacio para FAB y bottom bar
        }
    }
}

/** Tarjeta de métrica con contador naranja a la derecha */
@Composable
private fun MetricCard(
    title: String,
    value: Int,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(Modifier.weight(1f))
            CounterBadge(value = value)
        }
    }
}

/** Contador circular coral (para números) */
@Composable
private fun CounterBadge(value: Int) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(color = MaterialTheme.colorScheme.secondary, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

/** Bottom bar con tres íconos: campana / pata (activa) / lupa */
@Composable
private fun MainBottomBar(
    current: MainTab,
    onReminders: () -> Unit,
    onHome: () -> Unit,
    onPatients: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        NavigationBarItem(
            selected = current == MainTab.REMINDERS,
            onClick = onReminders,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Citas"
                )
            },
            label = { Text("Citas") }
        )
        NavigationBarItem(
            selected = current == MainTab.HOME,
            onClick = onHome,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = "Home",
                    tint = if (current == MainTab.HOME) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = {
                Text(
                    "Home",
                    color = if (current == MainTab.HOME) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        NavigationBarItem(
            selected = current == MainTab.PATIENTS,
            onClick = onPatients,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Pacientes"
                )
            },
            label = { Text("Pacientes") }
        )
    }
}
