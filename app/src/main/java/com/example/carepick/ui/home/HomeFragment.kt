package com.example.carepick.ui.home

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.MainActivity
import com.example.carepick.R
import com.example.carepick.adapter.AutoCompleteAdapter
import com.example.carepick.databinding.FragmentHomeBinding
import com.example.carepick.repository.ServiceListRepository
import com.example.carepick.adapter.HospitalCardAdapter
import com.example.carepick.adapter.ServiceListAdapter
import com.example.carepick.dto.doctor.DoctorDetailsResponse
import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.repository.HospitalRepository
import com.example.carepick.ui.search.SearchResultFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
// 홈화면의 기능을 구현한 Fragment
// - 메인 뷰의 검색창을 제공하며, 사용자가 입력한 키워드에 대해 병원/의사 이름 자동완성 목록을 제공한다.
// - 메인 뷰의 서비스 목록을 제공하며 각각의 서비스를 선택하면 해당하는 프래그먼트로 넘어가도록 한다.
// - 즐겨찾기 목록에서 현재 가져온 모든 병원 정보를 카드뷰의 형태로 출력하고 있다 --> 수정할 예정
class HomeFragment: Fragment() {

    // fragment_home.xml 을 객체처럼 취급하여 binding 변수에 저장할 것임을 선언한다
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var searchJob: Job? = null


    // <<프래그먼트에서 사용할 리포지토리를 가져오는 부분>>
    //
    // 서비스 목록 카드뷰와 병원 목록 카드뷰를 생성하기 위한 각각의 리포지토리
    // 서비스 리포지토리는 현재 시스템이 제공하는 서비스의 목록을 반환한다
    private val serviceListRepository = ServiceListRepository()
    // 병원 리포지토리는 병원 정보를 반환한다
    // 현재는 로컬 json 파일에서 병원 정보를 가져올 예정
    private val hospitalRepository = HospitalRepository()


    // 처음 프래그먼트가 생성되었을 때 실행하는 코드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 홈화면은 fragment_home.xml을 사용할 것임을 나타내는 코드
        // fragment_home 레이아웃에 데이터를 바인딩한다
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 기본적으로 네트워크 오류 화면 숨김
        // 마찬가지로 병원 카드 목록을 출력하는 레이아웃도 숨긴다
        binding.networkErrorView.root.visibility = View.GONE
        binding.hospitalListRecyclerView.visibility = View.GONE

        // <<서비스 목록에 서비스 카드 정보를 가져와서 출력하는 코드>>
        //
        // 서비스 리포지토리로부터 서비스 목록 정보를 가져온다
        val serviceList = serviceListRepository.getServiceList()
        // 카드뷰를 좌우로 3개씩 배치하도록 그리드 레이아웃 형태로 배치함을 명시한다
        binding.serviceListRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        // 리포지토리로부터 받아온 정보를 토대로 카드뷰들을 동적으로 생성한다
        // ServiceListAdapter를 불러와서 어디에 어떤 데이터가 들어갈지를 지정한다
        binding.serviceListRecyclerView.adapter = ServiceListAdapter(serviceList, requireActivity())

