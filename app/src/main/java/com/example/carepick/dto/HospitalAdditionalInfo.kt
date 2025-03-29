package com.example.carepick.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HospitalAdditionalInfo(
    val id: String,
    val open24Hours: Boolean, // 24시간 문의 가능 여부
    val emergencyTreatment: Boolean, // 24시간 응급 진료 가능 여부
    val maleFemaleDoctorChoice: Boolean, // 남여 전문의 선택 가능 여부
    val networkHospital: Boolean, // 네트워크 병원 여부
    val freeCheckup: Boolean, // 무료 검진 제공 여부
    val nearSubway: Boolean, // 역세권 위치 여부
    val openAllYear: Boolean, // 연중무휴 진료 여부
    val openOnSunday: Boolean, // 일요일 & 공휴일 진료 여부
    val nightShift: Boolean, // 평일 야간 진료 여부
    val collaborativeCare: Boolean, // 협진 시스템 지원 여부
    val noLunchBreak: Boolean, // 점심시간 없이 진료 여부

) : Parcelable
