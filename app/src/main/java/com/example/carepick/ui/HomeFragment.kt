package com.example.carepick.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.databinding.FragmentHomeBinding
import com.example.carepick.repository.HospitalListRepository
import com.example.carepick.repository.ServiceListRepository
import com.example.carepick.adapter.HospitalListAdapter
import com.example.carepick.adapter.ServiceListAdapter

// 홈화면의 기능을 구현한 Fragment
class HomeFragment: Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 서비스 목록 카드뷰와 병원 목록 카드뷰를 생성하기 위한 각각의 리포지토리
    private val serviceListRepository = ServiceListRepository()
    private val hospitalListRepository = HospitalListRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 홈화면은 fragment_home.xml을 사용할 것임을 나타내는 코드
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 리포지토리로부터 카드뷰에 들어갈 텍스트와 정보들을 받는다
        val serviceList = serviceListRepository.getServiceList()
        val hospitalList = hospitalListRepository.getHospitalList()

        // 카드뷰를 수평으로 배치하도록 HORIZONTAL 옵션을 준다
        binding.serviceListRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.hospitalListRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // 리포지토리로부터 받아온 정보를 토대로 카드뷰들을 동적으로 생성한다
        binding.serviceListRecyclerView.adapter = ServiceListAdapter(serviceList, requireActivity())
        binding.hospitalListRecyclerView.adapter = HospitalListAdapter(hospitalList, requireActivity())

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}