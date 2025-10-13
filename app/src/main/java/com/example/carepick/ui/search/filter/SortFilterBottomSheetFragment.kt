package com.example.carepick.ui.search.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.example.carepick.databinding.SortFilterMenuBinding
import com.example.carepick.ui.search.result.SearchMode
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SortFilterBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: SortFilterMenuBinding? = null
    private val binding get() = _binding!!

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
            // ✅ '가나다순'이 선택되었을 때의 분기 처리 추가
            val selectedText = when {
                binding.sortDistance.isChecked -> "거리순"
                binding.sortExperience.isChecked -> if (currentMode == SearchMode.HOSPITAL) "가나다순" else "근속연수순" // 병원 모드일땐 '가나다순'
                binding.sortEducation.isChecked -> "학력순"
                else -> null
            }

            val result = Bundle().apply {
                putString("selected_filter_text", selectedText)
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
