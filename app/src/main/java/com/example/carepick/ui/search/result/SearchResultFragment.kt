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
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.MainActivity
import com.example.carepick.R
import com.example.carepick.TabOwner
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

class SearchResultFragment : Fragment(), TabOwner {

    // fragment_search_result.xml을 사용할 것임을 명시하였다
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    // ✅ ViewModel 인스턴스 생성
    private val viewModel: SearchResultViewModel by viewModels { SearchResultViewModelFactory() }
    private val userLocationVM: UserLocationViewModel by activityViewModels {
        UserLocationViewModelFactory(requireContext().applicationContext)
    }
    private val filterVM: FilterViewModel by activityViewModels()

    // 자신이 '검색' 탭에 속한다고 알려줍니다.
    override fun getNavId(): Int = R.id.nav_search




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

        // ✅ RecyclerView에 어댑터와 레이아웃 매니저를 한 번만 설정합니다.
        binding.searchResultRecyclerView.adapter = searchResultAdapter
        binding.searchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ✅ 스크롤 리스너도 여기서 한 번만 설정합니다.
        binding.searchResultRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (totalItemCount > 0 && lastVisibleItemPosition >= totalItemCount - 5) {
                    viewModel.loadNextPage()
                }
            }
        })

        setupWindowInsets()
        setupListeners()
        observeUiState() // ✅ UI 상태를 구독하는 함수
    }





    // ✅ [추가] 프래그먼트가 보여지거나 숨겨질 때 호출되는 콜백
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            // 프래그먼트가 다시 화면에 나타날 때, 초기 데이터 로딩을 시작합니다.
            loadInitialData()
        }
    }


    private fun loadInitialData() {

        // arguments가 없으면(예: 그냥 탭만 눌러서 들어온 경우) 아무것도 하지 않고 현재 상태를 유지할 수 있습니다.
        // 또는 기본 로딩을 할 수도 있습니다. 여기서는 arguments가 있을 때만 동작하도록 합니다.
        val args = arguments ?: return

        // 1. arguments에서 모든 초기 조건을 한 번에 읽어옵니다.
        val query = args.getString("search_query")
        val initialModeString = args.getString("initial_search_mode")
        val initialSpecialty = args.getString("initial_specialty_filter")

        // 2. 초기 모드를 결정하고 UI와 ViewModel 상태를 업데이트합니다.
        val mode = if (initialModeString == "DOCTOR") SearchMode.DOCTOR else SearchMode.HOSPITAL
        viewModel.changeSearchMode(mode)
        updateToggleUI(mode)

        // 3. 자가진단에서 넘어온 진료과가 있다면, 공유 FilterViewModel의 상태도 동기화합니다.
        //    이렇게 해야 나중에 필터 화면을 열었을 때 해당 진료과가 선택된 상태로 보입니다.
        if (initialSpecialty != null) {
            filterVM.updateSpecialties(setOf(initialSpecialty))
        }

        // 4. 검색창 텍스트를 설정합니다.
        binding.searchResultSearchView.setText(query)

        // 5. 최종 조건에 따라 ViewModel에 데이터 로딩을 '한 번만' 요청합니다.
        // 키워드 검색이 최우선입니다.
        if (!query.isNullOrBlank()) {
            lifecycleScope.launch {
                viewModel.searchByKeyword(query)
            }
        } else {
            // 키워드가 없으면 위치 기반 검색을 합니다.
            lifecycleScope.launch {
                showLoading()
                try {
                    withTimeout(5000L) {
                        val location = userLocationVM.location.first { it != null }
                        // 자가진단에서 받은 진료과(initialSpecialty)가 있으면 필터 조건으로 사용합니다.
                        val specialties = if (initialSpecialty != null) listOf(initialSpecialty) else null
                        viewModel.searchByLocation(location!!, specialties = specialties)
                    }
                } catch (e: TimeoutCancellationException) {
                    showError(getString(R.string.need_location_message))
                }
            }
        }

        // 6. 모든 처리가 끝났으므로 arguments를 비워 다음 호출에 영향을 주지 않도록 합니다.
        arguments = null
    }

    // ✅ 변경된 생성자에 맞춰 수정
    private val searchResultAdapter by lazy {
        SearchResultListAdapter(requireActivity()) { item ->
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
    }

    // ✅ ViewModel의 상태 변화를 감지하고 UI 업데이트
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // ✅ 어댑터의 로딩 상태를 UI 상태에 맞춰 제어
                searchResultAdapter.setLoading(state is SearchResultUiState.LoadingNextPage)

                when (state) {
                    is SearchResultUiState.Loading -> showLoading()
                    is SearchResultUiState.Success -> showContent(state.items)
                    is SearchResultUiState.Error -> showError(state.message)
                    is SearchResultUiState.LoadingNextPage -> {
                        // 다음 페이지 로딩 중일 때는 기존 목록을 그대로 보여줍니다.
                        // 어댑터가 알아서 맨 아래에 로딩 스피너를 추가할 것입니다.
                        binding.searchResultRecyclerView.visibility = View.VISIBLE
                        searchResultAdapter.submitList(state.items)
                    }
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
                    // ✅ lifecycleScope.launch로 코루틴을 시작하고, 그 안에서 suspend 함수 호출
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.invalidateCache()
                        viewModel.searchByKeyword(newQuery)
                    }
                }
                return@setOnEditorActionListener true
            }
            false
        }

        // 필터 화면 결과 리스너
        parentFragmentManager.setFragmentResultListener("filter_apply_request", viewLifecycleOwner) { _, bundle ->

            // ✅ [추가] Bundle에 검색 모드 정보가 있는지 먼저 확인합니다.
            val modeString = bundle.getString("initial_search_mode")
            if (modeString == "HOSPITAL") {
                // ViewModel과 UI의 상태를 '병원' 모드로 강제 변경합니다.
                viewModel.changeSearchMode(SearchMode.HOSPITAL)
                updateToggleUI(SearchMode.HOSPITAL)
            }

            val receivedSpecialties = bundle.getStringArrayList("selected_specialties")?.toSet() ?: emptySet()
            filterVM.updateSpecialties(receivedSpecialties) // 공유 ViewModel 상태 업데이트

            val currentLocation = userLocationVM.location.value
            if (currentLocation != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.invalidateCache() // ✅ 새로운 필터가 적용되므로 캐시 초기화
                    viewModel.searchByLocation(currentLocation, receivedSpecialties.toList())
                }
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
        updateToggleUI(newMode)
        val query = binding.searchResultSearchView.text.toString()
        val location = userLocationVM.location.value
        viewModel.loadData(newMode = newMode, query = query, location = location)
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

        // ✅ 어댑터를 새로 만드는 대신, submitList로 데이터만 전달합니다.
        searchResultAdapter.submitList(items)
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
        if (this is TabOwner) {
            (activity as? MainActivity)?.updateNavIcons(getNavId())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}