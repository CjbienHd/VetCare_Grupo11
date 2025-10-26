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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Colores del caso
private val Teal = Color(0xFF00A9B9)
private val Coral = Color(0xFFFF6F61)
private val CardSoft = Color(0xFFE6F4F1)

@Composable
fun LoginVisualScreen(
    onCreateAccount: () -> Unit = {},
    onLoginOk: () -> Unit = {} // callback opcional para navegar cuando el login sea correcto
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // NUEVO: estados de validación/feedback
    var emailError by remember { mutableStateOf<String?>(null) }
    var passError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var loginOk by remember { mutableStateOf(false) }

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

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

                // Tarjetita suave de bienvenida
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
                        // ======== CORREO =========
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = null // limpia error reactivo
                            },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Teal) },
                            label = { Text("Correo") },
                            singleLine = true,
                            isError = emailError != null,
                            supportingText = { emailError?.let { Text(it) } },
                            trailingIcon = {
                                if (emailError != null) {
                                    Icon(
                                        painter = painterResource(android.R.drawable.ic_dialog_alert),
                                        contentDescription = null,
                                        tint = Coral
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // ======== CONTRASEÑA =========
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passError = null // limpia error reactivo
                            },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Teal) },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                Row {
                                    if (passError != null) {
                                        Icon(
                                            painter = painterResource(android.R.drawable.ic_dialog_alert),
                                            contentDescription = null,
                                            tint = Coral
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    TextButton(onClick = { showPassword = !showPassword }) {
                                        Text(if (showPassword) "Ocultar" else "Mostrar")
                                    }
                                }
                            },
                            isError = passError != null,
                            supportingText = { passError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Botón coral, ancho completo (con feedback de carga)
                        Button(
                            onClick = {
                                // Validaciones básicas (rúbrica IE 2.1.2 / 2.2.1)
                                var ok = true
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    emailError = "Email inválido"
                                    ok = false
                                }
                                if (password.length < 6) {
                                    passError = "Mínimo 6 caracteres"
                                    ok = false
                                }
                                if (!ok) return@Button

                                loading = true
                                loginOk = false
                                scope.launch {
                                    // pequeño delay: feedback visual
                                    delay(600)
                                    val acceso = checkCredentials(ctx, email.trim(), password)
                                    loading = false
                                    if (acceso) {
                                        loginOk = true
                                        onLoginOk() // deja tu navegación aquí si ya la tienes configurada
                                    } else {
                                        passError = "Credenciales incorrectas"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Coral),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !loading
                        ) {
                            if (loading) {
                                CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Text("Ingresar", color = Color.White)
                            }
                        }

                        // Mensaje simple de éxito (visual)
                        if (loginOk) {
                            Text(
                                text = "Inicio de sesión correcto",
                                color = Teal,
                                style = MaterialTheme.typography.bodyMedium
                            )
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

            // Barra inferior de navegación (decorativa, como tu ejemplo)
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
                    // Placeholder simple
                    Text("·  ·  ·", color = Teal.copy(alpha = 0.6f))
                }
            }
        }
    }
}

/**
 * Valida contra usuarios guardados en SharedPreferences por el Registro.
 * Formato: "email|pass|nombre;email|pass|nombre;..."
 */
private fun checkCredentials(
    ctx: android.content.Context,
    email: String,
    pass: String
): Boolean {
    val sp = ctx.getSharedPreferences("datos_app", android.content.Context.MODE_PRIVATE)
    val raw = sp.getString("usuarios", "") ?: ""
    return raw.split(";")
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .any {
            val parts = it.split("|")
            parts.size >= 2 && parts[0] == email && parts[1] == pass
        }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginVisual() {
    MaterialTheme { LoginVisualScreen() }
}
