package com.tuktak.carepick.ui.hospital

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.tuktak.carepick.R
import com.tuktak.carepick.common.adapter.TextListAdapter
import com.tuktak.carepick.data.model.DoctorDetailsResponse
import com.tuktak.carepick.data.repository.DoctorRepository
import com.tuktak.carepick.databinding.DialogDoctorDetailBinding // 👈 Dialog용 레이아웃 바인딩
import com.tuktak.carepick.ui.selfDiagnosis.adapter.SpecialtyAdapter
import kotlinx.coroutines.launch

class DoctorDetailDialogFragment : DialogFragment() {

    private var _binding: DialogDoctorDetailBinding? = null
    private val binding get() = _binding!!
    private val doctorRepository = DoctorRepository()

    // 1. companion object를 사용하여 BottomSheet를 생성하는 표준 방식
    companion object {
        const val TAG = "DoctorDetailDialog"
        private const val ARG_DOCTOR_ID = "doctor_id"

        fun newInstance(doctorId: String): DoctorDetailDialogFragment {
            return DoctorDetailDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DOCTOR_ID, doctorId)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDoctorDetailBinding.inflate(inflater, container, false)

        dialog?.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // ✅ [2. 이 한 줄을 추가]
            // 이 다이얼로그 창은 시스템 UI(상태바, 네비게이션 바) 영역을
            // 존중하도록(침범하지 않도록) 설정합니다.
            WindowCompat.setDecorFitsSystemWindows(this, true)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // ✅ 다이얼로그의 너비를 화면에 꽉 차게 설정 (좌우 여백은 XML에서 관리)
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(android.view.Gravity.BOTTOM) // 창을 하단에 붙임
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCloseBottomSheet.setOnClickListener {
            dismiss() // 다이얼로그 닫기
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val doctorId = arguments?.getString(ARG_DOCTOR_ID) ?: return@launch
            val doctor = doctorRepository.getDoctorById(doctorId) ?: return@launch
            populateDoctorData(doctor)
        }
    }

    // 3. 데이터 바인딩 로직 (기존 DoctorDetailFragment에서 가져옴)
    private fun populateDoctorData(doctor: DoctorDetailsResponse) {
        val cleanName = doctor.name.replace("\\[.*\\]".toRegex(), "").trim()
        binding.doctorDetailName.text = cleanName

        // 병원 상세에서 띄운 것이므로, 병원 이름/주소 클릭 로직은 제거해도 됨
        binding.doctorDetailHospitalName.text = doctor.hospitalName ?: "소속 병원 정보 없음"
        // ... (병원 주소 로직 제거) ...
        binding.doctorDetailAddress.visibility = View.GONE // 모달에서는 숨김

        Glide.with(binding.root)
            .load(doctor.profileImage)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.doctor_placeholder)
            .into(binding.doctorDetailImage)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}