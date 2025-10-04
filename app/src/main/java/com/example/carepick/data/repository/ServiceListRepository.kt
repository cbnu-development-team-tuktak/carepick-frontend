package com.example.carepick.data.repository

import com.example.carepick.R
import com.example.carepick.data.model.ServiceListData
import com.example.carepick.common.ui.DoctorListFragment
import com.example.carepick.common.ui.HospitalListFragment
import com.example.carepick.ui.selfDiagnosis.SelfDiagnosisFragment

// 현재 시스템이 제공하는 서비스의 목록을 제공한다.

// 하드코딩하지 않고 동적으로 서비스 카드를 생성하도록 하여
// 추후 다른 서비스를 추가하거나, 현재 있는 서비스를 제거/수정하더라도 유연하게 대응 가능하다
class ServiceListRepository {
    fun getServiceList(): MutableList<ServiceListData> {
        return mutableListOf(
            ServiceListData("병원 목록", R.drawable.hospital_icon, HospitalListFragment()),
            ServiceListData("의사 목록", R.drawable.doctor_icon, DoctorListFragment()),
            ServiceListData("자가진단", R.drawable.diagnosis_icon, SelfDiagnosisFragment())
        )
    }
}