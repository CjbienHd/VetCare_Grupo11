package com.example.vetcare_grupo11.viewmodel



import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vetcare_grupo11.data.PatientsStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.vetcare_grupo11.network.RetrofitInstance
import java.util.UUID


//Define la estructura de los datos de pacientes
data class Patient(
    val id: String? = null,
    val nombre: String,
    val especie: String,
    val raza: String? = null,
    val tutor: String
)

class PatientsViewModel(
    private val store: PatientsStore
) : ViewModel() {

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients

    // Estado para saber si estamos cargando datos del backend
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val activeCount: StateFlow<Int> = patients
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        loadPatientsFromBackend()
    }

    private fun loadPatientsFromBackend() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Siempre intentamos cargar desde el backend primero
                val fromBackend = RetrofitInstance.api.getPatients()
                _patients.value = fromBackend // Actualizamos la lista con la respuesta (incluso si está vacía)
                store.save(fromBackend) // Guardamos en el caché local
            } catch (e: Exception) {
                // 2. Si el backend falla, cargamos desde el caché local como respaldo
                Log.e("ViewModel", "Error cargando desde backend, usando caché local", e)
                _patients.value = store.load()
            } finally {
                _isLoading.value = false // Dejamos de cargar, ya sea con éxito o con error
            }
        }
    }

    // Crea un nuevo paciente y lo envía al BACKEND
    fun addPatient(p: Patient) {
        viewModelScope.launch {
            val withId = if (p.id.isNullOrBlank()) {
                p.copy(id = UUID.randomUUID().toString())
            } else {
                p
            }

            try {
                val created = RetrofitInstance.api.createPatient(withId)
                _patients.value = _patients.value + created
                store.save(_patients.value) // Actualizar caché local
            } catch (e: Exception) {
                Log.e("ViewModel", "API call failed for addPatient", e)
                // Opcional: podrías añadirlo localmente para que el usuario no pierda el dato
                _patients.value = _patients.value + withId
            }
        }
    }

    // Elimina paciente en backend y luego en la lista local
    fun removePatient(id: String?) {
        if (id == null) return
        viewModelScope.launch {
            val oldList = _patients.value
            // Optimistic UI: lo borramos de la UI inmediatamente
            _patients.value = _patients.value.filterNot { it.id == id }
            try {
                RetrofitInstance.api.deletePatient(id)
                store.save(_patients.value) // Actualizar caché local
            } catch (e: Exception) {
                Log.e("ViewModel", "API call failed for removePatient", e)
                // Si falla, revertimos el cambio en la UI
                _patients.value = oldList
            }
        }
    }


    // Actualiza paciente en backend y luego en la lista local
    fun updatePatient(patient: Patient) {
        val id = patient.id ?: return

        viewModelScope.launch {
            try {
                val updatedFromBackend = RetrofitInstance.api.updatePatient(id, patient)
                _patients.value = _patients.value.map {
                    if (it.id == id) updatedFromBackend else it
                }
                store.save(_patients.value)
            } catch (e: Exception) {
                Log.e("ViewModel", "API call failed for updatePatient", e)
                // Fallback opcional si falla el backend
            }
        }
    }

    fun getPatient(id: String): Patient? {
        return patients.value.find { it.id == id }
    }
}

class PatientsViewModelFactory(private val store: PatientsStore) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PatientsViewModel::class.java))
        // Ya no necesita la dependencia de Firebase
        return PatientsViewModel(store) as T
    }
}
