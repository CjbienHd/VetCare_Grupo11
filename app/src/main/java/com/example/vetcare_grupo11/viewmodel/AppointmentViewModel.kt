package com.example.vetcare_grupo11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetcare_grupo11.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// Modelo de Cita
data class Appointment(
    val id: String? = null,
    val patientName: String,
    val motivo: String,
    val fechaHora: String,   // ej: "2025-11-30 10:30"
    val notas: String = "",
    val estado: String = "Programada"
)

// ViewModel para manejar la lista de citas
class AppointmentsViewModel : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadFromBackend()
    }

    fun loadFromBackend() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fromBackend = RetrofitInstance.appointmentsApi.getAppointments()
                _appointments.value = fromBackend
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAppointment(a: Appointment) {
        viewModelScope.launch {
            val withId = if (a.id.isNullOrBlank()) {
                a.copy(id = UUID.randomUUID().toString())
            } else a

            try {
                val created = RetrofitInstance.appointmentsApi.createAppointment(withId)
                _appointments.value = _appointments.value + created
            } catch (e: Exception) {
                // fallback: añadir localmente aunque no se haya creado en backend
                _appointments.value = _appointments.value + withId
            }
        }
    }

    fun updateAppointment(a: Appointment) {
        val id = a.id ?: return
        viewModelScope.launch {
            try {
                val updated = RetrofitInstance.appointmentsApi.updateAppointment(id, a)
                _appointments.value = _appointments.value.map {
                    if (it.id == id) updated else it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeAppointment(id: String?) {
        if (id == null) return
        viewModelScope.launch {
            val old = _appointments.value
            _appointments.value = old.filterNot { it.id == id }
            try {
                RetrofitInstance.appointmentsApi.deleteAppointment(id)
            } catch (e: Exception) {
                // si falla, restauramos la lista anterior
                _appointments.value = old
            }
        }
    }

    fun getAppointment(id: String): Appointment? =
        appointments.value.find { it.id == id }
}


