package com.example.carepick.ui.search.result

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.databinding.DoctorCardBinding
import com.example.carepick.data.model.DoctorDetailsResponse
import com.example.carepick.common.ui.DoctorDetailFragment
import com.example.carepick.ui.selfDiagnosis.adapter.SpecialtyAdapter

// 의사 카드 뷰의 어디에 어떤 데이터가 들어갈지를 명시한다
class DoctorCardViewHolder(
    val binding: DoctorCardBinding,
    val activity: FragmentActivity
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



        // 카드를 선택했을 때 다음의 동작들을 수행한다
        binding.root.setOnClickListener {
            navigateToDetail(doctor)
        }

        // 진료과 목록에 따라 동적으로 진료과를 카드에 추가한다
        val specialtyAdapter = SpecialtyAdapter(doctor.specialties)
        binding.doctorCardRecyclerView.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        binding.doctorCardRecyclerView.adapter = specialtyAdapter
    }

    // 의사 상세 페이지로 화면을 이동시키는 코드
    private fun navigateToDetail(doctorData: DoctorDetailsResponse) {
        val doctorDetailFragment = DoctorDetailFragment()

        // 의사 상세 페이지에 데이터를 전달한다
        val bundle = Bundle().apply {
            putString("doctorId", doctorData.id)
        }

        doctorDetailFragment.arguments = bundle

        // 의사 상세 페이지로 화면을 넘긴다
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, doctorDetailFragment)
            .addToBackStack(null)
            .commit()
    }
}