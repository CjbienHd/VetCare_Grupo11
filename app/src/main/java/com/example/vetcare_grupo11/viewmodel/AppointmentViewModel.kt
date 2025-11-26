package com.example.vetcare_grupo11.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetcare_grupo11.network.AppointmentApiService
import com.example.vetcare_grupo11.network.RetrofitInstance
import com.example.vetcare_grupo11.notifications.programarRecordatorioCita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
class AppointmentsViewModel(
    private val api: AppointmentApiService = RetrofitInstance.appointmentsApi
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadFromBackend()
    }

    fun loadFromBackend() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val backendAppointments = api.getAppointments()
                _appointments.value = backendAppointments
            } catch (e: Exception) {
                Log.e("AppointmentsViewModel", "Error al cargar citas", e)
                _appointments.value = emptyList()
                _errorMessage.value = "No se pudo cargar las citas. Intenta nuevamente."
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
                val created = api.createAppointment(withId)
                _appointments.value = _appointments.value + created

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
                _errorMessage.value = "No se pudo crear la cita."
            }
        }
    }

    fun updateAppointment(a: Appointment) {
        val id = a.id ?: return
        viewModelScope.launch {
            val originalList = _appointments.value
            _appointments.value = originalList.map { if (it.id == id) a else it }
            try {
                api.updateAppointment(id, a)
            } catch (e: Exception) {
                Log.e("AppointmentsViewModel", "Error al actualizar cita", e)
                _appointments.value = originalList
                _errorMessage.value = "No se pudo actualizar la cita."
            }
        }
    }

    fun deleteAppointment(id: String) {
        viewModelScope.launch {
            try {
                api.deleteAppointment(id)
                _appointments.value = _appointments.value.filterNot { it.id == id }
            } catch (e: Exception) { 
                Log.e("AppointmentsViewModel", "Error al eliminar cita", e)
                _errorMessage.value = "No se pudo eliminar la cita."
            }
        }
    }

    fun getAppointment(id: String): Appointment? {
        return _appointments.value.find { it.id == id }
    }
}