        return binding.root
    }

    // 프래그먼트 내부의 위젯들이 생성된 후 작동하는 코드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상태창 영역 침범하지 않도록 패딩 부여
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0) // 상단 padding만 수동 적용

            insets
        }

        // 이 내부의 코드는 HomeFragment의 생명주기를 따른다
        // 즉, 사용자가 HomeFragment에 있을 때에만 내부 코드들이 실행되고,
        // 사용자가 다른 화면으로 이동하거나 하면 내부 코드들은 작동이 멈춘다.
        //
        // 병원 정보를 백엔드 서버에서 가져올 경우, 백엔드 서버에서 병원 정보를 가져오던 중,
        // 사용자가 다른 화면으로 넘어가거나 해서 HomeFragment의 생명주기가 끝나게 되면
        // 백엔드에서 가져온 병원 정보를 null 처리된 위치에 넣으려고 할 수 있기 때문이다
        viewLifecycleOwner.lifecycleScope.launch {

            // <<병원 정보를 백엔드 서버에서 받아서 동적으로 카드를 생성하는 파트>>
            // 현재는 주석처리하여 사용하지 않는다
            // val hospitalList = hospitalRepository.fetchHospitals()

            // <<병원 정보를 사전에 저장된 json 파일에서 읽는 코드>>
            // hospitalList는 HospitalDetailResponse들의 MutableList를 담는다
//            val hospitalList = hospitalRepository.loadHospitalsFromAsset(requireContext())
//
//            // 병원 정보에 존재하는 의사 정보 객체를 추출하여 별도 객체 리스트로 저장한다
//            val doctorList = hospitalList.flatMap { it.doctors ?: emptyList() }
//
//            // 제대로 병원 정보를 로드했는지, 그 여부에 따른 동작을 지정하는 코드
//            if (hospitalList.isEmpty()) {
//                // 병원 목록이 비어있는 경우, 병원 정보를 가져오는데 이상이 있었다는 의미이다
//                // 네트워크 오류 발생 시 오류 화면 표시
//                binding.networkErrorView.root.visibility = View.VISIBLE
//                binding.hospitalListRecyclerView.visibility = View.GONE
//            } else {
//                // 병원 정보 목록이 비어있지 않은 경우
//                // 병원 정보를 제대로 가져온 상태이다
//                // 네트워크 오류 화면은 표시하지 않고, 병원 목록을 출력할 리사이클러 뷰를 출력해야 한다.
//                binding.hospitalListRecyclerView.visibility = View.VISIBLE
//                binding.networkErrorView.root.visibility = View.GONE
//
//                // 병원 목록을 출력할 어댑터를 지정한다
//                // 어댑터를 통해 어떤 레이아웃에 어떤 위치에 어떤 정보가 저장되는지를 지정한다
//                // 또한 어댑터를 통해 각각의 카드뷰에 대한 리스너를 추가할 수 있다
//                binding.hospitalListRecyclerView.adapter = HospitalCardAdapter(hospitalList, requireActivity())
//                binding.hospitalListRecyclerView.setHasFixedSize(true)
//
//                // 병원 정보를 몇개 가져왔는지 확인하기 위한 코드
//                val itemCount = HospitalCardAdapter(hospitalList, requireActivity()).getItemCount()
//                Log.e("Item Count", "$itemCount")
//            }
//            // 병원 카드는 좌우 스크롤을 할 수 있도록 배치된다
//            binding.hospitalListRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)




            // 검색창 입력 이벤트 처리
            binding.searchView.addTextChangedListener(object : TextWatcher {
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
                                    binding.searchView.setAdapter(autoCompleteAdapter)

                                    // 한 글자만 입력해도 자동완성이 되도록 설정
                                    binding.searchView.threshold = 1

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

            // 검색 버튼 클릭 이벤트
            binding.searchView.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = binding.searchView.text.toString()
                    if (query.isNotBlank()) {
                        navigateToSearchResult(query)
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
        (requireActivity() as? MainActivity)?.updateNavIcons(R.id.nav_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    // 검색 버튼을 클릭할 경우 검색 결과 화면으로 이동한다
    // 백엔드 서버로부터 병원 정보를 받는게 아니라 로컬에 저장된 json 파일을 이용한다
    private fun navigateToSearchResult(query: String) {

        // 검색 결과를 출력하는 프래그먼트에 데이터를 전달하기 위해 bundle에 데이터를 담는다
        // 검색 결과 프래그먼트에서는 arguments를 이용해서 아래의 데이터들을 불러올 수 있다
        val bundle = Bundle().apply {
            // 사용자가 검색한 키워드를 담는다
            putString("search_query", query)
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

    // 자동완성 리스트 업데이트
    private fun updateAutoComplete(names: List<String>) {
        val autoCompleteAdapter = AutoCompleteAdapter(requireContext(), names)
        binding.searchView.setAdapter(autoCompleteAdapter)
        binding.searchView.threshold = 1
    }
}