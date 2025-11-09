package com.tuktak.carepick.ui.search.result.doctor

import android.os.Bundle
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
import com.tuktak.carepick.MainActivity
import com.tuktak.carepick.R
import com.tuktak.carepick.SortFilterBottomSheetFragment
import com.tuktak.carepick.TabOwner
import com.tuktak.carepick.common.ui.DoctorDetailFragment
import com.tuktak.carepick.data.model.DoctorDetailsResponse
import com.tuktak.carepick.data.model.HospitalDetailsResponse
import com.tuktak.carepick.data.model.LoadingItem
import com.tuktak.carepick.data.model.SearchResultItem
import com.tuktak.carepick.databinding.FragmentDoctorSearchResultBinding
import com.tuktak.carepick.ui.location.viewModel.UserLocationViewModel
import com.tuktak.carepick.ui.location.viewModelFactory.UserLocationViewModelFactory
import com.tuktak.carepick.ui.search.FilterViewModel
import com.tuktak.carepick.ui.search.result.hospital.HospitalFilterFragment
import com.tuktak.carepick.ui.search.result.SearchResultUiState
import com.tuktak.carepick.ui.search.result.adapter.SearchResultListAdapter
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class DoctorSearchResultFragment : Fragment(), TabOwner  {
    private var _binding: FragmentDoctorSearchResultBinding? = null // 👈 바인딩 클래스 변경
    private val binding get() = _binding!!

    // ✅ 의사 전용 ViewModel 사용
    private val viewModel: DoctorSearchViewModel by viewModels { DoctorSearchViewModelFactory() }
    private val userLocationVM: UserLocationViewModel by activityViewModels {
        UserLocationViewModelFactory(requireContext().applicationContext)
    }
    private val filterVM: DoctorFilterViewModel by activityViewModels()

    override fun getNavId(): Int = R.id.nav_doctor // 👈 탭 ID 변경

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDoctorSearchResultBinding.inflate(inflater, container, false) // 👈 바인딩 클래스 변경
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchResultRecyclerView.adapter = searchResultAdapter
        binding.searchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (totalItemCount > 0 && lastVisibleItemPosition >= totalItemCount - 5) {
                    val currentLocation = userLocationVM.location.value
                    viewModel.loadNextPage() // 👈 ViewModel 로직 단순화 (위치 필요시 ViewModel이 이미 알고 있음)
                }
            }
        })

        observeFilterState()
        observeUserLocation()
        setupWindowInsets()
        setupListeners()
        observeUiState()
    }

    private fun observeFilterState() {
        viewLifecycleOwner.lifecycleScope.launch {
            filterVM.isAnyFilterActive.collect { isActive ->
                updateFilterButtonUI(isActive)
            }
        }
    }

    private fun updateFilterButtonUI(isActive: Boolean) {
        if (isActive) {
            // 필터가 활성화되었을 때 사용할 배경 (예: bg_search_result_btn_active)
            binding.searchResultFilterButton.setBackgroundResource(R.drawable.bg_search_result_btn_active)
            // 필요하다면 텍스트 색상 등도 변경
            // binding.searchResultFilterButtonText.setTextColor(...)
        } else {
            // 필터가 비활성화되었을 때 사용할 기본 배경
            binding.searchResultFilterButton.setBackgroundResource(R.drawable.bg_search_result_btn)
            // binding.searchResultFilterButtonText.setTextColor(...)
        }
    }

    private fun observeUserLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            userLocationVM.location.collect { location ->
                if (location != null) {
                    searchResultAdapter.updateUserLocation(location)
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        val args = arguments
        val query = args?.getString("search_query")
        val initialModeString = args?.getString("initial_search_mode") // Home에서 DOCTOR로 넘어올 수 있음

        // ❌ 자가진단 필터 로직(initialSpecialty)은 의사 탭과 관련 없으므로 제거

        if (initialModeString != null && initialModeString == "HOSPITAL") {
            // 혹시 병원 모드로 잘못 넘어왔을 경우 (로직 오류 방지)
            // 아무것도 안 하거나, 기본 의사 검색 수행
        }

        binding.searchResultSearchView.setText(query)
        val currentSortBy = filterVM.selectedSortBy

        lifecycleScope.launch {
            showLoading()
            try {
                withTimeout(5000L) {
                    val location = userLocationVM.location.first { it != null }

                    // ✅ 의사 ViewModel의 loadData 호출 (운영시간 필터 없음)
                    viewModel.loadData(
                        query = query,
                        location = location!!,
                        specialties = filterVM.selectedSpecialties.toList(),
                        distance = filterVM.selectedDistance,
                        sortBy = currentSortBy,
                        forceReload = true
                    )
                }
            } catch (e: TimeoutCancellationException) {
                showError(getString(R.string.need_location_message))
            }
        }
        arguments = null
    }

    private val searchResultAdapter by lazy {
        SearchResultListAdapter(requireActivity()) { item ->
            when (item) {
                is DoctorDetailsResponse -> {
                    val detailFragment = DoctorDetailFragment()
                    detailFragment.arguments = Bundle().apply { putString("doctorId", item.id) }
                    parentFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit()
                }

                is LoadingItem -> { /* 클릭 무시 */
                }

                is HospitalDetailsResponse -> TODO()
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SearchResultUiState.Loading -> showLoading()
                    is SearchResultUiState.Error -> showError(state.message)
                    // ✅ Success와 LoadingNextPage는 어차피 목록을 보여주므로
                    //    하나의 블록으로 통합할 수 있습니다.
                    is SearchResultUiState.Success -> showContent(state.items)
                    is SearchResultUiState.LoadingNextPage -> showContent(state.items)
                }
            }
        }
    }

    private fun setupListeners() {
        // ❌ 병원/의사 토글 버튼 리스너는 이 프래그먼트에 없습니다.

        // 검색창 키보드의 '검색' 버튼 리스너
        binding.searchResultSearchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val newQuery = binding.searchResultSearchView.text.toString()
                if (newQuery.isNotBlank()) {
                    hideKeyboard()
                    // 코루틴을 시작하고, 그 안에서 suspend 함수 및 ViewModel 호출
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            // 위치 정보를 비동기적으로 가져옴
                            val location = withTimeout(5000L) { userLocationVM.location.first { it != null } }
                            // 필터 ViewModel에서 현재 정렬 기준을 가져옴
                            val sortBy = filterVM.selectedSortBy

                            viewModel.loadData(
                                query = newQuery,
                                location = location,
                                specialties = filterVM.selectedSpecialties.toList(),
                                distance = filterVM.selectedDistance,
                                sortBy = sortBy,
                                forceReload = true
                            )
                        } catch (e: TimeoutCancellationException) {
                            showError(getString(R.string.need_location_message))
                        } catch (e: Exception) {
                            showError("검색 중 오류가 발생했습니다.")
                        }
                    }
                }
                // ✅ true를 반환하여 이벤트가 처리되었음을 알림
                return@setOnEditorActionListener true
            }
            // ✅ false를 반환하여 이벤트가 처리되지 않았음을 알림
            false
        }

        // 필터 화면 결과 리스너
        parentFragmentManager.setFragmentResultListener("doctor_filter_request", viewLifecycleOwner) { _, bundle ->
            val receivedSpecialtiesSet = bundle.getStringArrayList("selected_specialties")?.toSet() ?: emptySet()
            val receivedDistance = bundle.getInt("selected_distance", -1)

            // 의사 필터만 ViewModel에 업데이트
            filterVM.updateSpecialties(receivedSpecialtiesSet)
            filterVM.updateDistance(if (receivedDistance == -1) 0 else receivedDistance)
            // ❌ 운영 시간 관련 로직은 여기에 없음

            val currentLocation = userLocationVM.location.value
            if (currentLocation != null) {
                // ✅ FilterViewModel에서 현재 정렬 기준을 가져옴
                val sortBy = filterVM.selectedSortBy

                viewModel.loadData(
                    location = currentLocation,
                    query = binding.searchResultSearchView.text.toString().ifBlank { null },
                    specialties = receivedSpecialtiesSet.toList(),
                    // ✅ Bundle에서 받은 거리 정보를 전달
                    distance = if (receivedDistance == -1) null else receivedDistance,
                    sortBy = sortBy, // ✅ sortBy 변수 전달
                    forceReload = true
                )
            } else {
                Toast.makeText(requireContext(), "위치 정보가 없어 필터를 적용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 정렬 BottomSheet 결과 리스너
        parentFragmentManager.setFragmentResultListener("doctor_sort_request", viewLifecycleOwner) { _, bundle ->
            // ✅ Bundle에서 sortBy 값을 가져옴
            val sortBy = bundle.getString("selected_sort_by") ?: "distance"
            val buttonText = bundle.getString("selected_filter_text") ?: "정렬"

            // FilterViewModel의 정렬 상태 업데이트
            filterVM.updateSortBy(sortBy)
            binding.searchResultSortButtonText.text = buttonText

            // ✅ ViewModel에 데이터 로딩 요청 전, 현재 위치를 가져옴
            val currentLocation = userLocationVM.location.value
            if (currentLocation != null) {
                viewModel.loadData(
                    location = currentLocation,
                    query = binding.searchResultSearchView.text.toString().ifBlank { null },
                    specialties = filterVM.selectedSpecialties.toList(),
                    distance = filterVM.selectedDistance,
                    sortBy = sortBy, // ✅ Bundle에서 가져온 sortBy 값 전달
                    forceReload = true
                )
            } else {
                Toast.makeText(requireContext(), "위치 정보가 없어 정렬을 적용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 필터 버튼 클릭 리스너 (DOCTOR 모드 고정)
        binding.searchResultFilterButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.fragment_container, DoctorFilterFragment())
                .addToBackStack(null)
                .commit()
        }

        // 정렬 버튼 클릭 리스너 (DOCTOR 모드 고정)
        binding.searchResultSortButton.setOnClickListener {
            DoctorSortBottomSheetFragment().show(parentFragmentManager, "DoctorSort")
        }
    }

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