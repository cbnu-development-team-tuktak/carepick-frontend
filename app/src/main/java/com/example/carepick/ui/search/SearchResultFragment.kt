package com.example.carepick.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.MainActivity
import com.example.carepick.R
import com.example.carepick.adapter.SearchResultListAdapter
import com.example.carepick.databinding.FragmentSearchResultBinding
import com.example.carepick.dto.doctor.DoctorDetailsResponse
import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.model.SearchResultItem

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

        // <<HomeFragment에서 보낸 데이터를 가져오는 코드>>
        //
        // 사용자가 검색했던 키워드를 가져온다
        val query = arguments?.getString("search_query") ?: return
        // HomeFragment로부터 받은 병원 정보 목록을 가져온다
        val hospitals = arguments?.getParcelableArrayList<HospitalDetailsResponse>("hospitals") ?: return
        // HomeFragment로부터 받은 의사 정보 목록을 가져온다
        val doctors = arguments?.getParcelableArrayList<DoctorDetailsResponse>("doctors") ?: return


        // <<병원 목록 중 이름이 완전/부분적으로 일치하는 병원들만 가져오는 코드>>
        //
        val filteredHospitals = hospitals.filter { hospital ->
            hospital.name.contains(query, ignoreCase = true)
        }.toMutableList()

        // <<의사 목록 중 이름이 완전/부분적으로 일치하는 병원들만 가져오는 코드>>
        //
        val filteredDoctors = doctors.filter { doctor ->
            doctor.name.contains(query, ignoreCase = true)
        }.toMutableList()

        // 병원 정보와 의사 정보를 모두 담을 수 있는 SearchResultItem 객체의 리스트 형태를 가진 객체를 선언한다
        val allResults = mutableListOf<SearchResultItem>()
        // 병원 정보와 의사 정보를 집어넣는다
        allResults.addAll(filteredHospitals)
        allResults.addAll(filteredDoctors)

        // 병원/의사 목록을 출력할 어댑터를 호출한다
        binding.searchResultRecyclerView.adapter = SearchResultListAdapter(allResults, requireActivity())
        // 병원/의사 목록은 LinearLayout 형태로 출력한다
        binding.searchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? MainActivity)?.updateNavIcons(R.id.nav_search)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}