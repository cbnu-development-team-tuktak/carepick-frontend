package com.example.carepick.data.network

import android.util.Log
import com.example.carepick.ui.location.network.AdminRegionApi
import com.example.carepick.ui.selfDiagnosis.network.SelfDiagnosisApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val BASE_URL = "http://10.0.2.2:8080"

    // 재시도 인터셉터 (타임아웃/일시적 연결 실패 시 최대 2회 재시도, 지수 백오프)
    private val retryInterceptor = Interceptor { chain ->
        var tryCount = 0
        val maxRetries = 2
        var lastException: Exception? = null
        val request: Request = chain.request()

        while (tryCount <= maxRetries) {
            try {
                return@Interceptor chain.proceed(request)
            } catch (e: SocketTimeoutException) {
                lastException = e
                Log.w("HTTP", "Timeout on ${request.url} (try=$tryCount)")
            } catch (e: IOException) {
                lastException = e
                Log.w("HTTP", "IO error on ${request.url} (try=$tryCount): ${e.message}")
            }
            tryCount++
            if (tryCount <= maxRetries) {
                val backoffMs = 300L * (1 shl (tryCount - 1)) // 300ms, 600ms
                Thread.sleep(backoffMs)
            }
        }
        throw lastException ?: IOException("Unknown network error")
    }

    // 요청/응답 1줄 요약
    private val verboseInterceptor = Interceptor { chain ->
        val req = chain.request()
        Log.d("HTTP", "➡️ ${req.method} ${req.url}")
        val start = System.nanoTime()
        val res = chain.proceed(req)
        val tookMs = (System.nanoTime() - start) / 1e6
        Log.d("HTTP", "⬅️ ${res.code} ${res.message} (${tookMs}ms) ${req.url}")
        res
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)  // 연결 타임아웃
        .readTimeout(30, TimeUnit.SECONDS)     // 응답 수신 타임아웃
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)     // 전체 호출 제한
        .followRedirects(true)
        .addInterceptor(retryInterceptor)
        .addInterceptor(verboseInterceptor)
        .build()

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

    val adminRegionService: AdminRegionApi by lazy {
        retrofit.create(AdminRegionApi::class.java)
    }

    val selfCheckService: SelfDiagnosisApi by lazy {
        Log.i("Retrofit", "Retrofit init with BASE_URL=$BASE_URL")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SelfDiagnosisApi::class.java)
    }
}