package com.example.carepick.ui.hospital

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.carepick.MainActivity
import com.example.carepick.R
import com.example.carepick.TabOwner
import com.example.carepick.common.adapter.DoctorCardAdapter
import com.example.carepick.databinding.FragmentHospitalDetailBinding
import com.example.carepick.data.model.DoctorDetailsResponse
import com.example.carepick.data.repository.DoctorRepository
import com.example.carepick.data.repository.HospitalRepository
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch

// 병원 상세 페이지를 구현하는 코드
// - 지도 화면을 불러와서 병원 위치로 이동시키고, 병원 좌표에 마커를 찍는다
// - 병원에 대한 기본 정보를 출력한다
// - 병원에 대한 추가 정보를 출력한다
// - 해당 병원에 소속된 의사 목록을 출력한다
class HospitalDetailFragment : Fragment(), TabOwner {

    // 이 프래그먼트는 fragment_hospital_detail.xml 레이아웃을 사용함을 명시한다
    private var _binding: FragmentHospitalDetailBinding? = null
    private val binding get() = _binding!!

    private val hospitalRepository = HospitalRepository()
    private val doctorRepository = DoctorRepository()

    private var doctors = mutableListOf<DoctorDetailsResponse>()

    // 이 상세 페이지도 '검색' 탭의 일부임을 명시합니다.
    override fun getNavId(): Int = R.id.nav_search // 👈 메소드 추가

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // fragment_hospital_detail.xml을 객체화하여 _binding 객체에 저장한다
        // 이를 통해 레이아웃의 텍스트를 변경하는 등 속성을 수정할 수 있다
        _binding = FragmentHospitalDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    // 뷰가 생성되면 지도 화면에서 병원 위치로 이동하고 병원 자리에 마커를 생성한다
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0) // 상단 padding만 수동 적용
            insets
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val hospitalId = requireArguments().getString("hospitalId") ?: error("Hospital ID is required")

            val hospital = hospitalRepository.getHospitalById(hospitalId)

            // 레이아웃에 데이터를 바인딩한다
            // 이때 데이터가 존재하지 않으면 "데이터 없음" 으로 데이터를 넣는다
            binding.hospitalDetailName.text = hospital?.name ?: "데이터 없음"
            binding.hospitalDetailAddress.text = hospital?.address ?: "데이터 없음"
            binding.hospitalDetailPhone.text = hospital?.phoneNumber ?: "데이터 없음"
            // 이미지 url을 받아서 로드한다
            Glide.with(binding.root)
                .load(hospital?.images?.firstOrNull()?.url)
                .placeholder(R.drawable.sand_clock)
                .error(R.drawable.hospital_placeholder)
                .into(binding.hospitalDetailImage)

            // 월요일 데이터 추출 및 바인딩
//            val mondayHours = hospital?.operatingHours?.get("월")
//            val timeText = if (mondayHours != null) {
//                "${mondayHours.first} - ${mondayHours.second}"
//            } else {
//                "운영시간 정보 없음"
//            }
//            binding.hospitalDetailTimeWeekDayText.text = timeText
//
//            val holidayHours = hospital?.operatingHours?.get("공휴일")
//            val holidayText = when (holidayHours?.first) {
//                "휴진" -> "휴진"
//                else -> "${holidayHours?.first ?: "-"} - ${holidayHours?.second ?: "-"}"
//            }

//            binding.hospitalDetailTimeWeekendText.text = holidayText

            // 병원 추가 정보 표시
            if (hospital?.additionalInfo != null) {
                addInfoCheck(requireContext(), binding, hospital.additionalInfo) // 추가 정보가 비어있지 않다면 추가 정보 중 true 값인 것을 찾아서 텍스트 색을 바꾼다
            }

            // 의사 목록 로드
            doctors = hospital?.doctors?.mapNotNull { doctorId ->
                try {
                    val doctor = doctorRepository.getDoctorById(doctorId)
                    if (doctor != null) {
                        doctor
                    } else {
                        Log.e("DoctorLoadError", "Doctor with ID $doctorId not found")
                        null
                    }
                } catch (e: Exception) {
                    Log.e("DoctorLoadError", "Failed to load doctor with ID: $doctorId", e)
                    null
                }
            }?.toMutableList() ?: mutableListOf()

            // 의사 목록이 비어 있으면 안내 메시지를 보여줌
            if (doctors.isEmpty()) {
                binding.doctorListEmptyText.visibility = View.VISIBLE
            } else {
                binding.doctorListEmptyText.visibility = View.GONE
                binding.doctorListRecyclerView.adapter = DoctorCardAdapter(doctors, requireActivity())
            }

            // 의사 카드들은 좌우 스크롤을 할 수 있도록 수평 배치한다
            binding.doctorListRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            val fm = childFragmentManager
            var mapFragment = fm.findFragmentById(R.id.map) as? MapFragment
            val latitude = hospital?.location?.latitude ?: 0.0
            val longitude = hospital?.location?.longitude ?: 0.0

            if (mapFragment == null) {
                mapFragment = MapFragment.newInstance()
                fm.beginTransaction().add(R.id.map, mapFragment!!).commit()
            }

            mapFragment.getMapAsync { naverMap ->
                val location = LatLng(latitude, longitude)

                // 병원 위치로 이동
                naverMap.moveCamera(CameraUpdate.scrollTo(location))

                // 마커 생성 및 지도에 추가
                val marker = Marker()
                marker.position = location
                marker.map = naverMap
            }
        }

        // include된 헤더 내의 뒤로가기 버튼
        val backButton = view.findViewById<ImageButton>(R.id.btn_back)
        backButton.setOnClickListener {
            val manager = requireActivity().supportFragmentManager
            if (manager.backStackEntryCount > 0) {
                manager.popBackStack()
            } else {
                requireActivity().finish() // or moveTaskToBack(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (this is TabOwner) {
            (activity as? MainActivity)?.updateNavIcons(getNavId())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}