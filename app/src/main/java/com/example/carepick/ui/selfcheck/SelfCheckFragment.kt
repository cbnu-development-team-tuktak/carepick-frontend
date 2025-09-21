package com.example.carepick.ui.selfcheck

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.R
import com.example.carepick.adapter.MessageAdapter
import com.example.carepick.databinding.FragmentSelfCheckBinding
import com.example.carepick.model.DiagnosisResult
import kotlinx.coroutines.launch

class SelfCheckFragment : Fragment() {
    private var _binding: FragmentSelfCheckBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<String>()
    private lateinit var adapter: MessageAdapter

    private val vm: SelfDiagnosisViewModel by viewModels()
    private var hasScrolledOnKeyboardOpen = false

    private val TAG = "SelfCheckFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelfCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated()")

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { rootView, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = 80.dpToPx(requireContext())

            rootView.setPadding(0, statusBarHeight, 0, 0)

            val bottomPadding = if (imeHeight > 0) imeHeight - navBarHeight else 0
            binding.inputContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = bottomPadding.coerceAtLeast(0)
            }
            insets
        }

        adapter = MessageAdapter(messages)
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.messageRecyclerView.adapter = adapter

        binding.selfCheckInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                sendRequest(binding.selfCheckInput.text?.toString().orEmpty())
                true
            } else false
        }

        binding.selfCheckSendButton.setOnClickListener {
            sendRequest(binding.selfCheckInput.text?.toString().orEmpty())
        }

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val safeBinding = _binding ?: return@addOnGlobalLayoutListener
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

        val backButton = view.findViewById<ImageButton>(R.id.btn_back)
        backButton.setOnClickListener {
            val manager = requireActivity().supportFragmentManager
            if (manager.backStackEntryCount > 0) manager.popBackStack() else requireActivity().finish()
        }

        // ✅ 상태 수집 로그
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { state ->
                    Log.d(TAG, "state=$state")
                    when (state) {
                        is UiState.Idle -> Unit
                        is UiState.Loading -> binding.selfCheckSendButton.isEnabled = false
                        is UiState.Success -> {
                            binding.selfCheckSendButton.isEnabled = true
                            val pretty = formatPredictions(state.result)
                            messages.add(pretty)
                            adapter.notifyItemInserted(messages.size - 1)
                            binding.messageRecyclerView.scrollToPosition(messages.size - 1)
                        }
                        is UiState.Error -> {
                            binding.selfCheckSendButton.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun sendRequest(raw: String) {
        val text = raw.trim()
        Log.d(TAG, "sendRequest() raw='$raw' trimmed='$text'")
        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "증상을 입력해 주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 내 메시지 먼저 추가
        messages.add(text)
        adapter.notifyItemInserted(messages.size - 1)
        binding.messageRecyclerView.scrollToPosition(messages.size - 1)

        // 입력 초기화 & 키보드 내림
        binding.selfCheckInput.text?.clear()
        hideKeyboard()
        hasScrolledOnKeyboardOpen = false

        // 실제 호출
        vm.requestDiagnosis(text, k = 3)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let { v -> imm.hideSoftInputFromWindow(v.windowToken, 0) }
    }

    private fun formatPredictions(result: DiagnosisResult): String {
        if (result.predictions.isEmpty()) return "진단 결과가 비어있어요."
        val sb = StringBuilder("예측 결과 Top-${result.predictions.size}\n")
        result.predictions.forEachIndexed { idx, p ->
            val prob = String.format("%.1f%%", p.probability * 100)
            val dept = p.department?.let { " / 과: $it" } ?: ""
            sb.append("${idx + 1}. ${p.disease} ($prob)$dept\n")
        }
        return sb.toString().trimEnd()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
}
