package com.example.carepick.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.adapter.AutoCompleteAdapter
import com.example.carepick.databinding.FragmentHomeBinding
import com.example.carepick.repository.ServiceListRepository
import com.example.carepick.adapter.HospitalListAdapter
import com.example.carepick.adapter.ServiceListAdapter
import com.example.carepick.repository.HospitalRepository
import kotlinx.coroutines.launch

// 홈화면의 기능을 구현한 Fragment
class HomeFragment: Fragment() {

    // fragment_home.xml 을 객체처럼 취급하여 binding 변수에 저장할 것임을 선언한다
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 서비스 목록 카드뷰와 병원 목록 카드뷰를 생성하기 위한 각각의 리포지토리
    private val serviceListRepository = ServiceListRepository()
    private val hospitalRepository = HospitalRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 홈화면은 fragment_home.xml을 사용할 것임을 나타내는 코드
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // ✅ 기본적으로 네트워크 오류 화면 숨김
        binding.networkErrorView.root.visibility = View.GONE
        binding.hospitalListRecyclerView.visibility = View.GONE

        // 리포지토리로부터 카드뷰에 들어갈 텍스트와 정보들을 받는다
        val serviceList = serviceListRepository.getServiceList()
        // 카드뷰를 수평으로 배치하도록 HORIZONTAL 옵션을 준다
        binding.serviceListRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        // 리포지토리로부터 받아온 정보를 토대로 카드뷰들을 동적으로 생성한다
        binding.serviceListRecyclerView.adapter = ServiceListAdapter(serviceList, requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            // <<병원 카드를 동적으로 생성하는 파트>>
            val hospitalList = hospitalRepository.fetchHospitals()

            if (hospitalList.isEmpty()) {
                // ❌ 네트워크 오류 발생 시 오류 화면 표시
                binding.networkErrorView.root.visibility = View.VISIBLE
                binding.hospitalListRecyclerView.visibility = View.GONE
            } else {
                // ✅ 데이터 성공적으로 가져왔을 때 RecyclerView 표시
                binding.hospitalListRecyclerView.visibility = View.VISIBLE
                binding.networkErrorView.root.visibility = View.GONE
                binding.hospitalListRecyclerView.adapter = HospitalListAdapter(hospitalList, requireActivity())
                binding.hospitalListRecyclerView.setHasFixedSize(true)

                // 병원 정보를 몇개 가져왔는지 확인하기 위한 코드
                val itemCount = HospitalListAdapter(hospitalList, requireActivity()).getItemCount()
                Log.e("Item Count", "$itemCount")
            }

            // 병원 카드는 좌우 스크롤을 할 수 있도록 배치된다
            binding.hospitalListRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


            // <<검색창에서 병원 이름을 자동 완성하는 부분>>
            val rawNames = hospitalList.map { it.name }
            // 병원 이름에서 괄호나 따음표로 감싸져있는 부분은 제거
            val hospitalNames = rawNames.map { name ->
                name.replace(Regex("""^["'(【\[].*?["')】\]]\s*"""), "")
            }

            Log.d("AutoComplete", "names: $hospitalNames")

            // 어댑터를 불러와서 부분적으로 일치하는 병원 이름을 출력하도록 함
            val autoCompleteAdapter = AutoCompleteAdapter(requireContext(), hospitalNames)
            binding.searchView.setAdapter(autoCompleteAdapter)
            // 한 문자만 입력해도 자동완성이 되도록 함
            binding.searchView.threshold = 3

            // 자동완성 항목 클릭 시 검색 창에 해당 병원 이름이 들어가도록 함
            binding.searchView.setOnItemClickListener { parent, _, position, _ ->
                val selectedName = parent.getItemAtPosition(position).toString()
                binding.searchView.setText(selectedName)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}