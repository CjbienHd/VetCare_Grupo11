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
import java.util.UUID

@Composable
fun AddPatientScreen(
    patientToEdit: Patient?,
    onBack: () -> Unit,
    onSave: (Patient) -> Unit,
    onGoHome: () -> Unit,
    onGoPatients: () -> Unit
) {
    val isEditMode = patientToEdit != null // Determina si estamos en modo edición
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("Perro") } // Perro | Gato
    var raza by remember { mutableStateOf("") }
    var tutor by remember { mutableStateOf("") }
    //Booleano dedicado a controlar el menu desplegable
    var especieExpanded by remember { mutableStateOf(false) }
    val especies = listOf("Perro", "Gato")

    LaunchedEffect(key1 = patientToEdit) {
        if (isEditMode) {
            nombre = patientToEdit?.nombre ?: ""
            especie = patientToEdit?.especie ?: "Perro"
            raza = patientToEdit?.raza ?: ""
            tutor = patientToEdit?.tutor ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Paciente" else "Agregar Paciente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Badge, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        //Reutiliza un componente indicando que la pestaña actual es la de PATIENTS.
        bottomBar = {
            SettingsBottomBar(
                current = MainTab.PATIENTS,
                onReminders = { },
                onHome = onGoHome,
                onPatients = onGoPatients
            )
        }
        //inner = cuánto espacio debe dejar en los bordes para no quedar oculto detrás de la TopAppBar o la BottomAppBar.
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

            // Tarjeta de cabecera
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEditMode) "Modificar paciente ✍️" else "Agregar paciente 🐾",
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

            // Card del formulario
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
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Selector de especie (exposed dropdown)
                    ExposedDropdownMenuBox(
                        //Aqui se configura para alternar el menu desplegable
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
                        //Es el menu flotante. Solo se muestra si especieExpanded es true.
                        ExposedDropdownMenu(
                            expanded = especieExpanded,
                            onDismissRequest = { especieExpanded = false }
                        ) {
                            //Crea un DropdownMenuItem para cada especie en la lista
                            especies.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    //Actualiza el estado de especie y se cierra el menu
                                    onClick = {
                                        especie = opt
                                        especieExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = raza,
                        onValueChange = { raza = it },
                        label = { Text("Raza") },
                        leadingIcon = { Icon(Icons.Default.Style, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tutor,
                        onValueChange = { tutor = it },
                        label = { Text("Tutor") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Botones
                    Button(
                        onClick = {
                            val p = Patient(
                                id = patientToEdit?.id ?: UUID.randomUUID().toString(),
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
                        //Desabilita el boton si alguno de los campo de textos esta vacio
                        enabled = nombre.isNotBlank() && raza.isNotBlank() && tutor.isNotBlank()
                    ) {
                        Text(if (isEditMode) "Actualizar" else "Guardar")
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
