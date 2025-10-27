@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.vetcare_grupo11.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vetcare_grupo11.viewmodel.Patient

@Composable
fun AddPatientScreen(
    onBack: () -> Unit,                  // volver a la pantalla anterior (patients)
    onSave: (Patient) -> Unit,           // callback: el VM agrega y persiste
    onGoHome: () -> Unit,                // por si quiero saltar a Home desde la bottom bar
    onGoPatients: () -> Unit             // por si quiero volver a Patients desde la bottom bar
) {
    // Estados locales del formulario
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("Perro") } // Perro | Gato
    var raza by remember { mutableStateOf("") }
    var tutor by remember { mutableStateOf("") }

    // Control del menú desplegable
    var especieExpanded by remember { mutableStateOf(false) }
    val especies = listOf("Perro", "Gato")

    Scaffold(
        // App bar con botón de volver
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "VetCare Móvil",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Icono genérico para “volver”. Mantengo coherencia visual con la app.
                        Icon(Icons.Default.Badge, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,

        // (Home / Patients)
        bottomBar = {
            SettingsBottomBar(
                current = MainTab.PATIENTS, // tab activo: Patients
                onReminders = { /* aquí no hay recordatorios */ },
                onHome = onGoHome,
                onPatients = onGoPatients
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Encabezado visual del formulario
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Agregar paciente 🐾",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Completa la información para registrar",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Card con los campos del formulario
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Nombre del paciente.
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Selector de especie
                    ExposedDropdownMenuBox(
                        expanded = especieExpanded,
                        onExpandedChange = { especieExpanded = !especieExpanded }
                    ) {
                        OutlinedTextField(
                            value = especie,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Especie") },
                            leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = especieExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = especieExpanded,
                            onDismissRequest = { especieExpanded = false }
                        ) {
                            especies.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        especie = opt
                                        especieExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Raza.
                    OutlinedTextField(
                        value = raza,
                        onValueChange = { raza = it },
                        label = { Text("Raza") },
                        leadingIcon = { Icon(Icons.Default.Style, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tutor (dueño).
                    OutlinedTextField(
                        value = tutor,
                        onValueChange = { tutor = it },
                        label = { Text("Tutor") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Guardar y Cancelar
                    Button(
                        onClick = {
                            val p = Patient(
                                nombre = nombre.trim(),
                                especie = especie,
                                raza = raza.trim(),
                                tutor = tutor.trim()
                            )
                            onSave(p)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(16.dp),

                        enabled = nombre.isNotBlank() && raza.isNotBlank() && tutor.isNotBlank()
                    ) {
                        Text("Guardar", color = MaterialTheme.colorScheme.onSecondary)
                    }

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
