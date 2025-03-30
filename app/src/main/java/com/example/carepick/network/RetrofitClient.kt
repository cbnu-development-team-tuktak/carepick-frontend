package com.example.carepick.network

import com.example.carepick.service.DoctorApiService
import com.example.carepick.service.HospitalApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val hospitalService: HospitalApiService by lazy {
        retrofit.create(HospitalApiService::class.java)
    }

    val doctorService: DoctorApiService by lazy {
        retrofit.create(DoctorApiService::class.java)
    }
}