package com.example.callrapport

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.callrapport.dto.HospitalDetailsResponse
import com.example.callrapport.dto.HospitalPageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchHospitals()
    }

    private fun fetchHospitals() {
        RetrofitClient.instance.getAllHospitals(page = 0, size = 10)
            .enqueue(object : Callback<HospitalPageResponse<HospitalDetailsResponse>> {
                override fun onResponse(
                    call: Call<HospitalPageResponse<HospitalDetailsResponse>>,
                    response: Response<HospitalPageResponse<HospitalDetailsResponse>>
                ) {
                    if (response.isSuccessful) {
                        val pageResponse = response.body()
                        val hospitals = pageResponse?.content // content 리스트 가져오기

                        hospitals?.let {
                            Log.e("API_SUCCESS", "병원 목록 (${it.size}개) 수신 성공")
                            it.forEach { hospital ->
                                Log.e("API_SUCCESS", "ID: ${hospital.id}, 이름: ${hospital.name}, 주소: ${hospital.address}")
                            }
                        } ?: Log.e("API_SUCCESS", "응답 성공 but 데이터 없음")
                    } else {
                        Log.e("API_ERROR", "응답 실패: ${response.code()}, 메시지: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<HospitalPageResponse<HospitalDetailsResponse>>, t: Throwable) {
                    Log.e("API_ERROR", "네트워크 오류 발생", t)
                }
            })
    }
}