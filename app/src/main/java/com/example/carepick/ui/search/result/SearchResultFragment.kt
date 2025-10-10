package com.example.carepick.ui.search.result

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.MainActivity
import com.example.carepick.R
import com.example.carepick.common.adapter.AutoCompleteAdapter
import com.example.carepick.common.ui.DoctorDetailFragment
import com.example.carepick.ui.hospital.HospitalDetailFragment
import com.example.carepick.data.model.DoctorDetailsResponse
import com.example.carepick.data.model.HospitalDetailsResponse
import com.example.carepick.data.model.SearchResultItem
import com.example.carepick.data.repository.DoctorRepository
import com.example.carepick.databinding.FragmentSearchResultBinding
import com.example.carepick.data.repository.HospitalRepository
import com.example.carepick.ui.location.repository.UserLocation
import com.example.carepick.ui.location.viewModel.UserLocationViewModel
import com.example.carepick.ui.location.viewModelFactory.UserLocationViewModelFactory
import com.example.carepick.ui.search.FilterViewModel
import com.example.carepick.ui.search.filter.FilterFragment
import com.example.carepick.ui.search.filter.SortFilterBottomSheetFragment
import com.example.carepick.ui.search.result.adapter.SearchResultListAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class SearchResultFragment : Fragment() {

    // fragment_search_result.xml을 사용할 것임을 명시하였다
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    // ✅ ViewModel 인스턴스 생성
    private val viewModel: SearchResultViewModel by viewModels { SearchResultViewModelFactory() }
    private val userLocationVM: UserLocationViewModel by activityViewModels {
        UserLocationViewModelFactory(requireContext().applicationContext)
    }
    private val filterVM: FilterViewModel by activityViewModels()

    // 프래그먼트가 생성되었을 때 실행할 코드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 프래그먼트가 생성되고 위젯들이 배치된 후 실행할 코드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWindowInsets()
        setupListeners()
        observeUiState() // ✅ UI 상태를 구독하는 함수
        loadInitialData() // ✅ 초기 데이터 로딩 함수
    }

    private fun loadInitialData() {
        val query = arguments?.getString("search_query")
        binding.searchResultSearchView.setText(query) // 검색창에 이전 검색어 설정

        if (query.isNullOrBlank()) {
            // 위치 기반 검색 시작
            lifecycleScope.launch {
                showLoading()
                try {
                    withTimeout(5000L) {
                        val location = userLocationVM.location.first { it != null }
                        viewModel.searchByLocation(location!!)
                    }
                } catch (e: TimeoutCancellationException) {
                    showError(getString(R.string.need_location_message))
                }
            }
        } else {
            // 키워드 기반 검색 시작
            viewModel.searchByKeyword(query)
        }
    }

    // ✅ ViewModel의 상태 변화를 감지하고 UI 업데이트
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SearchResultUiState.Loading -> showLoading()
                    is SearchResultUiState.Success -> showContent(state.items)
                    is SearchResultUiState.Error -> showError(state.message)
                }
            }
        }
    }

    // ✨ 1. 리스너 설정 로직 분리
    private fun setupListeners() {
        // 병원/의사 토글 버튼 리스너
        binding.searchResultHospitalFilterButton.setOnClickListener {
            handleSearchModeChange(SearchMode.HOSPITAL)
        }
        binding.searchResultDoctorFilterButton.setOnClickListener {
            handleSearchModeChange(SearchMode.DOCTOR)
        }

        // 검색창 키보드의 '검색' 버튼 리스너
        binding.searchResultSearchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val newQuery = binding.searchResultSearchView.text.toString()
                if (newQuery.isNotBlank()) {
                    hideKeyboard()
                    viewModel.invalidateCache() // ✅ 새로운 검색이므로 캐시 초기화
                    viewModel.searchByKeyword(newQuery)
                }
                return@setOnEditorActionListener true
            }
            false
        }

        // 필터 화면 결과 리스너
        parentFragmentManager.setFragmentResultListener("filter_apply_request", viewLifecycleOwner) { _, bundle ->
            val receivedSpecialties = bundle.getStringArrayList("selected_specialties")?.toSet() ?: emptySet()
            filterVM.updateSpecialties(receivedSpecialties) // 공유 ViewModel 상태 업데이트

            val currentLocation = userLocationVM.location.value
            if (currentLocation != null) {
                viewModel.invalidateCache() // ✅ 새로운 필터가 적용되므로 캐시 초기화
                viewModel.searchByLocation(currentLocation, receivedSpecialties.toList())
            } else {
                Toast.makeText(requireContext(), "위치 정보가 없어 필터를 적용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 정렬 BottomSheet 결과 리스너 (기능 확장 필요)
        parentFragmentManager.setFragmentResultListener("sort_filter_result", viewLifecycleOwner) { _, bundle ->
            // TODO: 정렬 로직 ViewModel로 이동 및 구현
        }

        // 필터/정렬 버튼 클릭 리스너
        binding.searchResultFilterButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.fragment_container, FilterFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.searchResultSortButton.setOnClickListener {
            SortFilterBottomSheetFragment().show(parentFragmentManager, "SortFilter")
        }
    }

    private fun handleSearchModeChange(newMode: SearchMode) {
        if (viewModel.currentSearchMode == newMode) return // 이미 선택된 모드이면 아무것도 안 함

        updateToggleUI(newMode)
        viewModel.changeSearchMode(newMode)
        // 모드 변경 후, 현재 조건(검색어/위치)에 맞춰 데이터 다시 로드
        loadInitialData()
    }

    private fun updateToggleUI(mode: SearchMode) {
        if (mode == SearchMode.HOSPITAL) {
            binding.searchResultHospitalFilterButton.setBackgroundResource(R.drawable.bg_search_result_filter_left_selected)
            binding.searchResultDoctorFilterButton.setBackgroundResource(R.drawable.bg_search_result_filter_right)
        } else {
            binding.searchResultDoctorFilterButton.setBackgroundResource(R.drawable.bg_search_result_filter_right_selected)
            binding.searchResultHospitalFilterButton.setBackgroundResource(R.drawable.bg_search_result_filter_left)
        }
    }

    // ✨ 5. UI 상태 변경 함수들 분리
    private fun showLoading() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.searchResultRecyclerView.visibility = View.GONE
        binding.searchResultErrorText.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.searchResultRecyclerView.visibility = View.GONE
        binding.searchResultErrorText.visibility = View.VISIBLE
        binding.searchResultErrorText.text = message
    }

    private fun showContent(items: List<SearchResultItem>) { // 👈 HospitalDetailsResponse에서 SearchResultItem으로 변경
        binding.loadingIndicator.visibility = View.GONE
        binding.searchResultRecyclerView.visibility = View.VISIBLE
        binding.searchResultErrorText.visibility = View.GONE
        setupRecyclerView(items)
    }

    // RecyclerView 설정
    private fun setupRecyclerView(items: List<SearchResultItem>) {
        val myAdapter = SearchResultListAdapter(items, requireActivity()) { item ->
            when (item) {
                is HospitalDetailsResponse -> {
                    val detailFragment = HospitalDetailFragment()
                    detailFragment.arguments = Bundle().apply { putString("hospitalId", item.id) }
                    parentFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit()
                }
                is DoctorDetailsResponse -> {
                    val detailFragment = DoctorDetailFragment()
                    detailFragment.arguments = Bundle().apply { putString("doctorId", item.id) }
                    parentFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
        binding.searchResultRecyclerView.adapter = myAdapter
        binding.searchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
    }

    private fun hideKeyboard() {
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        // MainActivity에 아이콘 상태 업데이트 및 현재 프래그먼트가 활성 상태임을 알림
        (activity as? MainActivity)?.let { mainActivity ->
            mainActivity.updateNavIcons(R.id.nav_search)
            mainActivity.updateActiveFragment(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}