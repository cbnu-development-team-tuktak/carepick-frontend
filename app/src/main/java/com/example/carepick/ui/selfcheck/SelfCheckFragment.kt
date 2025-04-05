package com.example.carepick.ui.selfcheck

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.carepick.R
import com.example.carepick.databinding.FragmentSelfCheckBinding

// 아직 구현을 시작하지 않음
class SelfCheckFragment: Fragment() {
    private var _binding: FragmentSelfCheckBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelfCheckBinding.inflate(inflater, container, false)

        binding.selfCheckSendButton.setOnClickListener() {
            val inputText = binding.selfCheckInput.text.toString()
            if (inputText.isNotBlank()) {
                val inflater = LayoutInflater.from(requireContext())
                val messageView = inflater.inflate(R.layout.item_message, binding.messageList, false)

                val textView = messageView.findViewById<TextView>(R.id.messageText)
                textView.text = inputText

                binding.messageList.addView(messageView)
                binding.selfCheckInput.text?.clear()
            }

            binding.selfCheckScrollView.post {
                binding.selfCheckScrollView.fullScroll(View.FOCUS_DOWN)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }
}