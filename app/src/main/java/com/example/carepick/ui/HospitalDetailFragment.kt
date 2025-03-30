package com.example.carepick.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.adapter.DoctorListAdapter
import com.example.carepick.databinding.FragmentHospitalDetailBinding
import com.example.carepick.dto.HospitalAdditionalInfo
import com.example.carepick.repository.DoctorRepository
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch

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
        val phoneNumber = arguments?.getString("phoneNumber")
        val operatingHours = arguments?.getString("operatingHours")
        val imageUrl = arguments?.getString("imageUrl")
        val addInfo = arguments?.getParcelable<HospitalAdditionalInfo>("additionalInfo")

        binding.hospitalDetailName.text = name ?: "데이터 없음"
        binding.hospitalDetailAddress.text = address ?: "데이터 없음"
        binding.hospitalDetailPhone.text = phoneNumber ?: "데이터 없음"
        binding.hospitalDetailTime.text = operatingHours ?: "데이터 없음"
        Glide.with(binding.root)
            .load(imageUrl)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.warning)
            .into(binding.hospitalDetailImage)

        // 병원 추가 정보 표시
        if (addInfo != null) {
            addInfoCheck(requireContext(), binding, addInfo)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val doctorIds = arguments?.getStringArrayList("doctors") ?: return@launch
                val doctorList = DoctorRepository().getDoctorsByIds(doctorIds)

                binding.doctorListRecyclerView.adapter = DoctorListAdapter(doctorList, requireActivity())
            } catch (e: Exception) {
                Log.e("DoctorFetchError", "의사 목록 불러오기 실패", e)
                // TODO: 사용자에게 에러 메시지 표시할 수도 있음
            }

            binding.doctorListRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // 2열
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fm = childFragmentManager
        var mapFragment = fm.findFragmentById(R.id.map) as? MapFragment
        val latitude = arguments?.getDouble("latitude") ?: 0.0
        val longitude = arguments?.getDouble("longitude") ?: 0.0

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance()
            fm.beginTransaction().add(R.id.map, mapFragment!!).commit()
        }

        mapFragment.getMapAsync { naverMap ->
            val location = LatLng(latitude, longitude)

            // 병원 위치로 이동
            naverMap.moveCamera(CameraUpdate.scrollTo(location))

            // 마커 생성 및 지도에 추가
            val marker = Marker()
            marker.position = location
            marker.map = naverMap
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}