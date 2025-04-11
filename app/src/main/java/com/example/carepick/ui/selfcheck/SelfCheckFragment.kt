package com.example.carepick.ui.selfcheck

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.R
import com.example.carepick.adapter.MessageAdapter
import com.example.carepick.databinding.FragmentSelfCheckBinding

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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = 80.dpToPx(requireContext()) // nav_bar 높이 (단위: px)

            view.setPadding(0, statusBarHeight, 0, 0) // 상단 padding만 수동 적용

            // 키보드가 올라왔을 때만 nav_bar를 무시하고 딱 붙도록
            val bottomPadding = if (imeHeight > 0) imeHeight - navBarHeight else 0
            binding.inputContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = bottomPadding.coerceAtLeast(0)
            }

            insets
        }

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
            val safeBinding = _binding ?: return@addOnGlobalLayoutListener // 🔒 Null check

            val rect = Rect()
            safeBinding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = safeBinding.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                if (messages.isNotEmpty() && !hasScrolledOnKeyboardOpen) {
                    safeBinding.messageRecyclerView.post {
                        safeBinding.messageRecyclerView.scrollToPosition(messages.size - 1)
                        hasScrolledOnKeyboardOpen = true
                    }
                }
            } else {
                hasScrolledOnKeyboardOpen = false
            }
        }

        // include된 헤더 내의 뒤로가기 버튼
        val backButton = view.findViewById<ImageButton>(R.id.btn_back)
        backButton.setOnClickListener {
            val manager = requireActivity().supportFragmentManager
            if (manager.backStackEntryCount > 0) {
                manager.popBackStack()
            } else {
                requireActivity().finish() // or moveTaskToBack(true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}