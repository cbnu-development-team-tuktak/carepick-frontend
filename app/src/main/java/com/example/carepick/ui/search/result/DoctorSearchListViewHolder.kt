package com.example.carepick.ui.search.result

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.ui.selfDiagnosis.adapter.SpecialtyAdapter
import com.example.carepick.databinding.SearchListBinding
import com.example.carepick.data.model.DoctorDetailsResponse
import com.example.carepick.common.ui.DoctorDetailFragment
import com.example.carepick.data.model.HospitalDetailsResponse

class DoctorSearchListViewHolder(
    val binding: SearchListBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(doctor: DoctorDetailsResponse, onItemClicked: (DoctorDetailsResponse) -> Unit) {
        binding.searchListName.text = doctor.name
        binding.searchListAddress.text = "병원 이름이 들어가야 함"

        // url을 통해 의사 이미지를 불러온다
        val imageUrl = doctor.profileImage?: ""
        Glide.with(binding.root)
            .load(imageUrl)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.doctor_placeholder)
            .into(binding.searchListImage)

        // 진료과 목록에 따라 동적으로 진료과를 카드에 추가한다
        val specialtyAdapter = SpecialtyAdapter(doctor.specialties)
        binding.searchListRecyclerView.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        binding.searchListRecyclerView.adapter = specialtyAdapter

        binding.root.setOnClickListener {
            onItemClicked(doctor)
        }
    }
}