@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)


package com.example.vetcare_grupo11.ui


import androidx.compose.foundation.background
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

// Colores del caso
private val Teal = Color(0xFF00A9B9)
private val Coral = Color(0xFFFF6F61)
private val CardSoft = Color(0xFFE6F4F1)

@Composable
fun LoginVisualScreen(onCreateAccount: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }



    Scaffold(
        topBar = {
            // AppBar teal como en tu ejemplo
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "VetCare Móvil",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Teal)
            )
        },
        containerColor = Color.White
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .align(Alignment.TopCenter),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // Tarjetitas suaves para mantener el lenguaje del dashboard
                Surface(
                    color = CardSoft,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Bienvenido 👋", style = MaterialTheme.typography.titleMedium)
                        Text("Inicia sesión para continuar", color = Color.Black.copy(alpha = 0.65f))
                    }
                }

                // Card principal del formulario (bordes suaves)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Teal) },
                            label = { Text("Correo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Teal) },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                TextButton(onClick = { showPassword = !showPassword }) {
                                    Text(if (showPassword) "Ocultar" else "Mostrar")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Botón coral, ancho completo
                        Button(
                            onClick = { /* solo visual */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Coral),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Ingresar", color = Color.White)
                        }

                        TextButton(
                            onClick = onCreateAccount,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Crear cuenta", color = Teal)
                        }

                    }
                }
            }

            // Barra inferior de navegación (solo para mantener coherencia visual del ejemplo)
            Surface(
                color = Color(0xFFE5F1F1),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(Color(0xFFE5F1F1))
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder simple (no funcional) que imita el pie del ejemplo
                    Text("·  ·  ·", color = Teal.copy(alpha = 0.6f))
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewLoginVisual() {
    MaterialTheme { LoginVisualScreen() }
}


