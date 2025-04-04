package com.example.carepick.repository

import com.example.carepick.R
import com.example.carepick.model.ServiceListData
import com.example.carepick.ui.doctor.DoctorListFragment
import com.example.carepick.ui.hospital.HospitalListFragment
import com.example.carepick.ui.selfcheck.SelfCheckFragment

class ServiceListRepository {
    fun getServiceList(): MutableList<ServiceListData> {
        return mutableListOf(
            ServiceListData("병원 목록", R.drawable.hospital_icon, HospitalListFragment()),
            ServiceListData("의사 목록", R.drawable.doctor_icon, DoctorListFragment()),
            ServiceListData("자가진단", R.drawable.diagnosis_icon, SelfCheckFragment())
        )
    }
}