package com.example.carepick.ui.search.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.example.carepick.databinding.SortFilterMenuBinding
import com.example.carepick.ui.search.FilterViewModel
import com.example.carepick.ui.search.result.SearchMode
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SortFilterBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: SortFilterMenuBinding? = null
    private val binding get() = _binding!!

    private val filterVM: FilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SortFilterMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 1. arguments로부터 현재 검색 모드를 가져옵니다.
        val modeString = arguments?.getString("current_search_mode")
        val currentMode = if (modeString == "DOCTOR") SearchMode.DOCTOR else SearchMode.HOSPITAL

        // ✅ 2. 검색 모드에 따라 체크박스의 UI를 동적으로 변경합니다.
        when (currentMode) {
            SearchMode.HOSPITAL -> {
                // 병원 모드일 때: '거리순', '가나다순'
                binding.sortDistance.visibility = View.VISIBLE
                binding.sortExperience.visibility = View.VISIBLE // '경험순' 체크박스를 '가나다순'으로 재사용
                binding.sortExperience.text = "가나다순"
                binding.sortEducation.visibility = View.GONE // '학력순' 숨기기
            }
            SearchMode.DOCTOR -> {
                // 의사 모드일 때: '거리순', '학력순'
                binding.sortDistance.visibility = View.VISIBLE
                binding.sortExperience.visibility = View.GONE // '경험순/가나다순' 숨기기
                binding.sortEducation.visibility = View.VISIBLE
            }
        }

        // --- 👇 ViewModel 값으로 초기 체크 상태 설정 👇 ---
        when (filterVM.selectedSortBy) {
            "distance" -> binding.sortDistance.isChecked = true
            "name" -> if (currentMode == SearchMode.HOSPITAL) binding.sortExperience.isChecked = true // 병원: 가나다순(경험 체크박스 재사용)
            "education" -> if (currentMode == SearchMode.DOCTOR) binding.sortEducation.isChecked = true // 의사: 학력순
            // 기본값 (distance) 또는 예외 처리
            else -> binding.sortDistance.isChecked = true
        }

        // 닫기 버튼 동작
        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        // 체크박스
        binding.sortDistance.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.sortExperience.isChecked = false
                binding.sortEducation.isChecked = false
            }
        }
        binding.sortExperience.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.sortDistance.isChecked = false
                binding.sortEducation.isChecked = false
            }
        }
        binding.sortEducation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.sortDistance.isChecked = false
                binding.sortExperience.isChecked = false
            }
        }

        // 적용 버튼
        binding.applyFilterBtn.setOnClickListener {
            // ✅ 선택된 체크박스와 현재 모드에 따라 sortBy 값 결정
            val sortByValue = when {
                binding.sortDistance.isChecked -> "distance"
                binding.sortExperience.isChecked && currentMode == SearchMode.HOSPITAL -> "name" // 병원 모드 + '가나다순' 체크박스 -> "name"
                binding.sortEducation.isChecked && currentMode == SearchMode.DOCTOR -> "education" // 의사 모드 + '학력순' 체크박스 -> "education"
                else -> "distance" // 아무것도 선택 안됐거나 예외 경우 기본값
            }

            val selectedText = when (sortByValue) { // 결과 화면 버튼 텍스트용
                "distance" -> "거리순"
                "name" -> "가나다순"
                "education" -> "학력순"
                else -> "거리순"
            }

            val result = Bundle().apply {
                putString("selected_sort_by", sortByValue) // ✅ API에 전달할 sortBy 값
                putString("selected_filter_text", selectedText) // 결과 화면 버튼 텍스트
            }
            setFragmentResult("sort_filter_result", result)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
