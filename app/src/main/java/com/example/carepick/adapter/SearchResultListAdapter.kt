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

// 검색 결과 화면에서 병원과 의사 정보에서 데이터를 읽어서 레이아웃에 바인딩하는 코드
class SearchResultListAdapter(
    // 병원과 의사 정보를 모두 담을 수 있는 SearchResultItem 객체 형태로 받을 것임을 선언
    private val items: List<SearchResultItem>,
    private val activity: FragmentActivity
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 특정 데이터가 병원인지, 의사 데이터인지 구분할 수 있도록 타입 설정
    companion object {
        private const val TYPE_HOSPITAL = 0
        private const val TYPE_DOCTOR = 1
    }

    override fun getItemCount(): Int = items.size

    // items의 각각의 데이터에 대해 병원 데이터인지, 의사 데이터인지를 보고 타입을 정해준다
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {

            // HospitalDetailResponse 형태로 넘어왔다면 병원 타입이다
            is HospitalDetailsResponse -> TYPE_HOSPITAL
            // DoctorDetailResponse 형태로 넘어왔다면 의사 타입나다
            is DoctorDetailsResponse -> TYPE_DOCTOR

            // 그 외에는 예외처리
            else -> throw IllegalArgumentException("Unknown type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        // 뷰 타입에 따라 다른 동작 수행
        return when (viewType) {
            // 뷰 타입이 병원인 경우
            TYPE_HOSPITAL -> {
                // search_list.xml 레이아웃을 바인딩한다
                val binding = SearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                // 병원 뷰홀더를 호출한다
                HospitalViewHolder(binding)
            }
            // 뷰 타입이 의사인 경우
            TYPE_DOCTOR -> {
                // search_list.xml 레이아웃을 바인딩한다
                val binding = SearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                // 의사 뷰홀더를 호출한다
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
    }

    // search_list.xml에 병원 정보를 바인딩한다
    inner class HospitalViewHolder(private val binding: SearchListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(hospital: HospitalDetailsResponse) {
            binding.searchListName.text = hospital.name
            binding.searchListAddress.text = hospital.address

            // url을 통해 병원 이미지를 불러온다
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

    // search_list.xml에 의사 정보를 바인딩한다
    inner class DoctorViewHolder(private val binding: SearchListBinding) :
        RecyclerView.ViewHolder(binding.root) {
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
            val specialtyAdapter = SpecialtyAdapterNoBg(specialtyArray)
            binding.searchListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            binding.searchListRecyclerView.adapter = specialtyAdapter
        }
    }

    // 병원을 선택할 경우 해당 병원의 상세 페이지로 넘어가야 한다
    private fun navigateToDetailHospital(hospitalData: HospitalDetailsResponse) {
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

    // 의사를 선택하면 의사 상세 페이지로 넘어간다
    private fun navigateToDetailDoctor(doctorData: DoctorDetailsResponse) {

    }
}