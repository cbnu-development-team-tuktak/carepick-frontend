package com.tuktak.carepick.ui.search.result.doctor

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tuktak.carepick.R
import com.tuktak.carepick.databinding.DoctorCardBinding
import com.tuktak.carepick.data.model.DoctorDetailsResponse
import com.tuktak.carepick.common.ui.DoctorDetailFragment
import com.tuktak.carepick.ui.selfDiagnosis.adapter.SpecialtyAdapter

// 의사 카드 뷰의 어디에 어떤 데이터가 들어갈지를 명시한다
class DoctorCardViewHolder(
    val binding: DoctorCardBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(doctor: DoctorDetailsResponse) {

        // 의사 이름을 집어넣는다
        val cleanName = doctor.name.replace("\\[.*\\]".toRegex(), "").trim()
        binding.doctorName.text = cleanName

        // 카드에 이미지를 url을 통해서 집어넣는다
        Glide.with(binding.root)
            .load(doctor.profileImage)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.doctor_placeholder)
            .into(binding.doctorImage)

        // 병원 이름을 넣는다
        binding.doctorHospital.text = doctor.hospitalName

        // 진료과 목록에 따라 동적으로 진료과를 카드에 추가한다
        val specialtyAdapter = SpecialtyAdapter(doctor.specialties)
        binding.doctorCardRecyclerView.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        binding.doctorCardRecyclerView.adapter = specialtyAdapter
    }
}