package com.example.carepick.ui.search.result

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.ui.selfDiagnosis.adapter.SpecialtyAdapter
import com.example.carepick.databinding.SearchListBinding
import com.example.carepick.data.model.HospitalDetailsResponse

class HospitalSearchListViewHolder(
    val binding: SearchListBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(hospital: HospitalDetailsResponse, onItemClicked: (HospitalDetailsResponse) -> Unit) {
        binding.searchListName.text = hospital.name
        binding.searchListAddress.text = hospital.address

        // url을 통해 병원 이미지를 불러온다
        val imageUrl = hospital.images?.firstOrNull()?.url ?: ""
        Glide.with(binding.root)
            .load(imageUrl)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.hospital_placeholder)
            .into(binding.searchListImage)

        // 진료과 목록에 따라 동적으로 진료과를 카드에 추가한다
        val specialties = hospital.specialties ?: emptyList()
        val specialtyAdapter = SpecialtyAdapter(specialties)
        binding.searchListRecyclerView.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        binding.searchListRecyclerView.adapter = specialtyAdapter

        // 클릭 시 navigateToDetail() 대신 콜백 함수를 직접 호출
        binding.root.setOnClickListener {
            onItemClicked(hospital)
        }
    }
}