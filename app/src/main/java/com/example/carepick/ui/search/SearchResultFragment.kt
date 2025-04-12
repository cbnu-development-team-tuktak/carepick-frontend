package com.example.carepick.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
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
import kotlinx.coroutines.launch

// 검색 결과 화면을 구현한 Fragment
// - 검색창을 제공하며 검색창에는 사용자가 검색했던 키워드를 넣는다 -> 구현할 예정
// - 검색창에서 새로운 병원 이름/ 의사 이름을 검색할 수 있다 -> 구현할 예정
// - 필터 버튼을 제공하고 별도의 모달창을 제공해서 필터 기준을 정할 수 있어야 한다 -> 구현할 예정
// - 사용자가 입력했던 키워드와 이름이 완전/부분적으로 일치하는 병원, 의사 목록을 출력한다
// - 각각의 병원과 의사 목록에서 하나를 선택할 경우 상세 페이지로 이동해야 한다
class SearchResultFragment: Fragment() {

    // fragment_search_result.xml을 사용할 것임을 명시하였다
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private val hospitalRepository = HospitalRepository()


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

        // 병원 정보 목록을 가져온다
        val hospitals = hospitalRepository.loadHospitalsFromAsset(requireContext())
        // 병원 정보에 존재하는 의사 정보 객체를 추출하여 별도 객체 리스트로 저장한다
        val doctors = hospitals.flatMap { it.doctors ?: emptyList() }


        // <<HomeFragment에서 보낸 데이터를 가져오는 코드>>
        // 사용자가 검색했던 키워드를 가져온다
        val query = arguments?.getString("search_query")

        if (!query.isNullOrBlank()) {

            // <<병원 목록 중 이름이 완전/부분적으로 일치하는 병원들만 가져오는 코드>>
            // <<의사 목록 중 이름이 완전/부분적으로 일치하는 병원들만 가져오는 코드>>
            val filteredHospitals = hospitals.filter { it.name.contains(query, ignoreCase = true) }
            val filteredDoctors = doctors.filter { it.name.contains(query, ignoreCase = true) }

            // 병원 정보와 의사 정보를 모두 담을 수 있는 SearchResultItem 객체의 리스트 형태를 가진 객체를 선언한다
            val allResults = mutableListOf<SearchResultItem>().apply {
                addAll(filteredHospitals)
                addAll(filteredDoctors)
            }

            // 검색 결과가 없을 경우 안내 메시지를 표시한다
            if (allResults.isEmpty()) {
                binding.searchResultErrorText.visibility = View.VISIBLE
                binding.searchResultRecyclerView.visibility = View.GONE
            } else {
                binding.searchResultErrorText.visibility = View.GONE
                binding.searchResultRecyclerView.visibility = View.VISIBLE

                // 병원/의사 목록을 출력할 어댑터를 호출한다
                binding.searchResultRecyclerView.adapter = SearchResultListAdapter(allResults, requireActivity())
                // 병원/의사 목록은 LinearLayout 형태로 출력한다
                binding.searchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        } else {
            // 하단 네비게이션을 통해 처음 검색 결과 뷰로 넘어온 경우
            binding.searchResultRecyclerView.visibility = View.GONE
            binding.searchResultErrorText.visibility = View.GONE
            binding.searchResultRecentSearchText.visibility = View.VISIBLE
        }

        viewLifecycleOwner.lifecycleScope.launch {

            // <<검색창에서 병원 이름을 자동 완성하는 부분>>
            //
            // 병원 정보에서 병원 이름들만 뽑아낸다
            val hospitalNames = hospitals.map { it.name }
            // 의사 정보에서 의사 이름들만 뽑아낸다
            val doctorNames = doctors.map { it.name }

            Log.d("AutoComplete", "names: $hospitalNames")

            // 어댑터를 불러와서 부분적으로 일치하는 병원/의사 이름을 출력하도록 함
            val autoCompleteNames = (hospitalNames + doctorNames).distinct()
            val autoCompleteAdapter = AutoCompleteAdapter(requireContext(), autoCompleteNames)
            binding.searchResultSearchView.setAdapter(autoCompleteAdapter)
            // 한 문자만 입력해도 자동완성이 되도록 함
            binding.searchResultSearchView.threshold = 1

            // 자동완성 항목 클릭 시 검색 창에 해당 이름이 들어가도록 함
            binding.searchResultSearchView.setOnItemClickListener { parent, _, position, _ ->
                val selectedName = parent.getItemAtPosition(position).toString()
                binding.searchResultSearchView.setText(selectedName)
            }

            // <<검색 버튼 누르면 검색 결과 화면으로 이동>>
            binding.searchResultSearchView.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = binding.searchResultSearchView.text.toString()
                    if (query.isNotBlank()) {
                        navigateToSearchResult(query, hospitals, doctors)
                    }
                    true
                } else {
                    false
                }
            }
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
    // 백엔드 서버로부터 병원 정보를 받는게 아니라 로컬에 저장된 json 파일을 이용한다
    private fun navigateToSearchResult(query: String, hospitalList: List<HospitalDetailsResponse>, doctorList: List<DoctorDetailsResponse>) {

        // 검색 결과를 출력하는 프래그먼트에 데이터를 전달하기 위해 bundle에 데이터를 담는다
        // 검색 결과 프래그먼트에서는 arguments를 이용해서 아래의 데이터들을 불러올 수 있다
        val bundle = Bundle().apply {
            // 사용자가 검색한 키워드를 담는다
            putString("search_query", query)
            // 병원 목록을 담는다
            putParcelableArrayList("hospitals", ArrayList(hospitalList))
            // 의사 목록을 담는다
            putParcelableArrayList("doctors", ArrayList(doctorList))
        }

        // 검색 버튼을 누를 경우 어떤 프래그먼트로 넘어갈지를 지정한다
        val fragment = SearchResultFragment()
        // 해당 프래그먼트에 넘길 데이터를 지정한다
        fragment.arguments = bundle

        // 검색 결과 프래그먼트로 화면을 전환한다
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}