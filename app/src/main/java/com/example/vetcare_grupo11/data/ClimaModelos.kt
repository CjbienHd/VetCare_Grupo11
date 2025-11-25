package com.example.vetcare_grupo11.data

data class RespuestaClima(
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String?,
    val current_weather: ClimaActual?
)

data class ClimaActual(
    val temperature: Double?,     // °C
    val windspeed: Double?,       // km/h
    val winddirection: Double?,   // °
    val weathercode: Int?,        // código del tiempo
    val time: String?
)
