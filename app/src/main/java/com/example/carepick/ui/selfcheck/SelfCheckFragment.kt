package com.example.carepick.ui.selfcheck

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.adapter.MessageAdapter
import com.example.carepick.databinding.FragmentSelfCheckBinding

// 아직 구현을 시작하지 않음
class SelfCheckFragment : Fragment() {
    private var _binding: FragmentSelfCheckBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<String>()
    private lateinit var adapter: MessageAdapter

    private var hasScrolledOnKeyboardOpen = false // ✨ 키보드 올라왔을 때 한 번만 스크롤

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelfCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = MessageAdapter(messages)
        binding.messageRecyclerView.adapter = adapter
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.selfCheckSendButton.setOnClickListener {
            val text = binding.selfCheckInput.text.toString()
            if (text.isNotBlank()) {
                messages.add(text)
                adapter.notifyItemInserted(messages.size - 1)
                binding.messageRecyclerView.scrollToPosition(messages.size - 1)
                binding.selfCheckInput.text?.clear()
                hasScrolledOnKeyboardOpen = false // ✨ 새 메시지를 보냈으면 다음 키보드 열림에 다시 스크롤
            }
        }

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                if (messages.isNotEmpty() && !hasScrolledOnKeyboardOpen) {
                    binding.messageRecyclerView.post {
                        binding.messageRecyclerView.scrollToPosition(messages.size - 1)
                        hasScrolledOnKeyboardOpen = true // ✨ 한 번만 스크롤되게
                    }
                }
            } else {
                hasScrolledOnKeyboardOpen = false // 키보드 내려가면 초기화
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}