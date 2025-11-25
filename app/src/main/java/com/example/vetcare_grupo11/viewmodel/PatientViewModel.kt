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

data class Patient(
    val id: String? = null,
    val nombre: String,
    val especie: String,
    val raza: String? = null,
    val tutor: String,
    val fotoUri: String? = null
)

class PatientsViewModel(
    private val store: PatientsStore
) : ViewModel() {

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            _patients.value = store.load()
            try {
                val fromBackend = RetrofitInstance.api.getPatients()
                if (fromBackend.isNotEmpty()) {
                    _patients.value = fromBackend
                    store.save(fromBackend)
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error cargando desde backend, se mantiene caché local", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPatient(p: Patient) {
        viewModelScope.launch {
            val withId = if (p.id.isNullOrBlank()) {
                p.copy(id = UUID.randomUUID().toString())
            } else {
                p
            }

            // --- UI OPTIMISTA (Añadir es una operación de bajo riesgo) ---
            val originalList = _patients.value
            _patients.value = originalList + withId
            store.save(_patients.value)

            try {
                val created = RetrofitInstance.api.createPatient(withId)
                _patients.value = originalList + created
                store.save(_patients.value)
            } catch (e: Exception) {
                Log.e("ViewModel", "Fallo al añadir en backend, se mantiene en local", e)
            }
        }
    }

    // --- LÓGICA DE BORRADO SEGURA (PESIMISTA) ---
    fun removePatient(id: String?) {
        if (id == null) return
        viewModelScope.launch {
            val originalList = _patients.value
            try {
                // 1. Primero intentar borrar en el backend
                RetrofitInstance.api.deletePatient(id)
                
                // 2. Si tiene éxito, actualizar UI y caché local
                _patients.value = originalList.filterNot { it.id == id }
                store.save(_patients.value)

            } catch (e: Exception) {
                Log.e("ViewModel", "Fallo al borrar en backend, no se hacen cambios", e)
                // Opcional: Mostrar un snackbar o toast con el error al usuario
            }
        }
    }

    fun updatePatient(patient: Patient) {
        val id = patient.id ?: return
        viewModelScope.launch {
            val originalList = _patients.value
            // UI Optimista para que la edición se sienta rápida
            _patients.value = originalList.map { if (it.id == id) patient else it }
            store.save(_patients.value)

            try {
                RetrofitInstance.api.updatePatient(id, patient)
            } catch (e: Exception) {
                Log.e("ViewModel", "Fallo al actualizar en backend, revirtiendo", e)
                _patients.value = originalList
                store.save(originalList)
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
        return PatientsViewModel(store) as T
    }
}
