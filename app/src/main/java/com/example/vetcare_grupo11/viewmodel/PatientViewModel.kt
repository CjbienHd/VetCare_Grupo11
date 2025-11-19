package com.example.vetcare_grupo11.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vetcare_grupo11.data.PatientsStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID


//Define la estructura de los datos de pacientes
data class Patient(
    // Id único generado automáticamente
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val especie: String,  // "Perro" | "Gato"
    val raza: String,
    val tutor: String
)
//Hereda de ViewModel, lo que le permite sobrevivir a cambios de configuración (como girar el teléfono) y ser gestionado por el framework de Android.
class PatientsViewModel(private val store: PatientsStore) : ViewModel() {
    //Es mutable y privada, solo ViewModel puede modificarla
    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    //Esta es publica e inmutable, se expone a la UI
    val patients: StateFlow<List<Patient>> = _patients
    //Transoforma la lista de pacientes a un numero(total de pacientes)
    val activeCount: StateFlow<Int> =
        patients.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        //Cargar el disco para recuperar los pacientes guardados previamente
        val loaded = store.load()
        //Si no hay pacientes guardados, se cargan los por defecto
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

        // Se lanza corrutina para guardar los pacientes cada vez que cambian
        viewModelScope.launch {
            // Cada vez que se actualiza la lista de pacientes, se guarda, excepto el valor inicial, para que no guarde los datos que se cargaron al inicio
            patients.drop(1).collect { list -> store.save(list) }
        }
    }
    //Crea una nueva lista basada en la lista anterior y se le asigna a la lista mutable de pacientes
    fun addPatient(p: Patient) {
        _patients.value = _patients.value + p
    }
    //Crea una nueva lista basada en la lista anterior y se le asigna a la lista mutable de pacientes
    fun removePatient(id: String) {
        _patients.value = _patients.value.filterNot { it.id == id }
    }

    // Dentro de la clase PatientsViewModel

    fun updatePatient(patient: Patient) {
        _patients.value = _patients.value.map {
            if (it.id == patient.id) {
                // Si encontramos el paciente por su ID, lo reemplazamos por el nuevo
                patient
            } else {
                // Si no, dejamos el que ya estaba
                it
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
