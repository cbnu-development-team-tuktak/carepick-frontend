package com.example.carepick.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.databinding.SearchListBinding
import com.example.carepick.dto.doctor.DoctorDetailsResponse
import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.model.SearchResultItem
import com.example.carepick.ui.hospital.HospitalDetailFragment

class SearchResultListAdapter(
    private val items: List<SearchResultItem>,
    private val activity: FragmentActivity
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HOSPITAL = 0
        private const val TYPE_DOCTOR = 1
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HospitalDetailsResponse -> TYPE_HOSPITAL
            is DoctorDetailsResponse -> TYPE_DOCTOR
            else -> throw IllegalArgumentException("Unknown type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HOSPITAL -> {
                val binding = SearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HospitalViewHolder(binding)
            }
            TYPE_DOCTOR -> {
                val binding = SearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DoctorViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HospitalDetailsResponse -> (holder as HospitalViewHolder).bind(item)
            is DoctorDetailsResponse -> (holder as DoctorViewHolder).bind(item)
        }
//        val binding = (holder as HospitalSearchListViewHolder).binding
//
//        // 병원 정보들을 한번에 받는 객체
//        val hospitalData = items[position]
//
//        val specialties = hospitalData.specialties ?: emptyList()
//
//        val imageUrl = hospitalData.images?.firstOrNull()?.url ?: ""
//
//        binding.hospitalSearchListName.text = hospitalData.name
//        binding.hospitalSearchListAddress.text = hospitalData.address
//        Glide.with(binding.root)
//            .load(imageUrl)
//            .placeholder(R.drawable.sand_clock)
//            .error(R.drawable.warning)
//            .into(binding.hospitalSearchListImage)
//
//        // 진료과 목록에 따라 동적으로 진료과를 카드에 추가한다
//        val specialtyAdapter = SpecialtyAdapterNoBg(specialties)
//        binding.hospitalSearchListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
//        binding.hospitalSearchListRecyclerView.adapter = specialtyAdapter
//
//        // 카드를 선택했을 때 다음의 동작들을 수행한다
//        binding.root.setOnClickListener {
//            navigateToDetail(hospitalData)
//        }
    }

    inner class HospitalViewHolder(private val binding: SearchListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(hospital: HospitalDetailsResponse) {
            binding.searchListName.text = hospital.name
            binding.searchListAddress.text = hospital.address

            val imageUrl = hospital.images?.firstOrNull()?.url ?: ""
            Glide.with(binding.root)
                .load(imageUrl)
                .placeholder(R.drawable.sand_clock)
                .error(R.drawable.warning)
                .into(binding.searchListImage)

            // 진료과 목록에 따라 동적으로 진료과를 카드에 추가한다
            val specialties = hospital.specialties ?: emptyList()
            val specialtyAdapter = SpecialtyAdapterNoBg(specialties)
            binding.searchListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            binding.searchListRecyclerView.adapter = specialtyAdapter
        }
    }

    inner class DoctorViewHolder(private val binding: SearchListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(doctor: DoctorDetailsResponse) {
            binding.searchListName.text = doctor.name
            binding.searchListAddress.text = "병원 이름이 들어가야 함"

            val imageUrl = doctor.profileImage?: ""
            Glide.with(binding.root)
                .load(imageUrl)
                .placeholder(R.drawable.sand_clock)
                .error(R.drawable.warning)
                .into(binding.searchListImage)

            // 진료과 목록에 따라 동적으로 진료과를 카드에 추가한다
            val specialtyArray= doctor.specialty?.split(",")?.map { it.trim() } ?: emptyList()// 공백 제거 포함
            val specialtyAdapter = SpecialtyAdapterNoBg(specialtyArray)
            binding.searchListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            binding.searchListRecyclerView.adapter = specialtyAdapter
        }
    }

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
        }

        hospitalDetailFragment.arguments = bundle

        // 병원 상세 페이지 화면으로 넘어간다
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hospitalDetailFragment)
            .addToBackStack(null)
            .commit()
    }
}