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

@Composable
fun MainScreen(
    pacientesActivos: Int = 0,    // métrica visible 1
    proximasCitas: Int = 0,       // métrica visible 2
    vacunasPendientes: Int = 0,   // métrica visible 3
    currentTab: MainTab = MainTab.HOME, // control externo del tab activo
    onGoSettings: () -> Unit = {},      // abre Settings
    onGoReminders: () -> Unit = {},     // abre Citas
    onGoHome: () -> Unit = {},          // vuelve a Home
    onGoPatients: () -> Unit = {},      // abre Pacientes
    onFabClick: () -> Unit = {}         // acción principal: crear cita/paciente
) {
    Scaffold(
        // TopBar con título y botón de ajustes
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

        // Bottom bar: navegación entre secciones
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
        // Contenido principal: 3 tarjetas de métricas
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Tarjeta 1: Pacientes activos
            MetricCard(
                title = "Pacientes Activos",
                value = pacientesActivos,
                modifier = Modifier.fillMaxWidth()
            )

            // Tarjeta 2: Próximas citas
            MetricCard(
                title = "Citas",
                value = proximasCitas,
                modifier = Modifier.fillMaxWidth()
            )

            // Tarjeta 3: Vacunas pendientes
            MetricCard(
                title = "Vacunas Pendientes",
                value = vacunasPendientes,
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(Modifier.height(72.dp))
        }
    }
}


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
            // Texto de la métrica
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(Modifier.weight(1f))

            // Contador a la derecha
            CounterBadge(value = value)
        }
    }
}

/**
 * CounterBadge = círculo con el número de la métrica
 * - Color secundario para dar contraste.
 * - Tamaño controlado para consistencia visual con el resto de la UI.
 */
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

/**
 * MainBottomBar = barra inferior con 3 pestañas (Citas / Home / Pacientes).
 * - La selección depende de currentTab (estado que viene de arriba).
 * - Cada item ejecuta un callback para navegar (NavController vive fuera).
 * - Home destaca con color secundario cuando está activo (coherencia visual).
 */
@Composable
private fun MainBottomBar(
    current: MainTab,
    onReminders: () -> Unit,
    onHome: () -> Unit,
    onPatients: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        // Tab 1: Citas (campana). Navega a recordatorios/agenda.
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

        // Tab 2: Home. Es el seleccionado por defecto.
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

        // Tab 3: Pacientes. Busca/lista pacientes.
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
