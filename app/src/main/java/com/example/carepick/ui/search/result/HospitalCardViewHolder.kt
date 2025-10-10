package com.example.carepick.ui.search.result

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.ui.selfDiagnosis.adapter.SpecialtyAdapter
import com.example.carepick.databinding.HospitalCardBinding
import com.example.carepick.data.model.HospitalDetailsResponse
import com.example.carepick.ui.hospital.HospitalDetailFragment

// 병원 즐겨찾기용
// hospital_card.xml 레이아웃을 바인딩한 객체에 속성으로 접근하여 데이터를 넣는 코드
// 뷰 어디에 어떤 데이터가 들어갈지를 보여준다
class HospitalCardViewHolder(
    val binding: HospitalCardBinding,
    private val activity: FragmentActivity
) : RecyclerView.ViewHolder(binding.root){
    fun bind(hospital: HospitalDetailsResponse) {

        // 진료 과목을 추출한다
        val specialties = hospital.specialties ?: emptyList()

        // 첫번째 이미지를 가져와서 url을 추출한다
        val imageUrl = hospital.images?.firstOrNull()?.url ?: ""

        binding.hospitalCardName.text = hospital.name
        binding.hospitalCardAddress.text = hospital.address

        Glide.with(binding.root)
            .load(imageUrl)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.hospital_placeholder)
            .into(binding.hospitalPicture)

        // 진료 과목 개수에 따라 동적으로 생성한다
        val specialtyAdapter = SpecialtyAdapter(specialties)
        binding.hospitalCardRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.hospitalCardRecyclerView.adapter = specialtyAdapter

        // 병원 카드를 클릭했을 때 수행할 동작
        binding.root.setOnClickListener {
            navigateToDetail(hospital)
        }
    }

    // 사용자가 선택한 병원의 상세 페이지로 이동한다
    private fun navigateToDetail(hospitalData: HospitalDetailsResponse) {
        // 카드뷰를 클릭했을 때 넘어갈 Fragment를 객체에 저장
        val hospitalDetailFragment = HospitalDetailFragment()

        // 병원 상세 페이지에 전달할 데이터를 지정한다
        val bundle = Bundle().apply {
            putString("hospitalId", hospitalData.id)
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