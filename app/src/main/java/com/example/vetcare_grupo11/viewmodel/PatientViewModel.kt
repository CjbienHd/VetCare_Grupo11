package com.example.vetcare_grupo11.viewmodel


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.lifecycle.viewModelScope
import java.util.UUID

data class Patient(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val especie: String,
    val raza: String,
    val tutor: String
)

class PatientsViewModel : ViewModel() {

    private val _patients = MutableStateFlow(
        listOf(
            Patient(nombre = "Luna",  especie = "Gato",  raza = "Doméstico de pelo corto", tutor = "María Gómez"),
            Patient(nombre = "Rocky", especie = "Perro", raza = "Labrador",                tutor = "Juan Pérez"),
            Patient(nombre = "Milo",  especie = "Gato",  raza = "Siames",                  tutor = "Camila Díaz"),
            Patient(nombre = "Toby",  especie = "Perro", raza = "Beagle",                  tutor = "Pedro Soto")
        )
    )
    val patients: StateFlow<List<Patient>> = _patients

    val activeCount: StateFlow<Int> =
        patients.map { it.size }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun addPatient(p: Patient) {
        _patients.value = _patients.value + p
    }
    fun removePatient(id: String) {
        _patients.value = _patients.value.filterNot { it.id == id }
    }
}

