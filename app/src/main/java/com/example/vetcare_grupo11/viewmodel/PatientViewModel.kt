package com.example.vetcare_grupo11.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vetcare_grupo11.data.PatientsStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// -------------------------
// Data class Patient
// -------------------------

// - Representa el modelo de datos (la entidad del dominio).
// - Cada paciente tiene un ID único generado
// - Es inmutable y se usa tanto en la UI como en el almacenamiento.
//
data class Patient(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val especie: String,  // "Perro" | "Gato"
    val raza: String,
    val tutor: String
)

// -------------------------
// ViewModel principal (PatientsViewModel)
// -------------------------

// - Es la capa intermedia entre la UI (Compose) y la persistencia (Store).
// - Maneja el estado reactivo con StateFlow.
// - Carga, guarda y actualiza la lista de pacientes.
//
class PatientsViewModel(private val store: PatientsStore) : ViewModel() {

    // Estado interno mutable
    private val _patients = MutableStateFlow<List<Patient>>(emptyList())

    // Exposición pública inmutable: la UI observa este flujo.
    val patients: StateFlow<List<Patient>> = _patients

    // Contador reactivo : cantidad de pacientes activos.
    // Se actualiza automáticamente cada vez que cambia la lista.
    val activeCount: StateFlow<Int> =
        patients.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        // 1) Cargar pacientes desde el almacenamiento al iniciar el ViewModel
        val loaded = store.load()

        // Si hay datos guardados, los uso; si no, creo lista por defecto
        _patients.value = if (loaded.isNotEmpty()) {
            loaded
        } else {
            listOf(
                Patient(nombre = "Luna",  especie = "Gato",  raza = "Doméstico de pelo corto", tutor = "María Gómez"),
                Patient(nombre = "Rocky", especie = "Perro", raza = "Labrador",                tutor = "Juan Pérez"),
                Patient(nombre = "Milo",  especie = "Gato",  raza = "Siames",                  tutor = "Camila Díaz")
            )
        }

        // 2) Guardar automáticamente ante cambios
        // - Esto usa una corrutina para observar cambios en el flujo "patients".
        // - Cada vez que cambia (se agrega o elimina un paciente), se guarda en SharedPreferences.
        viewModelScope.launch {
            patients.drop(1).collect { list -> store.save(list) }
        }
    }

    // -------------------------
    // Funciones públicas
    // -------------------------

    // Agregar paciente nuevo (la UI llama a esto desde AddPatientScreen)
    fun addPatient(p: Patient) {
        _patients.value = _patients.value + p

    }

    // Eliminar paciente por ID
    fun removePatient(id: String) {
        _patients.value = _patients.value.filterNot { it.id == id }

    }
}

// -------------------------
// ViewModelFactory
// -------------------------
// - Permite inyectar la dependencia "store" al crear el ViewModel.
// - Así evito acoplar el ViewModel directamente a una clase concreta.
// - Se usa en AppNavigation: viewModel(factory = PatientsViewModelFactory(...))
//
class PatientsViewModelFactory(private val store: PatientsStore) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PatientsViewModel::class.java))
        return PatientsViewModel(store) as T
    }
}
