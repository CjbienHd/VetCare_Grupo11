@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.vetcare_grupo11.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun MainScreen(
    navController: NavHostController,
    pacientesActivos: Int = 0,
    proximasCitas: Int = 0,
    vacunasPendientes: Int = 0,
    currentTab: MainTab = MainTab.HOME,
    onGoSettings: () -> Unit = {},
    onGoReminders: () -> Unit = {},
    onGoHome: () -> Unit = {},
    onGoPatients: () -> Unit = {},
    onFabClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VetCare") },
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
                    .size(90.dp) // <-- TAMAÑO AUMENTADO
                    .shadow(12.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir Cita",
                    modifier = Modifier.size(44.dp), // <-- TAMAÑO DE ICONO AUMENTADO
                    tint = MaterialTheme.colorScheme.onSecondary
                )
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Grupo de tarjetas de métrica
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(
                    title = "Pacientes Activos",
                    value = pacientesActivos,
                    onClick = onGoPatients,
                    icon = Icons.Default.Pets
                )

                MetricCard(
                    title = "Citas Programadas",
                    value = proximasCitas,
                    onClick = onGoReminders,
                    icon = Icons.Outlined.Event
                )

                MetricCard(
                    title = "Vacunas Pendientes",
                    value = vacunasPendientes,
                    onClick = onGoReminders,
                    icon = Icons.Default.Vaccines
                )
            }

            // Espaciador con peso para empujar el botón hacia abajo
            Spacer(Modifier.weight(1.5f))

            FilledTonalButton(
                onClick = { navController.navigate("clima_clinica") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Cloud, contentDescription = null)
                    Text("Clima de la Clínica", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.weight(15f))

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            CounterBadge(value = value)
        }
    }
}

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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MainBottomBar(
    current: MainTab,
    onReminders: () -> Unit,
    onHome: () -> Unit,
    onPatients: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = current == MainTab.REMINDERS,
            onClick = onReminders,
            icon = { Icon(Icons.Outlined.Notifications, contentDescription = "Citas") },
            label = { Text("Citas") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.secondary,
                selectedTextColor = MaterialTheme.colorScheme.secondary,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        NavigationBarItem(
            selected = current == MainTab.HOME,
            onClick = onHome,
            icon = { Icon(Icons.Filled.Pets, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.secondary,
                selectedTextColor = MaterialTheme.colorScheme.secondary,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        NavigationBarItem(
            selected = current == MainTab.PATIENTS,
            onClick = onPatients,
            icon = { Icon(Icons.Filled.Search, contentDescription = "Pacientes") },
            label = { Text("Pacientes") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.secondary,
                selectedTextColor = MaterialTheme.colorScheme.secondary,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
