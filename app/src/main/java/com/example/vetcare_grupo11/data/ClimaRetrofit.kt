package com.example.vetcare_grupo11.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ClimaRetrofit {

    private const val BASE_URL = "https://api.open-meteo.com/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ServicioClimaApi by lazy {
        retrofit.create(ServicioClimaApi::class.java)
    }
}