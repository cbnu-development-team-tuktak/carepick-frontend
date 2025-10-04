package com.example.carepick.common.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.carepick.databinding.FragmentHospitalListBinding

// 서비스 목록-병원 목록에 들어가면 나오는 화면에 대한 코드
// fragment_hospital_list.xml을 객체화하는 코드
// 아직 구현을 시작하지 않음
class HospitalListFragment: Fragment() {

    private var _binding: FragmentHospitalListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHospitalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}