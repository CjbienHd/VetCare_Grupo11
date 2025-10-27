package com.example.vetcare_grupo11.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vetcare_grupo11.data.PatientsStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class Patient(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val especie: String,  // "Perro" | "Gato"
    val raza: String,
    val tutor: String
)

class PatientsViewModel(private val store: PatientsStore) : ViewModel() {

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients

    val activeCount: StateFlow<Int> =
        patients.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        // 1) Cargar de disco
        val loaded = store.load()
        _patients.value = if (loaded.isNotEmpty()) {
            loaded
        } else {
            // Lista por defecto si no hay nada guardado
            listOf(
                Patient(nombre = "Luna",  especie = "Gato",  raza = "Doméstico de pelo corto", tutor = "María Gómez"),
                Patient(nombre = "Rocky", especie = "Perro", raza = "Labrador",                tutor = "Juan Pérez"),
                Patient(nombre = "Milo",  especie = "Gato",  raza = "Siames",                  tutor = "Camila Díaz"),
                Patient(nombre = "Toby",  especie = "Perro", raza = "Beagle",                  tutor = "Pedro Soto")
            )
        }

        // 2) Guardar automáticamente ante cambios
        viewModelScope.launch {
            patients.drop(1).collect { list -> store.save(list) }
        }
    }

    fun addPatient(p: Patient) {
        _patients.value = _patients.value + p
        // (También se guardará por el colector)
    }

    fun removePatient(id: String) {
        _patients.value = _patients.value.filterNot { it.id == id }
    }
}

/** Factory para inyectar el store sin acoplar a la UI */
class PatientsViewModelFactory(private val store: PatientsStore) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PatientsViewModel::class.java))
        return PatientsViewModel(store) as T
    }
}
