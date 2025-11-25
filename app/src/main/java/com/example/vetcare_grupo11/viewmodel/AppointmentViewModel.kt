package com.example.vetcare_grupo11.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetcare_grupo11.network.RetrofitInstance
import com.example.vetcare_grupo11.notifications.programarRecordatorioCita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// Modelo de Cita
data class Appointment(
    val id: String? = null,
    val patientName: String,
    val motivo: String,
    val fechaHora: String,
    val fechaHoraMillis: Long,
    val notas: String = "",
    val estado: String = "Programada",
    val esVacuna: Boolean = false
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
                Log.e("AppointmentsViewModel", "Error cargando citas", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAppointment(a: Appointment, context: Context) {
        viewModelScope.launch {
            val withId = if (a.id.isNullOrBlank()) {
                a.copy(id = UUID.randomUUID().toString())
            } else a

            try {
                val created = RetrofitInstance.appointmentsApi.createAppointment(withId)
                _appointments.value = _appointments.value + created

                // Dejado en modo de prueba para depurar notificaciones
                programarRecordatorioCita(
                    context = context,
                    citaId = created.id!!,
                    pacienteNombre = created.patientName,
                    motivo = created.motivo,
                    fechaHoraMillis = created.fechaHoraMillis,
                    esVacuna = created.esVacuna,
                    testNow = true
                )

            } catch (e: Exception) {
                Log.e("AppointmentsViewModel", "Error al crear cita", e)
                _appointments.value = _appointments.value + withId
            }
        }
    }

    fun updateAppointment(a: Appointment) {
        val id = a.id ?: return
        viewModelScope.launch {
            val originalList = _appointments.value
            _appointments.value = originalList.map { if (it.id == id) a else it }
            try {
                RetrofitInstance.appointmentsApi.updateAppointment(id, a)
            } catch (e: Exception) {
                Log.e("AppointmentsViewModel", "Error al actualizar cita", e)
                _appointments.value = originalList
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
                Log.e("AppointmentsViewModel", "Error al eliminar cita", e)
                _appointments.value = old
            }
        }
    }

    fun getAppointment(id: String): Appointment? =
        appointments.value.find { it.id == id }
}
