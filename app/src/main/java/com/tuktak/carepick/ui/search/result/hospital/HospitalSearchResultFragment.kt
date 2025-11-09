package com.tuktak.carepick.ui.search.result.hospital

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
import com.tuktak.carepick.TabOwner
import com.tuktak.carepick.data.model.DoctorDetailsResponse
import com.tuktak.carepick.data.model.HospitalDetailsResponse
import com.tuktak.carepick.data.model.LoadingItem
import com.tuktak.carepick.data.model.SearchResultItem
import com.tuktak.carepick.databinding.FragmentHospitalSearchResultBinding
import com.tuktak.carepick.ui.hospital.HospitalDetailFragment
import com.tuktak.carepick.ui.location.viewModel.UserLocationViewModel
import com.tuktak.carepick.ui.location.viewModelFactory.UserLocationViewModelFactory
import com.tuktak.carepick.ui.search.result.SearchResultUiState
import com.tuktak.carepick.ui.search.result.adapter.SearchResultListAdapter
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class HospitalSearchResultFragment : Fragment(), TabOwner {

    // fragment_search_result.xml을 사용할 것임을 명시하였다
    private var _binding: FragmentHospitalSearchResultBinding? = null
    private val binding get() = _binding!!

    // ✅ 병원 전용 ViewModel 사용
    private val viewModel: HospitalSearchViewModel by viewModels { HospitalSearchViewModelFactory() }
    private val userLocationVM: UserLocationViewModel by activityViewModels {
        UserLocationViewModelFactory(requireContext().applicationContext)
    }
    private val filterVM: HospitalFilterViewModel by activityViewModels()

    override fun getNavId(): Int = R.id.nav_hospital // 👈 탭 ID 변경


    // 프래그먼트가 생성되었을 때 실행할 코드
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHospitalSearchResultBinding.inflate(inflater, container, false) // 👈 바인딩 클래스 변경
        return binding.root
    }

    // 프래그먼트가 생성되고 위젯들이 배치된 후 실행할 코드
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

    private val logTag = "HospitalSearchFrag"

    /** ✅ FilterViewModel의 isAnyFilterActive 상태를 구독하여 필터 버튼 UI를 업데이트 */
    private fun observeFilterState() {
        viewLifecycleOwner.lifecycleScope.launch {
            filterVM.isAnyFilterActive.collect { isActive ->
                updateFilterButtonUI(isActive)
            }
        }
    }

    /** ✅ 필터 활성화 상태에 따라 필터 버튼 배경 변경 */
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


    // 3. ✅ 위치 정보를 구독하고 어댑터를 업데이트하는 함수를 추가합니다.
    private fun observeUserLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            userLocationVM.location.collect { location ->
                if (location != null) {
                    searchResultAdapter.updateUserLocation(location)
                }
            }
        }
    }

    // ✅ [추가] 프래그먼트가 보여지거나 숨겨질 때 호출되는 콜백
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        val args = arguments
        val query = args?.getString("search_query")
        val initialSpecialty = args?.getString("initial_specialty_filter")

        // ❌ 모드 관련 로직 모두 삭제

        if (initialSpecialty != null) {
            filterVM.updateSpecialties(setOf(initialSpecialty))
        }

        binding.searchResultSearchView.setText(query)
        val currentSortBy = filterVM.selectedSortBy

        lifecycleScope.launch {
            showLoading()
            try {
                withTimeout(5000L) {
                    val location = userLocationVM.location.first { it != null }
                    val specialties = if (initialSpecialty != null) listOf(initialSpecialty) else filterVM.selectedSpecialties.toList()

                    // ✅ 병원 ViewModel의 loadData 호출
                    viewModel.loadData(
                        query = query,
                        location = location!!,
                        specialties = specialties,
                        days = filterVM.selectedDays.toList(),
                        startTime = filterVM.startTime,
                        endTime = filterVM.endTime,
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
                // ❌ 의사 상세 로직 삭제 (단, 병원에서 의사로 넘어갈 수 있다면 유지)
                is LoadingItem -> {
                }
                is DoctorDetailsResponse -> TODO()
            }
        }
    }

    // ✅ ViewModel의 상태 변화를 감지하고 UI 업데이트
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

    // ✨ 1. 리스너 설정 로직 분리
    private fun setupListeners() {
        // ❌ 토글 버튼 리스너 삭제

        binding.searchResultSearchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val newQuery = binding.searchResultSearchView.text.toString()
                if (newQuery.isNotBlank()) {
                    hideKeyboard()
                    lifecycleScope.launch {
                        val location = userLocationVM.location.first { it != null }
                        viewModel.loadData(
                            query = newQuery,
                            location = location!!,
                            specialties = filterVM.selectedSpecialties.toList(),
                            days = filterVM.selectedDays.toList(),
                            startTime = filterVM.startTime,
                            endTime = filterVM.endTime,
                            distance = filterVM.selectedDistance,
                            sortBy = filterVM.selectedSortBy,
                            forceReload = true
                        )
                    }
                }
                return@setOnEditorActionListener true
            }
            false
        }

        parentFragmentManager.setFragmentResultListener("hospital_filter_request", viewLifecycleOwner) { _, bundle ->
            // ❌ 모드 변경 로직 삭제

            val receivedSpecialtiesSet = bundle.getStringArrayList("selected_specialties")?.toSet() ?: emptySet()
            val receivedDays = bundle.getStringArrayList("selected_days")?.toList()
            val receivedStartTime = bundle.getString("start_time")
            val receivedEndTime = bundle.getString("end_time")
            val receivedDistance = bundle.getInt("selected_distance", -1)

            // ViewModel 상태 업데이트
            filterVM.updateSpecialties(receivedSpecialtiesSet)
            filterVM.updateOperatingHours(receivedDays?.toSet() ?: emptySet(), receivedStartTime, receivedEndTime)
            filterVM.updateDistance(if (receivedDistance == -1) 0 else receivedDistance)

            val currentLocation = userLocationVM.location.value
            if (currentLocation != null) {
                viewModel.loadData(
                    location = currentLocation,
                    query = binding.searchResultSearchView.text.toString().ifBlank { null },
                    specialties = receivedSpecialtiesSet.toList(),
                    days = receivedDays,
                    startTime = receivedStartTime,
                    endTime = receivedEndTime,
                    distance = if (receivedDistance == -1) null else receivedDistance,
                    sortBy = filterVM.selectedSortBy,
                    forceReload = true
                )
            } else {
                Toast.makeText(requireContext(), "위치 정보가 없어 필터를 적용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        parentFragmentManager.setFragmentResultListener("hospital_sort_request", viewLifecycleOwner) { _, bundle ->
            val sortBy = bundle.getString("selected_sort_by") ?: "distance"
            val buttonText = bundle.getString("selected_filter_text") ?: "정렬"

            filterVM.updateSortBy(sortBy)
            binding.searchResultSortButtonText.text = buttonText

            val currentLocation = userLocationVM.location.value
            if (currentLocation != null) {
                viewModel.loadData(
                    location = currentLocation,
                    query = binding.searchResultSearchView.text.toString().ifBlank { null },
                    specialties = filterVM.selectedSpecialties.toList(),
                    days = filterVM.selectedDays.toList(),
                    startTime = filterVM.startTime,
                    endTime = filterVM.endTime,
                    distance = filterVM.selectedDistance,
                    sortBy = sortBy,
                    forceReload = true
                )
            } else {
                Toast.makeText(requireContext(), "위치 정보가 없어 정렬을 적용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.searchResultFilterButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.fragment_container, HospitalFilterFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.searchResultSortButton.setOnClickListener {
            // ✅ 병원 전용 정렬 바텀 시트 실행
            val bottomSheet = HospitalSortBottomSheetFragment()
            // 모드 전달 불필요 (이미 병원 전용임)
            bottomSheet.show(parentFragmentManager, "HospitalSort")
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