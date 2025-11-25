@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.vetcare_grupo11.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
    val isEditMode = patientToEdit != null
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("Perro") }
    var raza by remember { mutableStateOf("") }
    var tutor by remember { mutableStateOf("") }
    var fotoPacienteUri by remember { mutableStateOf<String?>(null) }

    var especieExpanded by remember { mutableStateOf(false) }
    val especies = listOf("Perro", "Gato")

    val lanzadorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                fotoPacienteUri = uri.toString()
            }
        }
    )

    LaunchedEffect(key1 = patientToEdit) {
        if (isEditMode && patientToEdit != null) {
            nombre = patientToEdit.nombre
            especie = patientToEdit.especie
            raza = patientToEdit.raza ?: ""
            tutor = patientToEdit.tutor
            fotoPacienteUri = patientToEdit.fotoUri
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
        bottomBar = {
            MainBottomBar(
                current = MainTab.PATIENTS,
                onReminders = { },
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

            // Sección de Foto del Paciente
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (fotoPacienteUri != null) {
                    AsyncImage(
                        model = fotoPacienteUri,
                        contentDescription = "Foto del paciente",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "Icono Paciente",
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { lanzadorGaleria.launch("image/*") }) {
                    Text("Seleccionar Foto")
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
                            modifier = Modifier.menuAnchor().fillMaxWidth()
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

                    Button(
                        onClick = {
                            val p = Patient(
                                id = patientToEdit?.id ?: UUID.randomUUID().toString(),
                                nombre = nombre.trim(),
                                especie = especie,
                                raza = raza.trim(),
                                tutor = tutor.trim(),
                                fotoUri = fotoPacienteUri
                            )
                            onSave(p)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(16.dp),
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
