package com.example.vetcare_grupo11.network

import com.example.vetcare_grupo11.viewmodel.Patient
import retrofit2.http.*

interface PatientApiService {

    @GET("/api/patients")
    suspend fun getPatients(): List<Patient>

    @GET("/api/patients/{id}")
    suspend fun getPatientById(
        @Path("id") id: String
    ): Patient

    @POST("/api/patients")
    suspend fun createPatient(
        @Body patient: Patient
    ): Patient

    @PUT("/api/patients/{id}")
    suspend fun updatePatient(
        @Path("id") id: String,
        @Body patient: Patient
    ): Patient

    @DELETE("/api/patients/{id}")
    suspend fun deletePatient(
        @Path("id") id: String
    )
}

