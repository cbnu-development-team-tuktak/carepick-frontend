package com.example.carepick.viewHolder

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.adapter.SpecialtyAdapter
import com.example.carepick.databinding.SearchListBinding
import com.example.carepick.dto.doctor.DoctorDetailsResponse
import com.example.carepick.ui.doctor.DoctorDetailFragment

class DoctorSearchListViewHolder(
    val binding: SearchListBinding,
    val activity: FragmentActivity
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(doctor: DoctorDetailsResponse) {
        binding.searchListName.text = doctor.name
        binding.searchListAddress.text = "병원 이름이 들어가야 함"

        // url을 통해 의사 이미지를 불러온다
        val imageUrl = doctor.profileImage?: ""
        Glide.with(binding.root)
            .load(imageUrl)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.warning)
            .into(binding.searchListImage)

        // 진료과 목록에 따라 동적으로 진료과를 카드에 추가한다
        val specialtyArray= doctor.specialty?.split(",")?.map { it.trim() } ?: emptyList()// 공백 제거 포함
        val specialtyAdapter = SpecialtyAdapter(specialtyArray)
        binding.searchListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.searchListRecyclerView.adapter = specialtyAdapter

        binding.root.setOnClickListener {
            navigateToDetail(doctor)
        }
    }

    private fun navigateToDetail(doctorData: DoctorDetailsResponse) {
        val doctorDetailFragment = DoctorDetailFragment()

        // 의사 상세 페이지에 데이터를 전달한다
        val bundle = Bundle().apply {
            putString("name", doctorData.name)
            putString("url", doctorData.url)
            putString("profileImage", doctorData.profileImage)
            putString("career", doctorData.career)
            putString("specialty", doctorData.specialty)

            putStringArrayList(
                "educationLicense",
                ArrayList(doctorData.educationLicenses ?: emptyList())
            )
        }

        doctorDetailFragment.arguments = bundle

        // 의사 상세 페이지로 화면을 넘긴다
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, doctorDetailFragment)
            .addToBackStack(null)
            .commit()
    }
}