package com.example.carepick

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.adapter.HospitalListAdapter
import com.example.carepick.adapter.ServiceListAdapter
import com.example.carepick.databinding.ActivityMainBinding
import com.example.carepick.repository.HospitalListRepository
import com.example.carepick.repository.HospitalRepository
import com.example.carepick.repository.ServiceListRepository


class MainActivity : AppCompatActivity() {

    private val hospitalRepository = HospitalRepository()
    private val serviceListRepository = ServiceListRepository()
    private val hospitalListRepository = HospitalListRepository()

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
        hospitalRepository.fetchHospitals()

        // 서비스 목록 리포지토리로부터 서비스 목록 카드뷰에 들어갈 텍스트와 정보들을 받는다
        val serviceList = serviceListRepository.getServiceList()
        val hospitalList = hospitalListRepository.getHospitalList()

        // 서비스 목록의 카드뷰를 수평으로 배치하도록 HORIZONTAL 옵션을 준다
        binding.serviceListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.hospitalListRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 서비스 목록 리포지토리로부터 받아온 정보를 토대로 카듀브들을 동적으로 생성한다
        binding.serviceListRecyclerView.adapter = ServiceListAdapter(serviceList)
        binding.hospitalListRecyclerView.adapter = HospitalListAdapter(hospitalList)
    }
}