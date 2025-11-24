package com.example.vetcare_grupo11.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    // Pacientes (lo que ya tenías)
    val api: PatientApiService by lazy {
        retrofit.create(PatientApiService::class.java)
    }

    // Citas
    val appointmentsApi: AppointmentApiService by lazy {
        retrofit.create(AppointmentApiService::class.java)
    }
}


