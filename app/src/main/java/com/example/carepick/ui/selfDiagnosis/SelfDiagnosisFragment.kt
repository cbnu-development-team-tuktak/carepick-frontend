package com.example.carepick.ui.selfDiagnosis

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.MainActivity
import com.example.carepick.R
import com.example.carepick.TabOwner
import com.example.carepick.ui.selfDiagnosis.adapter.MessageAdapter
import com.example.carepick.databinding.FragmentSelfDiagnosisBinding
import com.example.carepick.ui.selfDiagnosis.model.ChatMessage
import kotlinx.coroutines.launch

class SelfDiagnosisFragment : Fragment(), TabOwner {
    private var _binding: FragmentSelfDiagnosisBinding? = null
    private val binding get() = _binding!!

    private val chatMessageList = mutableListOf<ChatMessage>()
    private lateinit var messageAdapter: MessageAdapter

    private val vm: SelfDiagnosisViewModel by activityViewModels { SelfDiagnosisViewModelFactory() }
    private var hasScrolledOnKeyboardOpen = false
    private val TAG = "SelfCheckFragment"

    override fun getNavId(): Int = R.id.nav_self_diagnosis

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelfDiagnosisBinding.inflate(inflater, container, false)
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

        // ✅ Initialize adapter with an empty list.
        messageAdapter = MessageAdapter(chatMessageList) { specialty ->
            // 이 코드는 MessageAdapter의 ButtonsVH에서 버튼이 클릭될 때 실행됩니다.
            navigateToHospitalSearch(specialty)
        }
        binding.messageRecyclerView.adapter = messageAdapter
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(requireContext())



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
            val safe = _binding ?: return@addOnGlobalLayoutListener
            val rect = Rect()
            safe.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = safe.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * 0.15) {
                // ✅ ViewModel의 StateFlow에서 현재 값(.value)을 직접 가져옵니다.
                if (vm.messages.value.isNotEmpty() && !hasScrolledOnKeyboardOpen) {
                    safe.messageRecyclerView.post {
                        // ✅ 여기도 동일하게 수정합니다.
                        safe.messageRecyclerView.scrollToPosition(vm.messages.value.size - 1)
                        hasScrolledOnKeyboardOpen = true
                    }
                }
            } else hasScrolledOnKeyboardOpen = false
        }

        val backButton = view.findViewById<ImageButton>(R.id.btn_back)
        backButton.setOnClickListener {
            (requireActivity() as? MainActivity)?.navigateToTab(R.id.nav_home)
        }

        // ✅ ViewModel의 상태(로딩/에러) 수집
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { state ->
                    Log.d(TAG, "state=$state")
                    // 로딩 중일 때만 버튼 비활성화
                    binding.selfCheckSendButton.isEnabled = state !is UiState.Loading
                    if (state is UiState.Error) {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // ✅ ViewModel의 메시지 리스트를 구독하여 어댑터에 반영
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.messages.collect { messageList ->
                    messageAdapter.updateMessages(messageList) // 어댑터에 새 리스트 전달
                    if (messageList.isNotEmpty()) {
                        binding.messageRecyclerView.post {
                            binding.messageRecyclerView.scrollToPosition(messageList.size - 1)
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

        // 1. Clear input and hide keyboard
        binding.selfCheckInput.text?.clear()
        hideKeyboard()
        hasScrolledOnKeyboardOpen = false

        // 2. ✅ Delegate the entire process to the ViewModel
        vm.sendMessage(text, k = 3)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let { v -> imm.hideSoftInputFromWindow(v.windowToken, 0) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

    private fun navigateToHospitalSearch(specialty: String) {
        // 1. 전달할 데이터를 Bundle에 담습니다.
        //    FilterFragment와 동일한 키("selected_specialties")를 사용합니다.
        val resultBundle = Bundle().apply {
            // 단일 진료과를 ArrayList에 담아서 전달
            putStringArrayList("selected_specialties", arrayListOf(specialty))
        }

        // 2. ✅ 'filter_apply_request' 라는 키로 결과를 미리 설정합니다.
        //    SearchResultFragment가 이 결과를 받게 됩니다.
        parentFragmentManager.setFragmentResult("filter_apply_request", resultBundle)

        // 3. ✅ 그 다음에 '검색' 탭으로 이동만 합니다. (Bundle 전달은 이제 필요 없음)
        (activity as? MainActivity)?.navigateToTab(R.id.nav_search)

        // Toast 메시지는 그대로 유지하거나 필요에 따라 수정
        Toast.makeText(requireContext(), "'주변의 ${specialty}' 병원을 검색합니다.", Toast.LENGTH_SHORT).show()
    }
}
