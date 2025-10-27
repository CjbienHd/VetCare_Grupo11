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

private val Teal = Color(0xFF00A9B9)
private val Coral = Color(0xFFFF6F61)
private val CardSoft = Color(0xFFE6F4F1)

@Composable
fun AddPatientScreen(
    onBack: () -> Unit,
    onSave: (Patient) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("Perro") } // Perro | Gato
    var raza by remember { mutableStateOf("") }
    var tutor by remember { mutableStateOf("") }

    var especieExpanded by remember { mutableStateOf(false) }
    val especies = listOf("Perro", "Gato")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "VetCare Móvil",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Badge, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Teal)
            )
        },
        containerColor = Color.White
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
            Surface(color = CardSoft, shape = RoundedCornerShape(20.dp)) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Agregar paciente 🐾",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                    Text(
                        "Completa la información para registrar",
                        color = Color.Black.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Card del formulario
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null, tint = Teal) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Selector de especie (exposed dropdown)
                    ExposedDropdownMenuBox(
                        expanded = especieExpanded,
                        onExpandedChange = { especieExpanded = !especieExpanded }
                    ) {
                        OutlinedTextField(
                            value = especie,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Especie") },
                            leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null, tint = Teal) },
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

                    OutlinedTextField(
                        value = raza,
                        onValueChange = { raza = it },
                        label = { Text("Raza") },
                        leadingIcon = { Icon(Icons.Default.Style, contentDescription = null, tint = Teal) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tutor,
                        onValueChange = { tutor = it },
                        label = { Text("Tutor") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Teal) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Botones
                    Button(
                        onClick = {
                            // solo UI: armamos el Patient y lo devolvemos
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
                        colors = ButtonDefaults.buttonColors(containerColor = Coral),
                        shape = RoundedCornerShape(16.dp),
                        enabled = nombre.isNotBlank() && raza.isNotBlank() && tutor.isNotBlank()
                    ) {
                        Text("Guardar", color = Color.White)
                    }

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Cancelar", color = Teal)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
