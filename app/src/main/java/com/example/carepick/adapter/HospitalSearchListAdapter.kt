package com.example.carepick.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.databinding.HospitalSearchListBinding
import com.example.carepick.dto.HospitalDetailsResponse
import com.example.carepick.ui.hospital.HospitalDetailFragment
import com.example.carepick.viewHolder.HospitalSearchListViewHolder

class HospitalSearchListAdapter(
    private val data: MutableList<HospitalDetailsResponse>,
    private val activity: FragmentActivity
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        HospitalSearchListViewHolder(HospitalSearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as HospitalSearchListViewHolder).binding

        // 병원 정보들을 한번에 받는 객체
        val hospitalData = data[position]

        val imageUrl = hospitalData.images?.firstOrNull()?.url ?: ""

        binding.hospitalSearchListName.text = hospitalData.name
        binding.hospitalSearchListAddress.text = hospitalData.address
        Glide.with(binding.root)
            .load(imageUrl)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.warning)
            .into(binding.hospitalSearchListImage)

        // 카드를 선택했을 때 다음의 동작들을 수행한다
        binding.root.setOnClickListener {
            navigateToDetail(hospitalData, imageUrl)
        }
    }

    private fun navigateToDetail(hospitalData: HospitalDetailsResponse, imageUrl: String) {
        // 카드뷰를 클릭했을 때 넘어갈 Fragment를 객체에 저장
        val hospitalDetailFragment = HospitalDetailFragment()

        // 병원 상세 페이지에 전달할 데이터를 지정한다
        val bundle = Bundle().apply {
            putString("name", hospitalData.name)
            putString("phoneNumber", hospitalData.phoneNumber)
            putString("homepage", hospitalData.homepage)
            putString("address", hospitalData.address)
            putString("imageUrl", imageUrl)
            putString("operatingHours", hospitalData.operatingHours)

            hospitalData.location?.latitude?.let { putDouble("latitude", it) }
            hospitalData.location?.longitude?.let { putDouble("longitude", it) }

            hospitalData.specialties?.let {
                putStringArrayList("specialties", ArrayList(it))
            }

            hospitalData.doctors?.let {
                putStringArrayList("doctors", ArrayList(it))
            }

            hospitalData.additionalInfo?.let {
                putParcelable("additionalInfo", it)
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