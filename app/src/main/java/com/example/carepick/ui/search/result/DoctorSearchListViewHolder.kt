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
import android.location.Location
import android.view.View
import com.example.carepick.ui.location.repository.UserLocation

class DoctorSearchListViewHolder(
    val binding: SearchListBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(doctor: DoctorDetailsResponse, userLocation: UserLocation?, onItemClicked: (DoctorDetailsResponse) -> Unit) {
        val cleanName = doctor.name.replace("\\[.*\\]".toRegex(), "").trim()

        binding.searchListName.text = cleanName // ✅ 깔끔하게 정리된 이름을 출력
        binding.searchListAddress.text = doctor.hospitalName

        // --- 거리 계산 및 표시 로직 ---
        // 1. 사용자 위치와 병원 위치가 모두 있을 때만 거리를 계산
        if (userLocation != null && doctor.hospitalLocation != null) {
            val results = FloatArray(1) // 결과를 담을 배열

            // 2. Location.distanceBetween() 호출하여 거리 계산 (결과는 미터 단위)
            Location.distanceBetween(
                userLocation.lat,
                userLocation.lng,
                doctor.hospitalLocation!!.latitude,
                doctor.hospitalLocation!!.longitude,
                results
            )

            // 3. 계산된 거리를 형식에 맞게 변환하여 TextView에 설정
            binding.hospitalDistance.text = formatDistance(results[0])
            binding.hospitalDistance.visibility = View.VISIBLE
        } else {
            // 위치 정보가 없으면 거리 텍스트뷰를 숨김
            binding.hospitalDistance.visibility = View.GONE
        }

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

    private fun formatDistance(meters: Float): String {
        return if (meters < 1000) {
            // 1km 미만이면 미터 단위로 표시
            "${meters.toInt()}m"
        } else {
            // 1km 이상이면 킬로미터 단위로, 소수점 첫째 자리까지 표시
            val kilometers = meters / 1000
            String.format("%.1fkm", kilometers)
        }
    }
}