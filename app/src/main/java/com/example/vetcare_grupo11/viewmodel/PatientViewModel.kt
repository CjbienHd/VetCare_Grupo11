package com.example.vetcare_grupo11.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vetcare_grupo11.data.FirebasePatientsSync
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

    private val remote = FirebasePatientsSync()   // 👈 nueva línea

    //Es mutable y privada, solo ViewModel puede modificarla
    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    //Esta es publica e inmutable, se expone a la UI
    val patients: StateFlow<List<Patient>> = _patients
    //Transoforma la lista de pacientes a un numero(total de pacientes)
    val activeCount: StateFlow<Int> =
        patients.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        // 1) Cargar lo que haya en local (SharedPreferences)
        val loadedLocal = store.load()
        _patients.value = if (loadedLocal.isNotEmpty()) {
            loadedLocal
        } else {
            // tu lista por defecto:
            listOf(
                Patient(nombre = "Luna",  especie = "Gato",  raza = "Doméstico de pelo corto", tutor = "María Gómez"),
                Patient(nombre = "Rocky", especie = "Perro", raza = "Labrador",                tutor = "Juan Pérez"),
                Patient(nombre = "Milo",  especie = "Gato",  raza = "Siames",                  tutor = "Camila Díaz"),
                Patient(nombre = "Toby",  especie = "Perro", raza = "Beagle",                  tutor = "Pedro Soto")
            )
        }

        // 2) En segundo plano, intentar cargar desde Firebase
        viewModelScope.launch {
            try {
                val remotePatients = remote.downloadPatients()
                if (remotePatients.isNotEmpty()) {
                    // Sobrescribe con los datos de la nube
                    _patients.value = remotePatients
                    // Y guarda también en local
                    store.save(remotePatients)
                }
            } catch (e: Exception) {
                // Si falla Firebase, te quedas con lo local sin romper la app
            }
        }

        // 3) Cada vez que cambian los pacientes, guarda en local y sube a Firebase
        viewModelScope.launch {
            patients
                .drop(1) // ignorar el valor inicial
                .collect { list ->
                    store.save(list)          // SharedPreferences
                    list.forEach { remote.uploadPatient(it) }  // Firebase
                }
        }
    }
    //Crea una nueva lista basada en la lista anterior y se le asigna a la lista mutable de pacientes
    fun addPatient(p: Patient) {
        _patients.value = _patients.value + p
    }
    //Crea una nueva lista basada en la lista anterior y se le asigna a la lista mutable de pacientes
    fun removePatient(id: String) {
        _patients.value = _patients.value.filterNot { it.id == id }
        remote.deletePatient(id) // 👈 borra en la nube
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
