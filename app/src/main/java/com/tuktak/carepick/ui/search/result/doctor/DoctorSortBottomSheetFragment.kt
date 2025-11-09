package com.tuktak.carepick.ui.search.result.doctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.tuktak.carepick.databinding.SortDoctorMenuBinding // 👈 의사 전용 레이아웃 바인딩
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DoctorSortBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: SortDoctorMenuBinding? = null
    private val binding get() = _binding!!

    // ✅ 의사 전용 ViewModel 사용
    private val filterVM: DoctorFilterViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SortDoctorMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 초기 상태 설정 (ViewModel 값 기준)
        when (filterVM.selectedSortBy) {
            "distance" -> binding.sortDistance.isChecked = true
            "education" -> binding.sortEducation.isChecked = true
            else -> binding.sortDistance.isChecked = true
        }

        // 2. 체크박스 단일 선택 로직
        binding.sortDistance.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.sortEducation.isChecked = false
        }
        binding.sortEducation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.sortDistance.isChecked = false
        }

        // 3. 적용 버튼 클릭
        binding.applyFilterBtn.setOnClickListener {
            val sortBy = if (binding.sortEducation.isChecked) "education" else "distance"
            val buttonText = if (sortBy == "education") "학력순" else "거리순"

            // 결과 전달
            val result = Bundle().apply {
                putString("selected_sort_by", sortBy)
                putString("selected_filter_text", buttonText)
            }
            // ✅ 의사 전용 결과 키 사용
            setFragmentResult("doctor_sort_request", result)
            dismiss()
        }

        binding.closeBtn.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}