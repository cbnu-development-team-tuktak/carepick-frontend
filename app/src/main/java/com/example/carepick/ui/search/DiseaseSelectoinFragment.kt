package com.example.carepick.ui.search

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.carepick.R
import com.example.carepick.databinding.FragmentDiseaseSelectionBinding

class DiseaseSelectionFragment : Fragment() {

    private var _binding: FragmentDiseaseSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiseaseSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // ✅ 검색 필드와 추천 박스 설정
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    binding.suggestion1.text = "감기"
                    binding.suggestion2.text = "기흉"
                    binding.suggestion3.text = "각막염"
                    binding.suggestion1.visibility = View.VISIBLE
                    binding.suggestion2.visibility = View.VISIBLE
                    binding.suggestion3.visibility = View.VISIBLE
                    binding.suggestionBox.visibility = View.VISIBLE
                } else {
                    binding.suggestion1.visibility = View.GONE
                    binding.suggestion2.visibility = View.GONE
                    binding.suggestion3.visibility = View.GONE
                    binding.suggestionBox.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 추천 단어 클릭 시
        binding.suggestion1.setOnClickListener { addTag("감기") }
        binding.suggestion2.setOnClickListener { addTag("기흉") }
        binding.suggestion3.setOnClickListener { addTag("각막염") }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addTag(tagText: String) {
        val tagView = LayoutInflater.from(context).inflate(R.layout.tag_item, binding.selectedTagsContainer, false)
        val textView = tagView.findViewById<TextView>(R.id.tag_text)
        val closeBtn = tagView.findViewById<TextView>(R.id.tag_close)

        textView.text = tagText
        closeBtn.setOnClickListener {
            binding.selectedTagsContainer.removeView(tagView)
        }

        binding.selectedTagsContainer.addView(tagView)
    }
}