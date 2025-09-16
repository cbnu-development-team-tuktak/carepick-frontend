package com.example.carepick.ui.location

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.R
import com.example.carepick.adapter.LocationAdapter
import com.example.carepick.databinding.FragmentLocationSettingBinding
import com.example.carepick.network.RetrofitClient
import kotlinx.coroutines.launch
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult


private const val KEY_SELECTED_ADDRESS = "key_selected_address"
private const val ARG_ADDRESS = "address"


class LocationSettingFragment : Fragment(R.layout.fragment_location_setting) {

    private var _binding: FragmentLocationSettingBinding? = null
    private val binding get() = _binding!!

    private var selectedSido: String? = null
    private var selectedSgg:  String? = null
    private var selectedUmd:  String? = null


    private val sidoAdapter by lazy {
        SidoAdapter { sido ->
            selectedSido = sido.name
            Log.d(TAG, "Sido clicked: ${sido.name} (${sido.type})")
            fetchSggs(sido.name)
        }
    }



    private val sggAdapter by lazy {
        SggAdapter(
            currentSido = { selectedSido ?: "시/도" },
            onBackClick = {
                // SGG → SIDO
                binding.rvSggGrid.visibility = View.GONE
                binding.rvSidoGrid.visibility = View.VISIBLE
            },
            onItemClick = { sgg ->
                selectedSgg = sgg.name
                fetchUmds(sgg.name)
            }
        )
    }

    private val umdAdapter by lazy {
        UmdAdapter(
            currentSgg = { selectedSgg ?: "시/군/구" },
            onBackClick = {
                // UMD → SGG
                binding.rvUmdGrid.visibility = View.GONE
                binding.rvSggGrid.visibility = View.VISIBLE
            },
            onItemClick = { umd ->
                selectedUmd = umd.name
                showConfirmation()
            }
        )
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


        binding.btnConfirmSelection.setOnClickListener {
            val addr = buildAddress()
            Log.d("ResultFlow", "POST addr=$addr (LocationSetting)")

            val fm = requireActivity().supportFragmentManager
            Log.d("ResultFlow", "sender fm=$fm backStack=${fm.backStackEntryCount}")

            // 1) 결과 먼저 세팅
            val bundle = android.os.Bundle().apply { putString(ARG_ADDRESS, addr) }
            fm.setFragmentResult(KEY_SELECTED_ADDRESS, bundle)
            Log.d("ResultFlow", "POST setFragmentResult done")

            // 2) 그 다음 pop (이 순서 중요!)
            fm.popBackStack()
            // onBackPressedDispatcher는 사용하지 말고 pop만 사용
        }



        binding.rvSidoGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = sidoAdapter
            setHasFixedSize(true)
        }

        // 시/군/구 그리드 준비
        binding.rvSggGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = sggAdapter
            setHasFixedSize(true)
        }

        binding.rvUmdGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = umdAdapter
            setHasFixedSize(true)
        }

        // 취소/확인 리스너
        binding.btnCancelSelection.setOnClickListener {
            // 요구사항: 취소 시 시/도 그리드로 복귀
            selectedUmd = null
            binding.confirmBar.visibility = View.GONE
            binding.rvUmdGrid.visibility = View.GONE
            binding.rvSggGrid.visibility = View.GONE
            binding.rvSidoGrid.visibility = View.VISIBLE
        }


        // 버튼 토글 + 첫 호출
        binding.btnAdminRegion.setOnClickListener {
            Log.d(TAG, "행정구역 버튼 클릭")

            val nowVisible = binding.rvSidoGrid.isVisible
            binding.rvSidoGrid.visibility = if (nowVisible) View.GONE else View.VISIBLE
            Log.d(TAG, "rvSidoGrid visibility = ${binding.rvSidoGrid.visibility}")

            if (!nowVisible && sidoAdapter.itemCount == 0) {
                fetchSidos()   // 처음 펼칠 때만 로드
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
        Log.d(TAG, "fetchSidos() 호출 - 서버로 요청 시작")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = RetrofitClient.adminRegionService.getSidos(0, 30)
                Log.d("RetrofitTest", "총 ${res.content.size}개 받아옴")

                res.content.forEachIndexed { i, s ->
                    Log.d(TAG, "[$i] name=${s.name}, type=${s.type}")
                }

                sidoAdapter.submit(res.content)
                Log.d(TAG, "어댑터에 submit 완료")
            } catch (e: Exception) {
                Log.e("RetrofitTest", "요청 실패: ${e.message}", e)
            }
        }
    }

    private fun fetchSggs(sidoName: String) {
        // ... 기존 구현 그대로 (SGG 표시)
        binding.rvSidoGrid.visibility = View.GONE
        binding.rvUmdGrid.visibility = View.GONE
        binding.confirmBar.visibility = View.GONE
        binding.rvSggGrid.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                RetrofitClient.adminRegionService.getSggsBySido(sidoName, 0, 100)
            }.onSuccess { res ->
                sggAdapter.submit(res.content)
            }.onFailure { e ->
                Log.e("LocationSetting", "SGG 조회 실패: ${e.message}", e)
                binding.rvSggGrid.visibility = View.GONE
                binding.rvSidoGrid.visibility = View.VISIBLE
            }
        }
    }

    private fun fetchUmds(sggName: String) {
        Log.d("LocationSetting", "fetchUmds('$sggName')")
        binding.rvSggGrid.visibility = View.GONE
        binding.confirmBar.visibility = View.GONE
        binding.rvUmdGrid.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                RetrofitClient.adminRegionService.getUmdsBySgg(sggName, 0, 100)
            }.onSuccess { res ->
                umdAdapter.submit(res.content)
            }.onFailure { e ->
                Log.e("LocationSetting", "UMD 조회 실패: ${e.message}", e)
                binding.rvUmdGrid.visibility = View.GONE
                binding.rvSggGrid.visibility = View.VISIBLE
            }
        }
    }

    private fun showConfirmation() {
        // 모든 그리드 감추고, 확인 바만 보여주기
        binding.rvSidoGrid.visibility = View.GONE
        binding.rvSggGrid.visibility = View.GONE
        binding.rvUmdGrid.visibility = View.GONE

        binding.tvSelectedAddress.text = buildAddress()
        binding.confirmBar.visibility = View.VISIBLE
    }

    private fun buildAddress(): String {
        val a = listOfNotNull(selectedSido, selectedSgg, selectedUmd)
        return a.joinToString(" ")
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