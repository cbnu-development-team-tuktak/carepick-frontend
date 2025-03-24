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
                address = hospital.address,
                imageUrl = hospital.images?.firstOrNull()?.url ?: "", // ✅ null이나 빈 경우 안전 처리
                fragment = HospitalDetailFragment()
            )
        }.toMutableList()
    }
}