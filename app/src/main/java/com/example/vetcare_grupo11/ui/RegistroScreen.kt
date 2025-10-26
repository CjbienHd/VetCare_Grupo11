@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.vetcare_grupo11.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.dp


// Reutilizo tu paleta
private val Teal = Color(0xFF00A9B9)
private val Coral = Color(0xFFFF6F61)
private val CardSoft = Color(0xFFE6F4F1)

@Composable
fun RegistroScreenSimple(
    goLogin: () -> Unit = {}
) {
    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }

    // Errores reactivos
    var errNombre by remember { mutableStateOf<String?>(null) }
    var errEmail by remember { mutableStateOf<String?>(null) }
    var errPass by remember { mutableStateOf<String?>(null) }
    var errPass2 by remember { mutableStateOf<String?>(null) }

    // Confirmación
    var registrado by remember { mutableStateOf(false) }

    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Crear cuenta",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Surface(
                color = CardSoft,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Regístrate 📝", style = MaterialTheme.typography.titleMedium)
                    Text("Completa los datos para crear tu usuario", color = Color.Black.copy(alpha = 0.65f))
                }
            }

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
                    // ======= NOMBRE =======
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            errNombre = null
                        },
                        label = { Text("Nombre") },
                        isError = errNombre != null,
                        supportingText = { errNombre?.let { Text(it) } },
                        trailingIcon = {
                            if (errNombre != null) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_dialog_alert),
                                    contentDescription = null,
                                    tint = Coral
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ======= EMAIL =======
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errEmail = null
                        },
                        label = { Text("Email") },
                        isError = errEmail != null,
                        supportingText = { errEmail?.let { Text(it) } },
                        trailingIcon = {
                            if (errEmail != null) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_dialog_alert),
                                    contentDescription = null,
                                    tint = Coral
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    var showPass by remember { mutableStateOf(false) }
                    var showPass2 by remember { mutableStateOf(false) }

                    // ======= CONTRASEÑA =======
                    OutlinedTextField(
                        value = pass,
                        onValueChange = {
                            pass = it
                            errPass = null
                        },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(), // NUEVO
                        isError = errPass != null,
                        supportingText = { errPass?.let { Text(it) } },
                        trailingIcon = {
                            Row {
                                if (errPass != null) {
                                    Icon(
                                        painter = painterResource(android.R.drawable.ic_dialog_alert),
                                        contentDescription = null,
                                        tint = Coral
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                TextButton(onClick = { showPass = !showPass }) {
                                    Text(if (showPass) "Ocultar" else "Mostrar")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )


                    // ======= REPETIR CONTRASEÑA =======
                    OutlinedTextField(
                        value = pass2,
                        onValueChange = {
                            pass2 = it
                            errPass2 = null
                        },
                        label = { Text("Repite contraseña") },
                        singleLine = true,
                        visualTransformation = if (showPass2) VisualTransformation.None else PasswordVisualTransformation(), // NUEVO
                        isError = errPass2 != null,
                        supportingText = { errPass2?.let { Text(it) } },
                        trailingIcon = {
                            Row {
                                if (errPass2 != null) {
                                    Icon(
                                        painter = painterResource(android.R.drawable.ic_dialog_alert),
                                        contentDescription = null,
                                        tint = Coral
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                TextButton(onClick = { showPass2 = !showPass2 }) {
                                    Text(if (showPass2) "Ocultar" else "Mostrar")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )


                    Button(
                        onClick = {

                            var ok = true
                            if (nombre.trim().length < 3) {
                                errNombre = "Mínimo 3 caracteres"
                                ok = false
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errEmail = "Email inválido"
                                ok = false
                            }
                            if (pass.length < 6) {
                                errPass = "Mínimo 6 caracteres"
                                ok = false
                            }
                            if (pass != pass2) {
                                errPass2 = "No coinciden"
                                ok = false
                            }
                            if (!ok) return@Button

                            val guardado = saveUserIfNew(ctx, nombre.trim(), email.trim(), pass)
                            if (!guardado) {
                                errEmail = "Este correo ya está registrado"
                            } else {
                                registrado = true
                                // Vuelve al login
                                goLogin()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Registrarme", color = Color.White)
                    }

                    if (registrado) {
                        Text(
                            text = "Registro exitoso",
                            color = Teal,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    TextButton(
                        onClick = goLogin,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Ya tengo cuenta", color = Teal)
                    }
                }
            }
        }
    }
}

/** Guarda usuario si NO existe el email. Formato: email|pass|nombre;email|pass|nombre;... */
private fun saveUserIfNew(
    ctx: android.content.Context,
    nombre: String,
    email: String,
    pass: String
): Boolean {
    val sp = ctx.getSharedPreferences("datos_app", android.content.Context.MODE_PRIVATE)
    val raw = sp.getString("usuarios", "") ?: ""
    val entries = raw.split(";").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()

    val exists = entries.any {
        val parts = it.split("|")
        parts.isNotEmpty() && parts[0] == email
    }
    if (exists) return false

    // Formato simple: email|pass|nombre
    entries.add("$email|$pass|$nombre")
    val joined = entries.joinToString(";")
    sp.edit().putString("usuarios", joined).apply()
    return true
}