package com.example.carepick.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.setFragmentResult
import com.example.carepick.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SortFilterBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sort_filter_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 닫기 버튼 동작
        val closeButton = view.findViewById<ImageView>(R.id.close_btn)
        closeButton.setOnClickListener {
            dismiss()
        }

        // 체크박스
        val checkDistance = view.findViewById<CheckBox>(R.id.sort_distance)
        val checkExperience = view.findViewById<CheckBox>(R.id.sort_experience)
        val checkEducation = view.findViewById<CheckBox>(R.id.sort_education)

        // 서로 하나만 선택 가능하게 처리
        checkDistance.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkExperience.isChecked = false
                checkEducation.isChecked = false
            }
        }
        checkExperience.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkDistance.isChecked = false
                checkEducation.isChecked = false
            }
        }
        checkEducation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkDistance.isChecked = false
                checkExperience.isChecked = false
            }
        }

        // 적용 버튼
        val applyButton = view.findViewById<Button>(R.id.apply_filter_btn)
        applyButton.setOnClickListener {
            // 선택된 정렬 기준 텍스트
            val selectedText = when {
                checkDistance.isChecked -> "거리순"
                checkExperience.isChecked -> "근속연수순"
                checkEducation.isChecked -> "학력순"
                else -> null
            }

            val result = Bundle().apply {
                putString("selected_filter_text", selectedText)
            }
            setFragmentResult("sort_filter_result", result)
            dismiss()
        }
    }
}