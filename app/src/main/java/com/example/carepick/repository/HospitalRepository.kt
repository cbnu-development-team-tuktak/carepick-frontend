package com.example.carepick.repository

import android.util.Log
import com.example.carepick.dto.HospitalDetailsResponse
import com.example.carepick.dto.HospitalPageResponse
import com.example.carepick.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HospitalRepository {

    fun fetchHospitals() {
        RetrofitClient.instance.getAllHospitals(page = 0, size = 10)
            .enqueue(object : Callback<HospitalPageResponse<HospitalDetailsResponse>> {
                override fun onResponse(
                    call: Call<HospitalPageResponse<HospitalDetailsResponse>>,
                    response: Response<HospitalPageResponse<HospitalDetailsResponse>>
                ) {
                    if (response.isSuccessful) {
                        val pageResponse = response.body()
                        val hospitals = pageResponse?.content ?: emptyList()

                        Log.e("API_SUCCESS", "(Successfully brought in ${hospitals.size} hospital information")
                        hospitals.forEach { hospital ->
                            Log.e(
                                "API_SUCCESS",
                                "ID: ${hospital.id}, name: ${hospital.name}, address: ${hospital.address}"
                            )
                        }
                    } else {
                        Log.e("API_ERROR", "Fail to get Information's from Database : ${response.code()}, message: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<HospitalPageResponse<HospitalDetailsResponse>>, t: Throwable) {
                    Log.e("API_ERROR", "network problem reveal!!", t)
                }
            })
    }
}
