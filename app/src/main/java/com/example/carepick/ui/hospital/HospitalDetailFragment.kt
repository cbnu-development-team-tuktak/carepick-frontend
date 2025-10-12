package com.example.carepick.ui.hospital

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
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
import com.example.carepick.data.model.Hospital
import com.example.carepick.data.model.HospitalDetailsResponse
import com.example.carepick.data.model.HospitalOperatingHours
import com.example.carepick.data.repository.DoctorRepository
import com.example.carepick.data.repository.HospitalRepository
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

            if (hospital != null) {
                // 기본 정보 바인딩
                binding.hospitalDetailName.text = hospital.name ?: "데이터 없음"
                binding.hospitalDetailAddress.text = hospital.address ?: "데이터 없음"
                binding.hospitalDetailPhone.text = hospital.phoneNumber ?: "데이터 없음"

                Glide.with(binding.root)
                    .load(hospital.images?.firstOrNull()?.url)
                    .placeholder(R.drawable.sand_clock)
                    .error(R.drawable.hospital_placeholder)
                    .into(binding.hospitalDetailImage)

                // ▼▼▼▼▼ 운영 시간 처리 로직 추가 ▼▼▼▼▼
                setupOperatingHours(hospital)
                // ▲▲▲▲▲ 운영 시간 처리 로직 추가 ▲▲▲▲▲

                // 병원 추가 정보 표시
                hospital.additionalInfo?.let {
                    addInfoCheck(requireContext(), binding, it)
                }

                // 의사 목록 로드
                loadDoctors(hospital.doctors)

                // 지도 설정
                setupMap(hospital.location?.latitude, hospital.location?.longitude)
            } else {
                // 병원 정보가 없을 경우 처리
                // (예: 에러 메시지 표시)
            }
        }

        // 뒤로가기 버튼 설정
        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupOperatingHours(hospital: HospitalDetailsResponse) {
        val operatingHours = hospital.operatingHours

        if (operatingHours.isNullOrEmpty()) {
            binding.hospitalDetailTimeTodayLabel.text = "정보 없음"
            binding.hospitalDetailTimeText.text = "운영 정보 없음"
            binding.fullOperatingHoursText.text = "운영 정보 없음"
            binding.btnOperatingHoursToggle.visibility = View.GONE
            return
        }


        // 1. 현재 요일에 맞는 운영시간 표시
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayKorean = when (today) {
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            Calendar.SUNDAY -> "일"
            else -> ""
        }
        val todayHours = operatingHours.find { it.day == todayKorean }

        // 2. 현재 운영 상태 확인 및 UI 업데이트
        val status = checkOperatingStatus(todayHours) // 상태와 색상을 한 번에 받아옴
        binding.hospitalDetailTimeTodayLabel.text = status

        // 3. 오늘 운영 시간 텍스트 설정
        binding.hospitalDetailTimeText.text = formatOperatingHours(todayHours)

        // 2. 전체 운영시간 텍스트 생성
        val fullHoursText = StringBuilder()
        // 요일 순서대로 정렬
        val dayOrder = listOf("월", "화", "수", "목", "금", "토", "일", "공휴일")
        operatingHours.sortedBy { dayOrder.indexOf(it.day) }.forEach { hour ->
            fullHoursText.append("${hour.day}: ${formatOperatingHours(hour)}\n")
        }
        binding.fullOperatingHoursText.text = fullHoursText.toString().trim()

        // 3. 드롭다운 버튼 클릭 리스너 설정
        binding.btnOperatingHoursToggle.setOnClickListener {
            val fullHoursLayout = binding.fullOperatingHoursLayout
            if (fullHoursLayout.visibility == View.VISIBLE) {
                fullHoursLayout.visibility = View.GONE
                binding.btnOperatingHoursToggle.setImageResource(R.drawable.ic_arrow_down) // 아래 화살표
            } else {
                fullHoursLayout.visibility = View.VISIBLE
                binding.btnOperatingHoursToggle.setImageResource(R.drawable.ic_arrow_up) // 위 화살표 (ic_arrow_up 추가 필요)
            }
        }

    }

    private fun checkOperatingStatus(operatingHour: HospitalOperatingHours?): String {
        // 휴진이거나 시간 정보가 없으면 "운영 종료" 반환
        if (operatingHour?.startTime == null || operatingHour.endTime == null) {
            return "운영 종료"
        }

        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val now = sdf.parse(sdf.format(Date()))
            val start = sdf.parse(operatingHour.startTime)
            val end = sdf.parse(operatingHour.endTime)

            // 현재 시간이 시작 시간과 종료 시간 사이에 있는지 확인
            return if (now.after(start) && now.before(end)) {
                "운영 중"
            } else {
                "운영 종료"
            }
        } catch (e: Exception) {
            // 시간 포맷 파싱 중 에러 발생 시
            Log.e("TimeParseError", "Could not parse operating hours", e)
            return "운영 종료"
        }
    }

    private fun formatOperatingHours(hours: HospitalOperatingHours?): String {
        return if (hours?.startTime != null && hours.endTime != null) {
            "${hours.startTime} - ${hours.endTime}"
        } else {
            "휴진" // startTime 또는 endTime이 null이면 휴진으로 간주
        }
    }

    /**
     * 의사 목록을 로드하고 RecyclerView에 설정하는 함수
     */
    private suspend fun loadDoctors(doctorIds: List<String>?) {
        doctors = doctorIds?.mapNotNull { doctorId ->
            try {
                doctorRepository.getDoctorById(doctorId)
            } catch (e: Exception) {
                Log.e("DoctorLoadError", "Failed to load doctor with ID: $doctorId", e)
                null
            }
        }?.toMutableList() ?: mutableListOf()

        if (doctors.isEmpty()) {
            binding.doctorListEmptyText.visibility = View.VISIBLE
            binding.doctorListRecyclerView.visibility = View.GONE
        } else {
            binding.doctorListEmptyText.visibility = View.GONE
            binding.doctorListRecyclerView.visibility = View.VISIBLE
            binding.doctorListRecyclerView.adapter = DoctorCardAdapter(doctors, requireActivity())
            binding.doctorListRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    /**
     * 지도를 설정하고 마커를 표시하는 함수
     */
    private fun setupMap(latitude: Double?, longitude: Double?) {
        if (latitude == null || longitude == null) return

        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as? MapFragment
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync { naverMap ->
            val location = LatLng(latitude, longitude)
            naverMap.moveCamera(CameraUpdate.scrollTo(location))
            Marker().apply {
                position = location
                map = naverMap
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