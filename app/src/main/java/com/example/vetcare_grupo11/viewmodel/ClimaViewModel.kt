package com.example.vetcare_grupo11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetcare_grupo11.data.ClimaRetrofit
import com.example.vetcare_grupo11.data.RespuestaClima
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EstadoClimaUi(
    val cargando: Boolean = false,
    val datosClima: RespuestaClima? = null,
    val error: String? = null
)

class ClimaViewModel : ViewModel() {

    private val _estado = MutableStateFlow(EstadoClimaUi())
    val estado: StateFlow<EstadoClimaUi> = _estado

    /**
     * Coordenadas de la clínica (ejemplo)
     */
    fun cargarClimaClinica(
        latitud: Double = -33.0245,
        longitud: Double = -71.5518
    ) {
        // Si ya tengo datos o ya estoy cargando, no vuelvo a llamar
        if (_estado.value.datosClima != null || _estado.value.cargando) return

        _estado.value = _estado.value.copy(cargando = true, error = null)

        viewModelScope.launch {
            try {
                val respuesta = ClimaRetrofit.api.obtenerClimaActual(latitud, longitud)
                _estado.value = _estado.value.copy(
                    cargando = false,
                    datosClima = respuesta,
                    error = null
                )
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(
                    cargando = false,
                    error = e.message ?: "Error al cargar datos del clima"
                )
            }
        }
    }
}