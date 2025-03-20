package com.example.carepick

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.carepick.repository.HospitalRepository
import com.example.carepick.ui.HomeFragment


class MainActivity : AppCompatActivity() {

    // 서버로부터 병원 정보를 가져오는 리포지토리
    private val hospitalRepository = HospitalRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 백엔드 서버로부터 정보를 가져오는 메소드
        hospitalRepository.fetchHospitals()

        // 첫 실행 시 HomeFragment 표시
        // MainActivity는 실행만을 맡고 첫 화면을 비롯한 나머지 화면은 모두 Fragment로 구현하였음
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}