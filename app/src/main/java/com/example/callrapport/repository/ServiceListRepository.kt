package com.example.callrapport.repository

import com.example.callrapport.R
import com.example.callrapport.model.ServiceListData

class ServiceListRepository {
    fun getServiceList(): MutableList<ServiceListData> {
        return mutableListOf(
            ServiceListData("병원 목록", R.drawable.hospital_icon),
            ServiceListData("의사 목록", R.drawable.doctor_icon),
            ServiceListData("자가진단", R.drawable.diagnosis_icon),
        )
    }
}