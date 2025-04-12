package com.example.carepick.viewHolder

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.databinding.DoctorCardBinding
import com.example.carepick.dto.doctor.DoctorDetailsResponse
import com.example.carepick.ui.doctor.DoctorDetailFragment

// 의사 카드 뷰의 어디에 어떤 데이터가 들어갈지를 명시한다
class DoctorCardViewHolder(
    val binding: DoctorCardBinding,
    val activity: FragmentActivity
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(doctor: DoctorDetailsResponse) {

        // 의사 이름을 집어넣는다
        binding.doctorName.text = doctor.name
        // 카드에 이미지를 url을 통해서 집어넣는다
        Glide.with(binding.root)
            .load(doctor.profileImage)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.doctor_placeholder)
            .into(binding.doctorImage)

        // 카드를 선택했을 때 다음의 동작들을 수행한다
        binding.root.setOnClickListener {
            navigateToDetail(doctor)
        }
    }

    // 의사 상세 페이지로 화면을 이동시키는 코드
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