package com.example.carepick.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.carepick.R
import com.example.carepick.databinding.FragmentHospitalDetailBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment

class HospitalDetailFragment : Fragment() {

    private var _binding: FragmentHospitalDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHospitalDetailBinding.inflate(inflater, container, false)

        val name = arguments?.getString("name")
        val address = arguments?.getString("address")

        binding.hospitalDetailName.text = name ?: "데이터 없음"
        binding.hospitalDetailAddress.text = address ?: "데이터 없음"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fm = childFragmentManager
        var mapFragment = fm.findFragmentById(R.id.map) as? MapFragment

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance()
            fm.beginTransaction().add(R.id.map, mapFragment!!).commit()
        }

        mapFragment.getMapAsync { naverMap ->
            naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(37.5665, 126.9780)))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}