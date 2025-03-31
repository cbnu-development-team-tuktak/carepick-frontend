package com.example.carepick.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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

        // ✅ 병원 목록 가져오기 (CoroutineScope 사용)
        viewLifecycleOwner.lifecycleScope.launch {
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
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}