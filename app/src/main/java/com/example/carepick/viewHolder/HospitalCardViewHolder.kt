package com.example.carepick.viewHolder

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.adapter.SpecialtyAdapter
import com.example.carepick.databinding.HospitalCardBinding
import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.ui.hospital.HospitalDetailFragment

// layout 폴더의 hospital_card.xml 레이아웃을 객체처럼 접근하여 속성(텍스트 등등)을 수정하기 위해 binding 시키는 부분
// HospitalListViewHolder는 병원 카드를 바인딩한 객체를 받고 리사이클러뷰를 반환한다
class HospitalCardViewHolder(
    val binding: HospitalCardBinding,
    private val activity: FragmentActivity
) : RecyclerView.ViewHolder(binding.root){
    fun bind(hospital: HospitalDetailsResponse) {
        val specialties = hospital.specialties ?: emptyList()
        val imageUrl = hospital.images?.firstOrNull()?.url ?: ""

        binding.hospitalCardName.text = hospital.name
        binding.hospitalCardAddress.text = hospital.address

        Glide.with(binding.root)
            .load(imageUrl)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.warning)
            .into(binding.hospitalPicture)

        val specialtyAdapter = SpecialtyAdapter(specialties)
        binding.hospitalCardRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.hospitalCardRecyclerView.adapter = specialtyAdapter

        binding.root.setOnClickListener {
            navigateToDetail(hospital)
        }
    }

    // 카드를 선택했을 때 수행할 로직을 담은 코드
    private fun navigateToDetail(hospitalData: HospitalDetailsResponse) {
        // 카드뷰를 클릭했을 때 넘어갈 Fragment를 객체에 저장
        val hospitalDetailFragment = HospitalDetailFragment()

        // 병원 상세 페이지에 전달할 데이터를 지정한다
        val bundle = Bundle().apply {
            putString("name", hospitalData.name)
            putString("phoneNumber", hospitalData.phoneNumber)
            putString("homepage", hospitalData.homepage)
            putString("address", hospitalData.address)
            putString("operatingHours", hospitalData.operatingHours)

            hospitalData.location?.latitude?.let { putDouble("latitude", it) }
            hospitalData.location?.longitude?.let { putDouble("longitude", it) }

            hospitalData.images?.let {
                putParcelableArrayList("images", ArrayList(hospitalData.images ?: emptyList()))
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
        }

        // 번들에 잘 담은 데이터들을 arguments라는 이름으로 넘겨준다
        hospitalDetailFragment.arguments = bundle

        // 병원 상세 페이지 화면으로 넘어간다
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hospitalDetailFragment)
            .addToBackStack(null)
            .commit()
    }
}