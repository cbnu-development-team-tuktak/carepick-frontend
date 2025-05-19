package com.example.carepick.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.MainActivity
import com.example.carepick.R
import com.example.carepick.adapter.AutoCompleteAdapter
import com.example.carepick.adapter.SearchResultListAdapter
import com.example.carepick.databinding.FragmentSearchResultBinding
import com.example.carepick.dto.doctor.DoctorDetailsResponse
import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.model.SearchResultItem
import com.example.carepick.repository.HospitalRepository
import com.example.carepick.ui.location.LocationSettingFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 검색 결과 화면을 구현한 Fragment
// - 검색창을 제공하며 검색창에는 사용자가 검색했던 키워드를 넣는다 -> 구현할 예정
// - 검색창에서 새로운 병원 이름/ 의사 이름을 검색할 수 있다 -> 구현할 예정
// - 필터 버튼을 제공하고 별도의 모달창을 제공해서 필터 기준을 정할 수 있어야 한다 -> 구현할 예정
// - 사용자가 입력했던 키워드와 이름이 완전/부분적으로 일치하는 병원, 의사 목록을 출력한다
// - 각각의 병원과 의사 목록에서 하나를 선택할 경우 상세 페이지로 이동해야 한다
class SearchResultFragment : Fragment() {

    // fragment_search_result.xml을 사용할 것임을 명시하였다
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private val hospitalRepository = HospitalRepository()
    private val selectedFilters = mutableListOf<String>() // 추가: 선택된 필터를 저장할 리스트

    private var searchJob: Job? = null

    // 프래그먼트가 생성되었을 때 실행할 코드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // fragment_search_result.xml 레이아웃에 데이터를 바인딩하여 넣을 예정임을 나타내었다.
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 프래그먼트가 생성되고 위젯들이 배치된 후 실행할 코드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상태창 영역 침범하지 않도록 패딩 부여
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0) // 상단 padding만 수동 적용
            insets
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 정렬 팝업에서 결과 수신
                parentFragmentManager.setFragmentResultListener(
                    "sort_filter_result",
                    viewLifecycleOwner
                ) { _, bundle ->
                    val selectedSortText = bundle.getString("selected_filter_text")
                    if (!selectedSortText.isNullOrEmpty()) {
                        selectedFilters.clear()
                        selectedFilters.add(selectedSortText)
                        updateSearchSortButton()
                    }
                }

                // <<HomeFragment에서 보낸 데이터를 가져오는 코드>>
                // 사용자가 검색했던 키워드를 가져온다
                val query = arguments?.getString("search_query")
                Log.d("query", "$query")

                // 병원 정보에 존재하는 의사 정보 객체를 추출하여 별도 객체 리스트로 저장한다
//                val doctors = hospitals.flatMap { it.doctors ?: emptyList() }

                if (!query.isNullOrBlank()) {
                    // <<병원 목록 중 이름이 완전/부분적으로 일치하는 병원들만 가져오는 코드>>
                    // <<의사 목록 중 이름이 완전/부분적으로 일치하는 병원들만 가져오는 코드>>
//                    val filteredHospitals = hospitals.filter { it.name.contains(query, ignoreCase = true) }
//
//                    // 병원 정보와 의사 정보를 모두 담을 수 있는 SearchResultItem 객체의 리스트 형태를 가진 객체를 선언한다
//                    val allResults = mutableListOf<SearchResultItem>().apply {
//                        addAll(filteredHospitals)
//                    }

                    val hospital = hospitalRepository.getSearchedHospitals(query)
//
                    // 검색 결과가 없을 경우 안내 메시지를 표시한다
                    if (hospital.isEmpty()) {
                        binding.searchResultErrorText.visibility = View.VISIBLE
                        binding.searchResultRecyclerView.visibility = View.GONE
                    } else {
                        binding.searchResultErrorText.visibility = View.GONE
                        binding.searchResultRecyclerView.visibility = View.VISIBLE

                        // 병원/의사 목록을 출력할 어댑터를 호출한다
                        binding.searchResultRecyclerView.adapter = SearchResultListAdapter(hospital, requireActivity())
                        // 병원/의사 목록은 LinearLayout 형태로 출력한다
                        binding.searchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    }


                } else {
                    // 하단 네비게이션을 통해 처음 검색 결과 뷰로 넘어온 경우
                    binding.searchResultRecyclerView.visibility = View.GONE
                    binding.searchResultErrorText.visibility = View.GONE
//                    binding.searchResultRecentSearchText.visibility = View.VISIBLE
                }

                // <<검색창에서 병원 이름을 자동 완성하는 부분>>
                //
                // 병원 정보에서 병원 이름들만 뽑아낸다
//                val hospitalNames = hospital.map { it.name }
//
//                Log.d("AutoComplete", "names: $hospitalNames")
//
//                // 어댑터를 불러와서 부분적으로 일치하는 병원/의사 이름을 출력하도록 함
//                val autoCompleteNames = (hospitalNames).distinct()
//                val autoCompleteAdapter = AutoCompleteAdapter(requireContext(), autoCompleteNames)
//                binding.searchResultSearchView.setAdapter(autoCompleteAdapter)
//                // 한 문자만 입력해도 자동완성이 되도록 함
//                binding.searchResultSearchView.threshold = 1
//
//                // 자동완성 항목 클릭 시 검색 창에 해당 이름이 들어가도록 함
//                binding.searchResultSearchView.setOnItemClickListener { parent, _, position, _ ->
//                    val selectedName = parent.getItemAtPosition(position).toString()
//                    binding.searchResultSearchView.setText(selectedName)
//                }
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

                // <<검색 버튼 누르면 검색 결과 화면으로 이동>>
                binding.searchResultSearchView.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        val newQuery = binding.searchResultSearchView.text.toString()
                        if (newQuery.isNotBlank()) {
                            navigateToSearchResult(newQuery)
                        }
                        true
                    } else {
                        false
                    }
                }

            } catch (e: Exception) {
                Log.e("API_ERROR", "병원 데이터 불러오기 실패: ${e.message}")
                binding.searchResultErrorText.visibility = View.VISIBLE
                binding.searchResultRecyclerView.visibility = View.GONE
            }
        }

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

        // 위치 설정 버튼 클릭 시 LocationSettingFragment로 전환
        binding.btnLocationSetting.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LocationSettingFragment())
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

    override fun onResume() {
        super.onResume()
        (requireActivity() as? MainActivity)?.updateNavIcons(R.id.nav_search)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}