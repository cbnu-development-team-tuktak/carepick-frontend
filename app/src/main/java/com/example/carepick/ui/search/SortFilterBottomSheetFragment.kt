package com.example.carepick.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.example.carepick.databinding.SortFilterMenuBinding
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
            val selectedText = when {
                binding.sortDistance.isChecked -> "거리순"
                binding.sortExperience.isChecked -> "근속연수순"
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
