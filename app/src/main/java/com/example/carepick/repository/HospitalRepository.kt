package com.example.carepick.repository

import android.content.Context
import android.util.Log
import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.dto.hospital.HospitalPageResponse
import com.example.carepick.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import com.example.carepick.utils.cleanHospitalName
import java.time.LocalTime

class HospitalRepository {

    // 백엔드 서버로 getAllHospitals 메소드를 호출하고 그 결과를 HospitalPageResponse 객체로 받는다
    // HospitalPageResponse는 HospitalDetailsResponse 객체를 배열로 담는 객체이다
    suspend fun fetchHospitals(): MutableList<HospitalDetailsResponse> {

        return suspendCancellableCoroutine { continuation ->
            RetrofitClient.hospitalService.getAllHospitals(page = 0, size = 10)
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

    suspend fun getHospitalById(id: String): HospitalDetailsResponse? {
        return suspendCancellableCoroutine { continuation ->
            RetrofitClient.hospitalService.getHospitalById(id)
                .enqueue(object : Callback<HospitalDetailsResponse> {
                    override fun onResponse(
                        call: Call<HospitalDetailsResponse>,
                        response: Response<HospitalDetailsResponse>
                    ) {
                        if (response.isSuccessful) {
                            val hospital = response.body()
                            Log.e("API_SUCCESS", "Successfully brought hospital information: $hospital")
                            continuation.resume(hospital)
                        } else {
                            Log.e("API_ERROR", "Failed to get hospital details: ${response.code()}, message: ${response.message()}")
                            continuation.resume(null)
                        }
                    }

                    override fun onFailure(call: Call<HospitalDetailsResponse>, t: Throwable) {
                        Log.e("API_ERROR", "Network problem occurred: getHospitalById", t)
                        continuation.resume(null)
                    }
                })
        }
    }

    // 특정 키워드와 부분/완전 일치하는 병원 목록을 가져오는 함수
    suspend fun getSearchedHospitals(keyword: String): MutableList<HospitalDetailsResponse> {

        return suspendCancellableCoroutine { continuation ->
            RetrofitClient.hospitalService.getSearchedHospitals(keyword, page = 0, size = 10)
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
                        Log.e("API_ERROR", "network problem reveal!! : getSearchedHospitals", t)
                        continuation.resume(mutableListOf())
                    }
                })
        }
    }



    suspend fun getHospitalsWithExtendedFilter(
        lat: Double?,
        lng: Double?,
        distance: Double?,
        specialties: List<String>?,
        selectedDays: List<String>?,
        startTime: LocalTime?,
        endTime: LocalTime?,
        sortBy: String?,
        page: Int = 0,
        size: Int = 10
    ): MutableList<HospitalDetailsResponse> {

        return suspendCancellableCoroutine { continuation ->
            RetrofitClient.hospitalService.getFilteredHospitals(
                lat = lat,
                lng = lng,
                distance = distance,
                specialties = specialties,
                selectedDays = selectedDays,
                startTime = startTime,
                endTime = endTime,
                sortBy = sortBy,
                page = page,
                size = size
            ).enqueue(object : Callback<HospitalPageResponse<HospitalDetailsResponse>> {
                override fun onResponse(
                    call: Call<HospitalPageResponse<HospitalDetailsResponse>>,
                    response: Response<HospitalPageResponse<HospitalDetailsResponse>>
                ) {
                    if (response.isSuccessful) {
                        val hospitals = response.body()?.content?.toMutableList() ?: mutableListOf()
                        continuation.resume(hospitals)
                    } else {
                        Log.e("API_ERROR", "응답 실패: ${response.code()}, ${response.message()}")
                        continuation.resume(mutableListOf())
                    }
                }

                override fun onFailure(
                    call: Call<HospitalPageResponse<HospitalDetailsResponse>>,
                    t: Throwable
                ) {
                    Log.e("API_ERROR", "네트워크 오류 발생", t)
                    continuation.resume(mutableListOf())
                }
            })

        }
    }


    // 사전에 저장된 json 파일에서 병원 정보를 읽는 코드
    // json 파일에서 데이터를 불러온 다음, HospitalDetailResponse 객체의 포맷으로 데이터를 가공한 다음 반환한다
    fun loadHospitalsFromAsset(context: Context): MutableList<HospitalDetailsResponse> {
        // json을 읽는다
        val jsonString =
            context.assets.open("hospitals.json").bufferedReader().use { it.readText() }

        // json의 Document 타입을 HospitalDetailResponse로 포맷을 변환한다
        val listType = object : TypeToken<List<HospitalDetailsResponse>>() {}.type
        val rawList: MutableList<HospitalDetailsResponse> = Gson().fromJson(jsonString, listType)

        // 병원 이름 정리
        // 병원 이름에서 따음표나 괄호로 감싸진 부분을 제거하도록 하였음
        // cleanHospitalName 메소드는 utils 패키지의 StringExtension.kt에 정의하였음
        return rawList.map { hospital ->
            hospital.copy(name = hospital.name.cleanHospitalName())
        }.toMutableList()
    }
}