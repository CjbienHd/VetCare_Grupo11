package com.example.vetcare_grupo11.data

import retrofit2.http.GET
import retrofit2.http.Query

interface ServicioClimaApi {

    @GET("v1/forecast")
    suspend fun obtenerClimaActual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current_weather") climaActual: Boolean = true
    ): RespuestaClima
}