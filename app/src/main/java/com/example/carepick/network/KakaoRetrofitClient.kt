package com.example.carepick.network

import com.example.carepick.network.KakaoLocalApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KakaoRetrofitClient {
    private const val BASE_URL = "https://dapi.kakao.com/"
    private const val REST_API_KEY = "4bde7a7235a24839479023ff8eb22347" // 데모용

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Authorization", "KakaoAK $REST_API_KEY")
                .build()
            chain.proceed(req)
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val kakaoService: KakaoLocalApi by lazy {
        retrofit.create(KakaoLocalApi::class.java)
    }
}