package com.example.vetcare_grupo11.network

import com.example.vetcare_grupo11.viewmodel.Appointment
import retrofit2.http.*

interface AppointmentApiService {

    @GET("/api/appointments")
    suspend fun getAppointments(): List<Appointment>

    @GET("/api/appointments/{id}")
    suspend fun getAppointmentById(
        @Path("id") id: String
    ): Appointment

    @POST("/api/appointments")
    suspend fun createAppointment(
        @Body appointment: Appointment
    ): Appointment

    @PUT("/api/appointments/{id}")
    suspend fun updateAppointment(
        @Path("id") id: String,
        @Body appointment: Appointment
    ): Appointment

    @DELETE("/api/appointments/{id}")
    suspend fun deleteAppointment(
        @Path("id") id: String
    )
}


