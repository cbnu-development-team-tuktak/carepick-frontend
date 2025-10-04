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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.MainActivity
import com.example.carepick.R
import com.example.carepick.common.adapter.AutoCompleteAdapter
import com.example.carepick.common.ui.HospitalDetailFragment
import com.example.carepick.data.model.DoctorDetailsResponse
import com.example.carepick.data.model.HospitalDetailsResponse
import com.example.carepick.databinding.FragmentSearchResultBinding
import com.example.carepick.data.repository.HospitalRepository
import com.example.carepick.ui.location.repository.UserLocation
import com.example.carepick.ui.location.viewModel.UserLocationViewModel
import com.example.carepick.ui.location.viewModelFactory.UserLocationViewModelFactory
import com.example.carepick.ui.search.filter.FilterFragment
import com.example.carepick.ui.search.filter.SortFilterBottomSheetFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchResultFragment : Fragment() {

    // fragment_search_result.xml을 사용할 것임을 명시하였다
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private val hospitalRepository = HospitalRepository()
    private val selectedFilters = mutableListOf<String>() // 추가: 선택된 필터를 저장할 리스트
    private var searchJob: Job? = null

    private val userLocationVM: UserLocationViewModel by activityViewModels {
        UserLocationViewModelFactory(requireContext().applicationContext)
    }

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
        observeDataAndLoadContent()
    }

    // ✨ 1. 리스너 설정 로직 분리
    private fun setupListeners() {
        // 정렬 팝업 결과 리스너
        parentFragmentManager.setFragmentResultListener("sort_filter_result", viewLifecycleOwner) { _, bundle ->
            val selectedSortText = bundle.getString("selected_filter_text")
            if (!selectedSortText.isNullOrEmpty()) {
                selectedFilters.clear()
                selectedFilters.add(selectedSortText)
                updateSearchSortButton()
                // 거리순 정렬이면 현재 위치 기준으로 다시 로드 (위치 값은 collect를 통해 이미 확보됨)
                if (selectedSortText.contains("거리", ignoreCase = true)) {
                    userLocationVM.location.value?.let { loadHospitalsByLocation(it) }
                }
            }
        }

        // 검색창 자동완성 및 검색 실행 리스너
        // 검색창 입력 이벤트 처리
        binding.searchResultSearchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s?.toString()?.trim() ?: ""

                // 키워드가 1글자 이상일 때만 검색
                if (keyword.length >= 1) {
                    // 이전 검색 작업 취소 (debounce 효과)
                    searchJob?.cancel()

                    // 300ms 대기 후 검색 (네트워크 부하 감소)
                    searchJob = lifecycleScope.launch {
                        delay(300)

                        if (!isAdded) return@launch

                        try {
                            val hospitals = hospitalRepository.getSearchedHospitals(keyword)

                            // 병원 이름들 추출
                            val hospitalNames = hospitals.map { it.name }

                            // 로그로 확인
                            Log.d("AutoComplete", "names: $hospitalNames")

                            if (hospitalNames.isNotEmpty() && isAdded) {
                                // 어댑터 설정
                                val autoCompleteAdapter = AutoCompleteAdapter(requireContext(), hospitalNames)
                                binding.searchResultSearchView.setAdapter(autoCompleteAdapter)

                                // 한 글자만 입력해도 자동완성이 되도록 설정
                                binding.searchResultSearchView.threshold = 1

                                // 어댑터 갱신 (필수)
                                autoCompleteAdapter.notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            Log.e("SearchError", "Error occurred while searching", e)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.searchResultSearchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val newQuery = binding.searchResultSearchView.text.toString()
                if (newQuery.isNotBlank()) {
                    hideKeyboard()
                    performKeywordSearch(newQuery) // ✨ 새 검색어로 데이터만 다시 로드
                }
                true
            } else false
        }

        // 나머지 버튼 리스너들
        // 정렬 버튼 클릭 이벤트 추가
        binding.searchResultSortButton.setOnClickListener {
            val bottomSheet = SortFilterBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }
        // 필터 버튼 클릭 이벤트 추가
        binding.searchResultFilterButton.setOnClickListener {
            val filterFragment = FilterFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, filterFragment)
                .addToBackStack(null)
                .commit()
        }
        // 병원 / 의사 토글 버튼 클릭 처리
        binding.searchResultHospitalFilterButton.setOnClickListener {
            binding.searchResultHospitalFilterButton.setBackgroundResource(R.drawable.bg_search_result_filter_left_selected)
            binding.searchResultDoctorFilterButton.setBackgroundResource(R.drawable.bg_search_result_filter_right)
        }
        binding.searchResultDoctorFilterButton.setOnClickListener {
            binding.searchResultDoctorFilterButton.setBackgroundResource(R.drawable.bg_search_result_filter_right_selected)
            binding.searchResultHospitalFilterButton.setBackgroundResource(R.drawable.bg_search_result_filter_left)
        }
    }

    // ✨ 2. 데이터 구독 및 초기 로딩 로직 분리 (레이스 컨디션 해결)
    private fun observeDataAndLoadContent() {
        viewLifecycleOwner.lifecycleScope.launch {
            userLocationVM.location.collectLatest { location ->
                val query = arguments?.getString("search_query")

                if (query.isNullOrBlank()) { // 검색어 없음 -> 위치 기반 검색
                    if (location != null) {
                        loadHospitalsByLocation(location)
                    } else {
                        showError(getString(R.string.need_location_message))
                    }
                } else { // 검색어 있음 -> 키워드 검색
                    performKeywordSearch(query)
                    binding.searchResultSearchView.setText(query) // 검색창에 검색어 설정
                }
            }
        }
    }

    // ✨ 3. 위치 기반 검색 함수 수정
    private fun loadHospitalsByLocation(location: UserLocation, distanceKm: Double = 5.0) {
        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val hospitals = hospitalRepository.getHospitalsWithExtendedFilter(
                    lat = location.lat,
                    lng = location.lng,
                    distance = distanceKm,
                    specialties = null,
                    selectedDays = null,
                    startTime = null,
                    endTime = null,
                    sortBy = "distance",
                    page = 0,
                    size = 30
                )
                if (hospitals.isEmpty()) {
                    showError(getString(R.string.no_results_in_range))
                } else {
                    showContent(hospitals)
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "위치 기반 병원 조회 실패: ${e.message}", e)
                showError(getString(R.string.fetch_error_message))
            } finally {
                showLoading(false)
            }
        }
    }

    // ✨ 4. 키워드 검색 함수 분리
    private fun performKeywordSearch(query: String) {
        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val hospitals = hospitalRepository.getSearchedHospitals(query)
                if (hospitals.isEmpty()) {
                    showError("'$query'에 대한 검색 결과가 없습니다.")
                } else {
                    showContent(hospitals)
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "키워드 검색 실패: ${e.message}", e)
                showError(getString(R.string.fetch_error_message))
            } finally {
                showLoading(false)
            }
        }
    }

    // ✨ 5. UI 상태 변경 함수들 분리
    private fun showLoading(isLoading: Boolean) {
        // binding.progressBar.isVisible = isLoading // 프로그레스바가 있다면 활용
    }

    private fun showError(message: String) {
        binding.searchResultErrorText.text = message
        binding.searchResultErrorText.visibility = View.VISIBLE
        binding.searchResultRecyclerView.visibility = View.GONE
    }

    private fun showContent(results: List<SearchResultItem>) {
        binding.searchResultErrorText.visibility = View.GONE
        binding.searchResultRecyclerView.visibility = View.VISIBLE
        setupRecyclerView(results)
    }

    // 추가: 필터 체크박스 선택/해제 시 리스트 업데이트
    private fun updateFilter(filterName: String, isChecked: Boolean) {
        if (isChecked) {
            if (!selectedFilters.contains(filterName)) selectedFilters.add(filterName)
        } else {
            selectedFilters.remove(filterName)
        }
        updateSearchSortButton()
    }

    // 추가: 정렬 버튼 UI 업데이트
    private fun updateSearchSortButton() {
        if (selectedFilters.isNotEmpty()) {
            binding.searchResultSortButton.setBackgroundResource(R.drawable.bg_search_result_btn_active)
            binding.searchResultSortButtonText.text = selectedFilters.joinToString(", ")
        } else {
            binding.searchResultSortButton.setBackgroundResource(R.drawable.bg_search_result_btn)
            binding.searchResultSortButtonText.text = "정렬"
        }
    }

    // 검색 버튼을 클릭할 경우 검색 결과 화면으로 이동한다
    // 백엔드 서버로부터 병원 정보를 받는게 아니라 http://localhost:8080/api/hospitals/filter 이용한다.
    private fun navigateToSearchResult(query: String) {

        // 검색 결과를 출력하는 프래그먼트에 데이터를 전달하기 위해 bundle에 데이터를 담는다
        // 검색 결과 프래그먼트에서는 arguments를 이용해서 아래의 데이터들을 불러올 수 있다
        val bundle = Bundle().apply {
            // 사용자가 검색한 키워드를 담는다
            putString("search_query", query)
        }

        // 검색 결과를 출력할 프래그먼트를 생성하고 데이터를 연결한다
        val fragment = SearchResultFragment()
        fragment.arguments = bundle

        // 검색 결과 프래그먼트로 화면을 전환한다
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // RecyclerView 설정
    private fun setupRecyclerView(results: List<SearchResultItem>) {
        val myAdapter = SearchResultListAdapter(results, requireActivity()) { item ->
            val detailFragment = when (item) {
                is HospitalDetailsResponse -> HospitalDetailFragment().apply {
                    arguments = Bundle().apply { putString("hospitalId", item.id) }
                }
                is DoctorDetailsResponse -> {
                    // DoctorDetailFragment().apply { ... } // 의사 상세 화면 로직
                    null
                }
                else -> null
            }

            detailFragment?.let {
                parentFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, it)
                    .addToBackStack(null)
                    .commit()
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