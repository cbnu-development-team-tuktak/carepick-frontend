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
import com.example.carepick.model.ChatMessage
import com.example.carepick.model.DiagnosisResult
import kotlinx.coroutines.launch

class SelfCheckFragment : Fragment() {
    private var _binding: FragmentSelfCheckBinding? = null
    private val binding get() = _binding!!

    // messages 타입은 sealed class 리스트
    private val messages = mutableListOf<ChatMessage>()
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

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
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
                            // ✅ 타이핑 제거
                            adapter.removeLastIfTyping()

                            // ✅ 서버 message에서 Top-3 파싱 → 봇 메시지 추가
                            val botText = buildTop3Text(state.result)
                            adapter.add(ChatMessage.Bot(botText))
                            binding.messageRecyclerView.scrollToPosition(messages.size - 1)
                        }
                        is UiState.Error -> {
                            binding.selfCheckSendButton.isEnabled = true
                            // ✅ 타이핑 제거
                            adapter.removeLastIfTyping()

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
        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "증상을 입력해 주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 1) 내 메시지(오른쪽)
        adapter.add(ChatMessage.User(text))
        binding.messageRecyclerView.scrollToPosition(messages.size - 1)

        // 2) 입력창 정리 + 키보드 내림
        binding.selfCheckInput.text?.clear()
        hideKeyboard()
        hasScrolledOnKeyboardOpen = false

        // 3) 타이핑 말풍선(왼쪽) 추가
        adapter.add(ChatMessage.Typing)
        binding.messageRecyclerView.scrollToPosition(messages.size - 1)

        // 4) 호출
        vm.requestDiagnosis(text, k = 3)
    }



    // ✅ Top-3만 추출해서 보기 좋게 렌더링
    private fun buildTop3Text(result: DiagnosisResult): String {
        // 1) 백엔드 message에서 라인 파싱
        val msg = result.message.orEmpty()
        val extracted = extractTopKFromMessage(msg) // ["알레르기 비염 (0.1106)", "감기 (0.069)", "비염 (0.0626)"]

        // 2) 없으면 message 전체 출력(보호)
        if (extracted.isEmpty()) return msg.ifBlank { "예측 결과가 비어있어요." }

        val sb = StringBuilder()
        sb.appendLine("예측 결과 Top-3")
        extracted.forEachIndexed { i, line ->
            sb.appendLine("${i + 1}. $line")
        }
        // (선택) 진료과도 하단에 붙임
        if (result.suggestedSpecialties.isNotEmpty()) {
            sb.appendLine().append("권장 진료과: ").append(result.suggestedSpecialties.joinToString(", "))
        }
        return sb.toString().trimEnd()
    }


    // ✅ “- XXX (score)” 형태 라인만 추출
    private fun extractTopKFromMessage(message: String): List<String> {
        // "예측된 질병 Top-3:" 이후의 줄 중 "- "로 시작하는 라인만 추출
        val lines = message.lines()
        val startIdx = lines.indexOfFirst { it.contains("예측된 질병 Top-") }
        if (startIdx == -1) return emptyList()

        val result = mutableListOf<String>()
        for (i in (startIdx + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("- ")) {
                result.add(line.removePrefix("- ").trim())
            } else if (line.isNotBlank() && !line.startsWith("- ")) {
                // 리스트 영역이 끝난 것으로 간주
                break
            }
        }
        return result
    }



    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let { v -> imm.hideSoftInputFromWindow(v.windowToken, 0) }
    }



//    private fun formatPredictions(result: DiagnosisResult): String {
//        if (result.predictions.isEmpty()) return "진단 결과가 비어있어요."
//        val sb = StringBuilder("예측 결과 Top-${result.predictions.size}\n")
//        result.predictions.forEachIndexed { idx, p ->
//            val prob = String.format("%.1f%%", p.probability * 100)
//            val dept = p.department?.let { " / 과: $it" } ?: ""
//            sb.append("${idx + 1}. ${p.disease} ($prob)$dept\n")
//        }
//        return sb.toString().trimEnd()
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
}
