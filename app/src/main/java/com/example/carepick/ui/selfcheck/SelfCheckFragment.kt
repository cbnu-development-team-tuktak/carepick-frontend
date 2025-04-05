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
            }
        }

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            // 키보드가 올라온 경우
            if (keypadHeight > screenHeight * 0.15) {
                // 키보드가 보이는 동안 가장 마지막 메시지로 스크롤
                if (messages.isNotEmpty()) {
                    binding.messageRecyclerView.post {
                        binding.messageRecyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}