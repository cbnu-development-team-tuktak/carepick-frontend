package com.example.carepick.common.ui

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.carepick.R
import com.example.carepick.databinding.FragmentHospitalDetailBinding
import com.example.carepick.data.model.HospitalAdditionalInfo

fun addInfoCheck(
    context: Context,
    binding: FragmentHospitalDetailBinding,
    addInfo: HospitalAdditionalInfo
) {
    val color = ContextCompat.getColor(context, R.color.HospitalDetailAddInfoTrue)

    if (addInfo.open24Hours) {
        binding.hospitalDetailAddInfoTextView1.setTextColor(color)
    }

    if (addInfo.openAllYear) {
        binding.hospitalDetailAddInfoTextView2.setTextColor(color)
    }

    if (addInfo.emergencyTreatment) {
        binding.hospitalDetailAddInfoTextView3.setTextColor(color)
    }

    if (addInfo.openOnSunday) {
        binding.hospitalDetailAddInfoTextView4.setTextColor(color)
    }

    if (addInfo.maleFemaleDoctorChoice) {
        binding.hospitalDetailAddInfoTextView5.setTextColor(color)
    }

    if (addInfo.nightShift) {
        binding.hospitalDetailAddInfoTextView6.setTextColor(color)
    }

    if (addInfo.networkHospital) {
        binding.hospitalDetailAddInfoTextView7.setTextColor(color)
    }

    if (addInfo.collaborativeCare) {
        binding.hospitalDetailAddInfoTextView8.setTextColor(color)
    }

    if (addInfo.freeCheckup) {
        binding.hospitalDetailAddInfoTextView9.setTextColor(color)
    }

    if (addInfo.noLunchBreak) {
        binding.hospitalDetailAddInfoTextView10.setTextColor(color)
    }

    if (addInfo.nearSubway) {
        binding.hospitalDetailAddInfoTextView11.setTextColor(color)
    }
}