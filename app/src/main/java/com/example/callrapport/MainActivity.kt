package com.example.callrapport

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.callrapport.adapter.serviceAdapter
import com.example.callrapport.databinding.ActivityMainBinding
import com.example.callrapport.dto.HospitalDetailsResponse
import com.example.callrapport.dto.HospitalPageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 백엔드 서버로부터 정보를 가져오는 메소드
        fetchHospitals()

        // 서비스 목록에 동적으로 텍스트를 넣음
        // datas 배열의 크기와 내용을 기반으로 카드뷰를 생성한다
        val datas = mutableListOf<String>()
        datas.add("병원 목록")
        datas.add("의사 목록")
        datas.add("자가진단")

        // 카드뷰를 수평으로 배치하도록 설정
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        // 앞선 데이터와 설정을 토대로 리사이클러뷰를 출력한다
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = serviceAdapter(datas)
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