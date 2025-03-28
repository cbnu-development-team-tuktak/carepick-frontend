package com.example.carepick.repository

import android.util.Log
import com.example.carepick.R
import com.example.carepick.dto.HospitalDetailsResponse
import com.example.carepick.model.HospitalListData
import com.example.carepick.ui.HospitalDetailFragment

class HospitalListRepository {

    private val hospitalRepository = HospitalRepository()
    suspend fun getHospitalList(): MutableList<HospitalListData> {

        val hospitals: MutableList<HospitalDetailsResponse> = hospitalRepository.fetchHospitals()

        return hospitals.map { hospital ->
            HospitalListData(
                name = hospital.name,
                phoneNumber = hospital.phoneNumber,
                homepage = hospital.homepage,
                address = hospital.address,
                imageUrl = hospital.images?.firstOrNull()?.url ?: "", // ✅ null이나 빈 경우 안전 처리
                operatingHours = hospital.operatingHours,
                specialties = hospital.specialties,
                doctors = hospital.doctors,
                fragment = HospitalDetailFragment(),
                latitude = hospital.location?.latitude ?: 0.0, // 병원의 location 데이터에서 위도 추출
                longitude = hospital.location?.longitude ?: 0.0, // 병원의 location 데이터에서 경도 추출
                additionalInfo = hospital.additionalInfo
            )
        }.toMutableList()
    }
}