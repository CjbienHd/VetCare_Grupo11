package com.example.vetcare_grupo11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class que representa el estado de la UI de la pantalla de Login.
 * Contiene todos los datos necesarios para dibujar la vista en cualquier momento.
 */
data class UiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val loading: Boolean = false,
    val success: Boolean = false
)

/**
 * ViewModel para la pantalla de Login.
 * Hereda de androidx.lifecycle.ViewModel para sobrevivir a cambios de configuración
 * y tener acceso a 'viewModelScope'.
 */
class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    fun onEmail(value: String) {
        // Al actualizar el email, borramos cualquier error previo en ese campo.
        _state.value = _state.value.copy(email = value, emailError = null)
    }

    fun onPass(value: String) {
        // Al actualizar la contraseña, borramos cualquier error previo en ese campo.
        _state.value = _state.value.copy(pass = value, passError = null)
    }

    /**
     * onCheck: función que chequea credenciales y devuelve true/false.
     * La pasamos desde la pantalla. Aquí solo gestionamos estado y validaciones.
     */
    fun login(onCheck: (email: String, pass: String) -> Boolean) {
        val s = _state.value
        var eErr: String? = null
        var pErr: String? = null

        // --- 1. Validación de campos ---
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
            eErr = "Email inválido"
        }
        if (s.pass.length < 6) {
            pErr = "Mínimo 6 caracteres"
        }

        // Si hay algún error de validación, actualizamos el estado y no continuamos.
        if (eErr != null || pErr != null) {
            _state.value = s.copy(emailError = eErr, passError = pErr)
            return
        }


        viewModelScope.launch {
            // Ponemos el estado en 'cargando' para mostrar un spinner en la UI.
            _state.value = _state.value.copy(loading = true)
            delay(600) // Simula una llamada de red para dar feedback de carga.

            // Llamamos a la función externa para verificar las credenciales.
            val ok = onCheck(s.email.trim(), s.pass)

            // Actualizamos el estado según el resultado.
            _state.value = if (ok) {
                _state.value.copy(loading = false, success = true)
            } else {
                _state.value.copy(loading = false, passError = "Credenciales incorrectas")
            }
        }
    }
}