package com.example.carepick.ui.location

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.R
import com.example.carepick.adapter.LocationAdapter
import com.example.carepick.databinding.FragmentLocationSettingBinding
import kotlinx.coroutines.launch

class LocationSettingFragment : Fragment(R.layout.fragment_location_setting) {

    private var _binding: FragmentLocationSettingBinding? = null
    private val binding get() = _binding!!

    private val sidoAdapter by lazy {
        SidoAdapter { sido ->
            // TODO: 클릭 시 동작 (예: 다음 단계로 시/군/구 불러오기)
            // showToast("${sido.name} 선택")
            // 이후 시/군/구 API 호출/그리드 표시로 확장
        }
    }

    private val viewModel by lazy { LocationViewModel(restKey = "4bde7a7235a24839479023ff8eb22347") }
    private val adapter = LocationAdapter { doc ->
        // 클릭 시 좌표/주소 사용 예시
        val lat = doc.y?.toDoubleOrNull()
        val lon = doc.x?.toDoubleOrNull()
        val road = doc.road_address?.address_name
        val jibun = doc.address?.address_name ?: doc.address_name
        // TODO: 선택 결과 처리 (예: 상세 페이지 이동, 지도 표시, 폼에 채우기 등)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLocationSettingBinding.bind(view)

        // 뒤로가기 버튼
        binding.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Recycler
        binding.recycler.adapter = adapter
        if (binding.recycler.layoutManager == null) {
            binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        }
        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                val last = lm.findLastVisibleItemPosition()
                if (last >= adapter.itemCount - 4) {
                    viewModel.loadMore()
                }
            }
        })

        // IME 액션 리스너
        binding.searchEdit.setOnEditorActionListener { v, actionId, event ->
            Log.d("LocationSetting", "onEditorAction actionId=$actionId, event=$event")
            val isImeSearch = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterDown = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

            if (isImeSearch || isEnterDown) {
                val q = v.text?.toString().orEmpty()
                Log.d("LocationSetting", "Search triggered with query='$q'")
                viewModel.search(q, resetPage = true)
                true
            } else {
                false
            }
        }

        // 스크롤 페이징
        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                val last = lm.findLastVisibleItemPosition()
                if (last >= adapter.itemCount - 4) {
                    Log.d("LocationSetting", "loadMore() triggered at last=$last")
                    viewModel.loadMore()
                }
            }
        })

        // 상태 수집 (주석 해제 권장: 그래야 로딩/에러/리스트 업데이트 확인 가능)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.ui.collect { state ->
                Log.d("LocationSetting", "state loading=${state.loading} items=${state.items.size} error=${state.error}")
                // binding.progress.isVisible = state.loading  // progress 뷰가 있으면 활성화
                // binding.txtError.isVisible = state.error != null
                // binding.txtError.text = state.error
                adapter.submit(state.items)
                // binding.emptyView.isVisible = !state.loading && state.items.isEmpty() && state.error == null
            }
        }

        binding.rvSidoGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = sidoAdapter
            setHasFixedSize(true)
        }

        // 버튼 토글 + 첫 호출
        binding.btnAdminRegion.setOnClickListener {
            if (binding.rvSidoGrid.visibility == View.VISIBLE) {
                binding.rvSidoGrid.visibility = View.GONE
            } else {
                binding.rvSidoGrid.visibility = View.VISIBLE
                if (sidoAdapter.itemCount == 0) {
                    fetchSidos()
                }
            }
        }

//        // 검색 버튼 (바인딩 통해 접근!)
//        binding.btnSearch.setOnClickListener {
//            val q = binding.searchEdit.text?.toString().orEmpty()
//            viewModel.search(q, resetPage = true)
//        }

//        // 상태 구독 (오타 수정 + viewLifecycleOwner 사용)
//        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
//            viewModel.ui.collect { state ->
//                binding.progress.isVisible = state.loading
//                binding.txtError.isVisible = state.error != null
//                binding.txtError.text = state.error
//                adapter.submit(state.items)
//                binding.emptyView.isVisible = !state.loading && state.items.isEmpty() && state.error == null
//            }
//        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun fetchSidos() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                RetrofitProvider.api.getSidos(page = 0, size = 30)
            }.onSuccess { page ->
                sidoAdapter.submit(page.content)
            }.onFailure { e ->
                // TODO: 에러 처리 (스낵바/토스트 등)
                // showError(e.message)
            }
        }
    }

    private fun createSelectableButton(text: String): TextView {
        val textView = TextView(requireContext())
        textView.text = text
        textView.setPadding(32, 20, 32, 20)
        textView.setBackgroundResource(R.drawable.chip_unselected)
        textView.setTextColor(Color.BLACK)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(12, 12, 12, 12)
        textView.layoutParams = params

        return textView
    }
}