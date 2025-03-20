package com.example.carepick.repository

import android.util.Log
import com.example.carepick.dto.HospitalDetailsResponse
import com.example.carepick.dto.HospitalPageResponse
import com.example.carepick.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume

class HospitalRepository {

    suspend fun fetchHospitals(): MutableList<HospitalDetailsResponse> {
        return suspendCancellableCoroutine { continuation ->
            RetrofitClient.instance.getAllHospitals(page = 0, size = 10)
                .enqueue(object : Callback<HospitalPageResponse<HospitalDetailsResponse>> {
                    override fun onResponse(
                        call: Call<HospitalPageResponse<HospitalDetailsResponse>>,
                        response: Response<HospitalPageResponse<HospitalDetailsResponse>>
                    ) {
                        if (response.isSuccessful) {
                            val hospitals = response.body()?.content?.toMutableList() ?: mutableListOf()
                            Log.e("API_SUCCESS", "Successfully brought in ${hospitals.size} hospital information")
                            continuation.resume(hospitals)
                        } else {
                            Log.e("API_ERROR", "Fail to get hospitals: ${response.code()}, message: ${response.message()}")
                            continuation.resume(mutableListOf())
                        }
                    }

                    override fun onFailure(call: Call<HospitalPageResponse<HospitalDetailsResponse>>, t: Throwable) {
                        Log.e("API_ERROR", "network problem reveal!!", t)
                        continuation.resume(mutableListOf())
                    }
                })
        }
    }
}
