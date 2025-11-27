package com.example.vetcare_grupo11.network

import com.example.vetcare_grupo11.viewmodel.Appointment
import com.example.vetcare_grupo11.viewmodel.Patient
import retrofit2.http.*

interface PatientApi {
    @GET("patients")
    suspend fun getPatients(): List<Patient>

    @POST("patients")
    suspend fun createPatient(@Body patient: Patient): Patient

    @PUT("patients/{id}")
    suspend fun updatePatient(@Path("id") id: String, @Body patient: Patient): Patient

    @DELETE("patients/{id}")
    suspend fun deletePatient(@Path("id") id: String)
}

interface AppointmentApi {
    @GET("appointments")
    suspend fun getAppointments(): List<Appointment>

    @POST("appointments")
    suspend fun createAppointment(@Body appointment: Appointment): Appointment

    @PUT("appointments/{id}")
    suspend fun updateAppointment(@Path("id") id: String, @Body appointment: Appointment): Appointment

    @DELETE("appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: String)
}
