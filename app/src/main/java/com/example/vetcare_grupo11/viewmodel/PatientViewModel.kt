package com.example.vetcare_grupo11.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vetcare_grupo11.data.PatientsStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.vetcare_grupo11.network.RetrofitInstance
import java.io.File
import java.io.FileOutputStream
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

    private fun saveImageToInternalStorage(context: Context, uriString: String?): String? {
        if (uriString == null || !uriString.startsWith("content://")) {
            return uriString
        }
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            Log.e("ViewModel", "Error copying image", e)
            null
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            val localPatients = store.load()
            _patients.value = localPatients
            try {
                val fromBackend = RetrofitInstance.api.getPatients()
                if (fromBackend.isNotEmpty()) {
                    val mergedPatients = fromBackend.map { backendPatient ->
                        val localPatient = localPatients.find { it.id == backendPatient.id }
                        backendPatient.copy(fotoUri = localPatient?.fotoUri)
                    }
                    _patients.value = mergedPatients
                    store.save(mergedPatients)
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error cargando desde backend, se mantiene caché local", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPatient(p: Patient, context: Context) {
        viewModelScope.launch {
            val savedImageUri = saveImageToInternalStorage(context, p.fotoUri)
            val patientWithPermanentImage = p.copy(fotoUri = savedImageUri)

            val withId = if (patientWithPermanentImage.id.isNullOrBlank()) {
                patientWithPermanentImage.copy(id = UUID.randomUUID().toString())
            } else {
                patientWithPermanentImage
            }

            val newList = _patients.value + withId
            _patients.value = newList
            store.save(newList)

            try {
                val created = RetrofitInstance.api.createPatient(withId)
                val finalList = _patients.value.map {
                    if (it.id == withId.id) created.copy(fotoUri = withId.fotoUri) else it
                }
                _patients.value = finalList
                store.save(finalList)
            } catch (e: Exception) {
                Log.e("ViewModel", "Fallo al añadir en backend, se mantiene en local", e)
            }
        }
    }

    fun removePatient(id: String?) {
        if (id == null) return
        viewModelScope.launch {
            val originalList = _patients.value
            
            val newList = originalList.filterNot { it.id == id }
            _patients.value = newList
            store.save(newList)

            try {
                RetrofitInstance.api.deletePatient(id)
            } catch (e: Exception) {
                Log.e("ViewModel", "Fallo al borrar en backend, revirtiendo", e)
                _patients.value = originalList
                store.save(originalList)
            }
        }
    }

    fun updatePatient(patient: Patient, context: Context) {
        val id = patient.id ?: return
        viewModelScope.launch {
            val originalList = _patients.value
            val patientToUpdate = originalList.find { it.id == id }

            val savedImageUri = saveImageToInternalStorage(context, patient.fotoUri)
            
            val updatedPatientWithPhoto = patient.copy(
                fotoUri = savedImageUri ?: patientToUpdate?.fotoUri
            )

            val newList = originalList.map { if (it.id == id) updatedPatientWithPhoto else it }
            _patients.value = newList
            store.save(newList)

            try {
                RetrofitInstance.api.updatePatient(id, updatedPatientWithPhoto)
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
