@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.vetcare_grupo11.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onGoHome: () -> Unit,
    onGoReminders: () -> Unit,
    onGoPatients: () -> Unit
) {
    val ctx = LocalContext.current
    var showInfo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Configuración",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                actions = {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Ajustes",
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.secondary // <-- Resaltado
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            // Reutilizamos la barra de navegación principal
            MainBottomBar(
                current = MainTab.NONE, // No es una pestaña principal
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SettingsRowCard(
                icon = { Icon(Icons.Filled.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                text = "Cambiar Tema",
                trailing = {
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = onThemeChange
                    )
                },
                onClick = { onThemeChange(!darkTheme) }
            )

            SettingsRowCard(
                icon = { Icon(Icons.Filled.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                text = "Copia Local/ Exportar",
                onClick = {
                    val sp = ctx.getSharedPreferences("datos_app", android.content.Context.MODE_PRIVATE)
                    val usuarios = sp.getString("usuarios", "") ?: ""
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Backup VetCare")
                        putExtra(Intent.EXTRA_TEXT, "Usuarios registrados:\n$usuarios")
                    }
                    ctx.startActivity(Intent.createChooser(shareIntent, "Compartir copia"))
                }
            )

            SettingsRowCard(
                icon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                text = "Información",
                onClick = { showInfo = true }
            )

            if (showInfo) {
                AlertDialog(
                    onDismissRequest = { showInfo = false },
                    confirmButton = {
                        TextButton(onClick = { showInfo = false }) { Text("Cerrar") }
                    },
                    title = { Text("VetCare móvil") },
                    text = { Text("App demo de VetCare, versión 1.0.0.\n") }
                )
            }

            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
private fun SettingsRowCard(
    icon: @Composable () -> Unit,
    text: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(Modifier.weight(1f))
            trailing?.invoke()
        }
    }
}