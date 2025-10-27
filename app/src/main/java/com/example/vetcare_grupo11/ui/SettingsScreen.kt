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
import androidx.compose.material.icons.filled.Search
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
    //Booleano para saber si el tema oscuro esta activado o no
    darkTheme: Boolean,
    //Booleano que si es true muestra el tema claro, si es false, muestra el tema oscuro
    onThemeChange: (Boolean) -> Unit,
    onGoHome: () -> Unit,
    currentTab: MainTab = MainTab.HOME,
    onGoReminders: () -> Unit = {},
    onGoPatients: () -> Unit = {}
) {
    val ctx = LocalContext.current
    //Booleano que si es true muestra la informacion, si es false, no
    var showInfo by remember { mutableStateOf(false) }

    Scaffold(
        // Barra superior con título y engranaje
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
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        // Barra inferior
        bottomBar = {
            SettingsBottomBar(
                current = currentTab,
                onReminders = onGoReminders,
                onHome = onGoHome,
                onPatients = onGoPatients
            )
        },
        containerColor = MaterialTheme.colorScheme.background
        //inner = cuánto espacio debe dejar en los bordes para no quedar oculto detrás de la TopAppBar o la BottomAppBar.
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Permite alternar entre claro y oscuro
            SettingsRowCard(
                icon = { Icon(Icons.Filled.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                text = "Cambiar Tema",
                trailing = {
                    Switch(
                        //Muestra el estado del Switch (tema claro o oscuro)
                        checked = darkTheme,
                        //Cuando se interactua con el switch se invoca la funcion onThemeChange que se encarga de cambiar el tema
                        onCheckedChange = onThemeChange
                    )
                },
                onClick = { onThemeChange(!darkTheme) }
            )

             // Exportar Datos
            SettingsRowCard(
                icon = { Icon(Icons.Filled.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                text = "Copia Local/ Exportar",
                onClick = {
                    // Acceso a SharedPreferences para leer datos guardados de los usuarios
                    val sp = ctx.getSharedPreferences("datos_app", android.content.Context.MODE_PRIVATE)
                    val usuarios = sp.getString("usuarios", "") ?: ""
                    // Intent para compartir el contenido
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        // Añade el contenido a compartir como EXTRA_TEXT.
                        putExtra(Intent.EXTRA_SUBJECT, "Backup VetCare")
                        putExtra(Intent.EXTRA_TEXT, "Usuarios registrados:\n$usuarios")
                    }
                    // Abre el selector de aplicaciones para compartir
                    ctx.startActivity(Intent.createChooser(shareIntent, "Compartir copia"))
                }
            )

            // === Card 3: Información ===
            SettingsRowCard(
                icon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                text = "Información",
                onClick = { showInfo = true }
            )

            // Dialog información
            if (showInfo) {
                AlertDialog(
                    onDismissRequest = { showInfo = false },
                    confirmButton = {
                        TextButton(onClick = { showInfo = false }) { Text("Cerrar") }
                    },
                    title = { Text("VetCare móvil") },
                    text = {
                        Text(
                            "App demo de VetCare, versión 1.0.0.\n"
                        )
                    }
                )
            }

            Spacer(Modifier.height(72.dp)) // espacio para bottom bar
        }
    }
}

/** Fila-card como en el mockup: icono coral a la izquierda, texto y trailing opcional */
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

/** Bottom bar igual a Home */
@Composable
fun SettingsBottomBar(
    current: MainTab,
    onReminders: () -> Unit,
    onHome: () -> Unit,
    onPatients: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        NavigationBarItem(
            selected = current == MainTab.REMINDERS,
            onClick = onReminders,
            icon = { Icon(imageVector = Icons.Outlined.Notifications, contentDescription = "Citas") },
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
            label = { Text("Home", color = if (current == MainTab.HOME) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant) }
        )
        NavigationBarItem(
            selected = current == MainTab.PATIENTS,
            onClick = onPatients,
            icon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Pacientes") },
            label = { Text("Pacientes") }
        )
    }
}
