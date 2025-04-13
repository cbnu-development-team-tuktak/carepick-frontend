package com.example.carepick.viewHolder

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.adapter.SpecialtyAdapter
import com.example.carepick.databinding.SearchListBinding
import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.ui.hospital.HospitalDetailFragment

class HospitalSearchListViewHolder(
    val binding: SearchListBinding,
    val activity: FragmentActivity
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(hospital: HospitalDetailsResponse) {
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
        binding.searchListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.searchListRecyclerView.adapter = specialtyAdapter

        binding.root.setOnClickListener {
            navigateToDetail(hospital)
        }
    }

    // 병원을 선택할 경우 해당 병원의 상세 페이지로 넘어가야 한다
    private fun navigateToDetail(hospitalData: HospitalDetailsResponse) {
        // 카드뷰를 클릭했을 때 넘어갈 Fragment를 객체에 저장
        val hospitalDetailFragment = HospitalDetailFragment()

        // 병원 상세 페이지에 전달할 데이터를 지정한다
        val bundle = Bundle().apply {
            putString("name", hospitalData.name)
            putString("phoneNumber", hospitalData.phoneNumber)
            putString("homepage", hospitalData.homepage)
            putString("address", hospitalData.address)

            hospitalData.location?.latitude?.let { putDouble("latitude", it) }
            hospitalData.location?.longitude?.let { putDouble("longitude", it) }

            hospitalData.images?.let {
                putParcelableArrayList("images", ArrayList(it))
            }

            hospitalData.specialties?.let {
                putStringArrayList("specialties", ArrayList(it))
            }

            hospitalData.doctors?.let {
                putParcelableArrayList("doctors", ArrayList(it))
            }

            hospitalData.additionalInfo?.let {
                putParcelable("additionalInfo", it)
            }

            hospitalData.operatingHours.let {
                putSerializable("operatingHours", HashMap(it)) // Map은 직접 Serializable로 넘겨야 함
            }
        }

        hospitalDetailFragment.arguments = bundle

        // 병원 상세 페이지 화면으로 넘어간다
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hospitalDetailFragment)
            .addToBackStack(null)
            .commit()
    }
}