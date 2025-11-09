package com.tuktak.carepick.ui.search.result.hospital

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tuktak.carepick.databinding.SortHospitalMenuBinding

class HospitalSortBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: SortHospitalMenuBinding? = null
    private val binding get() = _binding!!

    // ✅ 병원 전용 ViewModel
    private val filterVM: HospitalFilterViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SortHospitalMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 초기 상태 설정 (ViewModel 값 기준)
        when (filterVM.selectedSortBy) {
            "distance" -> binding.sortDistance.isChecked = true
            "name" -> binding.sortName.isChecked = true
            else -> binding.sortDistance.isChecked = true
        }

        // 2. 체크박스 단일 선택 로직
        binding.sortDistance.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.sortName.isChecked = false
        }
        binding.sortName.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.sortDistance.isChecked = false
        }

        // 3. 적용 버튼 클릭
        binding.applyFilterBtn.setOnClickListener {
            val sortBy = if (binding.sortName.isChecked) "name" else "distance"
            val buttonText = if (sortBy == "name") "가나다순" else "거리순"

            // ViewModel 업데이트 (선택 사항, ResultListener에서 처리해도 됨)
            // filterVM.updateSortBy(sortBy)

            // 결과 전달
            val result = Bundle().apply {
                putString("selected_sort_by", sortBy)
                putString("selected_filter_text", buttonText)
            }
            // ✅ 병원 전용 결과 키 사용
            setFragmentResult("hospital_sort_request", result)
            dismiss()
        }

        binding.closeBtn.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}