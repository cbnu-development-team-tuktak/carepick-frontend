package com.example.carepick.repository

import com.example.carepick.R
import com.example.carepick.model.HospitalListData

class HospitalListRepository {
    fun getHospitalList(): MutableList<HospitalListData> {
        return mutableListOf(
            HospitalListData("한국 병원", "충북 청주시 상당구 단재로 106", R.drawable.hospital_pic),
            HospitalListData("효성 병원", "충북 청주시 상당구 쇠내로 16", R.drawable.hospital_pic),
            HospitalListData("충북대학교 병원", "충북 청주시 서원구 1순환로", R.drawable.hospital_pic),
        )
    }
}