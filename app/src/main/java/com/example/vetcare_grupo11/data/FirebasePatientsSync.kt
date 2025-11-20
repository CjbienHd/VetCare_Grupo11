package com.example.vetcare_grupo11.data

import com.example.vetcare_grupo11.viewmodel.Patient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePatientsSync {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("patients")

    // Descarga todos los pacientes desde Firebase
    suspend fun downloadPatients(): List<Patient> {
        val snapshot = collection.get().await()
        return snapshot.documents.map { doc ->
            Patient(
                id = doc.getString("id") ?: doc.id,
                nombre = doc.getString("nombre") ?: "",
                especie = doc.getString("especie") ?: "",
                raza = doc.getString("raza") ?: "",
                tutor = doc.getString("tutor") ?: ""
            )
        }
    }

    // Sube 1 paciente (crear/actualizar)
    fun uploadPatient(p: Patient) {
        val data = mapOf(
            "id" to p.id,
            "nombre" to p.nombre,
            "especie" to p.especie,
            "raza" to p.raza,
            "tutor" to p.tutor
        )
        collection.document(p.id).set(data)
    }

    // Borra 1 paciente por id
    fun deletePatient(id: String) {
        collection.document(id).delete()
    }
}