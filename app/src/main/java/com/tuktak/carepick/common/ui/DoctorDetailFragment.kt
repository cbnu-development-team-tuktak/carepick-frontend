package com.tuktak.carepick.common.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
                    // 1. 이동할 HospitalDetailFragment 인스턴스 생성
                    val hospitalDetailFragment = HospitalDetailFragment()

                    // 2. hospitalId를 전달하기 위한 Bundle 생성
                    val bundle = Bundle()
                    bundle.putString("hospitalId", hospitalId)
                    hospitalDetailFragment.arguments = bundle

                    // 3. Fragment Transaction을 통해 화면 전환
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, hospitalDetailFragment) // fragment_container는 MainActivity의 FragmentContainerView ID
                        .addToBackStack(null) // 뒤로가기 버튼으로 현재 프래그먼트로 돌아올 수 있게 함
                        .commit()
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

        // 뒤로가기 버튼 (기존 코드)
        val backButton = view.findViewById<ImageButton>(R.id.btn_back)
        backButton.setOnClickListener {
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