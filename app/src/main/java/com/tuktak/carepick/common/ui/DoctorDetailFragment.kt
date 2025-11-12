package com.tuktak.carepick.common.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.tuktak.carepick.MainActivity
import com.tuktak.carepick.R
import com.tuktak.carepick.TabOwner
import com.tuktak.carepick.common.adapter.TextListAdapter
import com.tuktak.carepick.ui.selfDiagnosis.adapter.SpecialtyAdapter
import com.tuktak.carepick.databinding.FragmentDoctorDetailBinding
import com.tuktak.carepick.data.repository.DoctorRepository
import com.tuktak.carepick.data.repository.HospitalRepository
import com.tuktak.carepick.ui.hospital.HospitalDetailFragment
import kotlinx.coroutines.launch

class DoctorDetailFragment: Fragment(), TabOwner {
    private var _binding: FragmentDoctorDetailBinding? = null
    private val binding get() = _binding!!

    private val doctorRepository = DoctorRepository()
    private val hospitalRepository = HospitalRepository()

    // 이 상세 페이지도 '검색' 탭의 일부임을 명시합니다.
    override fun getNavId(): Int = R.id.nav_doctor // 👈 메소드 추가

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Inset 리스너를 프래그먼트의 루트 뷰(binding.root)에 적용
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            // 1. 시스템 UI(상태바, 네비게이션 바) 영역 정보를 가져옵니다.
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 2. CommonHeader의 루트 뷰에 상단(상태 바) 패딩 적용
            binding.CommonHeader.root.updatePadding(top = systemBars.top)

            // 3. ScrollView에 하단(시스템 네비게이션 바) 패딩 적용
            binding.doctorDetailScrollView.updatePadding(bottom = systemBars.bottom)

            // 4. Insets을 '소비'하여 시스템이 추가 패딩을 적용하지 않도록 합니다.
            WindowInsetsCompat.CONSUMED
        }

        viewLifecycleOwner.lifecycleScope.launch{
            val doctorId = arguments?.getString("doctorId") ?: return@launch
            val doctor = doctorRepository.getDoctorById(doctorId) ?: return@launch

            Log.d("doctorInfo", "$doctor")

            // 의사 이름에서 "[전문의]" 같은 부분 제거
            val cleanName = doctor.name.replace("\\[.*\\]".toRegex(), "").trim()
            // 의사 이름 데이터를 넣는다
            binding.doctorDetailName.text = cleanName

            // 소속 병원 이름
            // 병원 이름 텍스트에 ">" 기호를 추가하여 이동 가능함을 암시
            val hospitalNameText = (doctor.hospitalName ?: "소속 병원 정보 없음") + " >"
            binding.doctorDetailHospitalName.text = hospitalNameText

            // TODO: 병원 주소는 현재 Doctor 데이터에 없으므로, 필요 시 hospitalId로 별도 조회 필요

            // 의사 이미지를 넣는다
            Glide.with(binding.root)
                .load(doctor.profileImage)
                .placeholder(R.drawable.sand_clock)
                .error(R.drawable.doctor_placeholder)
                .into(binding.doctorDetailImage)

            // ▼▼▼▼▼ 병원 주소 가져오기 로직 추가 ▼▼▼▼▼
            // 1. 의사 정보에서 hospitalId를 가져옵니다.
            val hospitalId = doctor.hospitalId
            if (!hospitalId.isNullOrBlank()) {
                // 2. hospitalId를 이용해 병원 정보를 비동기로 조회합니다.
                val hospital = hospitalRepository.getHospitalById(hospitalId)

                // 병원 주소 입력
                binding.doctorDetailAddress.text = hospital?.address ?: "주소 정보 없음"

                // 병원 이름 선택시 병원 상세 화면으로 전환
                binding.doctorDetailHospitalName.setOnClickListener {
                    // 1. 전달할 병원 ID를 Bundle에 담습니다.
                    val bundle = Bundle()
                    bundle.putString("hospitalId", hospitalId)

                    // 2. ✅ [추가] "교차 탭 이동" 깃발을 true로 설정합니다.
                    bundle.putBoolean("IS_CROSS_TAB_NAVIGATION", true)

                    // 2. ✅ MainActivity의 navigateToTab 함수를 호출하여
                    //    '병원' 탭(R.id.nav_hospital)으로 Bundle과 함께 이동을 요청합니다.
                    (activity as? MainActivity)?.navigateToTab(R.id.nav_hospital, bundle)
                }
            } else {
                // hospitalId가 없는 경우
                binding.doctorDetailAddress.text = "주소 정보 없음"
            }
            // ▲▲▲▲▲ 병원 주소 가져오기 로직 추가 ▲▲▲▲▲


            // 진료과 목록 (기존 코드)
            doctor.specialties.let {
                binding.doctorDetailSpecialties.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                binding.doctorDetailSpecialties.adapter = SpecialtyAdapter(it)
            }

            // ▼▼▼▼▼ 자격/면허 목록 RecyclerView 설정 ▼▼▼▼▼
            val licenses = doctor.educationLicenses?.mapNotNull { it.first } ?: emptyList()
            if (licenses.isNotEmpty()) {
                // 데이터가 있으면 RecyclerView를 보여주고, empty 텍스트는 숨김
                binding.doctorDetailLicenseList.visibility = View.VISIBLE
                binding.doctorDetailLicenseEmptyText.visibility = View.GONE
                binding.doctorDetailLicenseList.layoutManager = LinearLayoutManager(requireContext())
                binding.doctorDetailLicenseList.adapter = TextListAdapter(licenses)
            } else {
                // 데이터가 없으면 RecyclerView를 숨기고, empty 텍스트를 보여줌
                binding.doctorDetailLicenseList.visibility = View.GONE
                binding.doctorDetailLicenseEmptyText.visibility = View.VISIBLE
            }
            // ▲▲▲▲▲ 자격/면허 목록 RecyclerView 설정 ▲▲▲▲▲


            // ▼▼▼▼▼ 경력 목록 RecyclerView 설정 ▼▼▼▼▼
            val careers = doctor.careers ?: emptyList()
            if (careers.isNotEmpty()) {
                // 데이터가 있으면 RecyclerView를 보여주고, empty 텍스트는 숨김
                binding.doctorDetailCareerList.visibility = View.VISIBLE
                binding.doctorDetailCareerEmptyText.visibility = View.GONE
                binding.doctorDetailCareerList.layoutManager = LinearLayoutManager(requireContext())
                binding.doctorDetailCareerList.adapter = TextListAdapter(careers)
            } else {
                // 데이터가 없으면 RecyclerView를 숨기고, empty 텍스트를 보여줌
                binding.doctorDetailCareerList.visibility = View.GONE
                binding.doctorDetailCareerEmptyText.visibility = View.VISIBLE
            }
            // ▲▲▲▲▲ 경력 목록 RecyclerView 설정 ▲▲▲▲▲


            // ▼▼▼▼▼ 케어픽 스코어 설정 ▼▼▼▼▼
            // 1. 점수가 null이 아니면 String.format을 사용하여 소수점 두 자리까지 형식화
            val formattedScore = doctor.totalEducationLicenseScore?.let { score ->
                String.format("%.2f", score)
            } ?: "점수 없음" // 2. 점수가 null이면 "점수 없음"을 사용

            binding.doctorDetailScore.text = formattedScore
            // ▲▲▲▲▲ 케어픽 스코어 설정 ▲▲▲▲▲
        }

        // 뒤로가기 버튼
        // ❗️ CommonHeader.root가 아닌, binding.CommonHeader로 헤더 내부 뷰에 접근해야 합니다.
        binding.CommonHeader.btnBack.setOnClickListener { // 'btn_back' ID로 가정
            parentFragmentManager.popBackStack()
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