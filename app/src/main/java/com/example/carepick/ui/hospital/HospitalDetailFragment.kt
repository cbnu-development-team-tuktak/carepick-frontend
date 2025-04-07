package com.example.carepick.ui.hospital

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.adapter.DoctorListAdapter
import com.example.carepick.databinding.FragmentHospitalDetailBinding
import com.example.carepick.dto.ImageResponse
import com.example.carepick.dto.doctor.DoctorDetailsResponse
import com.example.carepick.dto.hospital.HospitalAdditionalInfo
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
        // fragment_hospital_detail.xml을 객체화하여 _binding 객체에 저장한다
        // 이를 통해 레이아웃의 텍스트를 변경하는 등 속성을 수정할 수 있다
        _binding = FragmentHospitalDetailBinding.inflate(inflater, container, false)

        // bundle이 전달해준 데이터를 받아서 변수에 저장한다
        val name = arguments?.getString("name")
        val address = arguments?.getString("address")
        val phoneNumber = arguments?.getString("phoneNumber")
        val operatingHours = arguments?.getString("operatingHours")
        val images = arguments?.getParcelableArrayList<ImageResponse>("images")?.firstOrNull()?.url ?: ""
        val addInfo = arguments?.getParcelable<HospitalAdditionalInfo>("additionalInfo")

        // 레이아웃에 데이터를 바인딩한다
        // 이때 데이터가 존재하지 않으면 "데이터 없음" 으로 데이터를 넣는다
        binding.hospitalDetailName.text = name ?: "데이터 없음"
        binding.hospitalDetailAddress.text = address ?: "데이터 없음"
        binding.hospitalDetailPhone.text = phoneNumber ?: "데이터 없음"
        binding.hospitalDetailTime.text = operatingHours ?: "데이터 없음"
        // 이미지 url을 받아서 로드한다
        Glide.with(binding.root)
            .load(images)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.warning)
            .into(binding.hospitalDetailImage)

        // 병원 추가 정보 표시
        if (addInfo != null) {
            addInfoCheck(requireContext(), binding, addInfo) // 추가 정보가 비어있지 않다면 추가 정보 중 true 값인 것을 찾아서 텍스트 색을 바꾼다
        }

        // 여기서는 의사 목록을 동적으로 생성한다
//        viewLifecycleOwner.lifecycleScope.launch {
//            try {
//                val doctorIds = arguments?.getStringArrayList("doctors") ?: return@launch
//                val doctorList = DoctorRepository().getDoctorsByIds(doctorIds)
//
//                binding.doctorListRecyclerView.adapter = DoctorListAdapter(doctorList, requireActivity())
//            } catch (e: Exception) {
//                Log.e("DoctorFetchError", "의사 목록 불러오기 실패", e)
//                // TODO: 사용자에게 에러 메시지 표시할 수도 있음
//            }
//
//            // 의사 카드들은 좌우 스크롤을 할 수 있도록 수평 배치한다
//            binding.doctorListRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//        }

        // 여기서는 의사 목록을 동적으로 생성한다
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val doctors = arguments?.getParcelableArrayList<DoctorDetailsResponse>("doctors") ?: arrayListOf()
                binding.doctorListRecyclerView.adapter = DoctorListAdapter(doctors, requireActivity())
            } catch (e: Exception) {
                Log.e("DoctorFetchError", "의사 목록 불러오기 실패", e)
                // TODO: 사용자에게 에러 메시지 표시할 수도 있음
            }

            // 의사 카드들은 좌우 스크롤을 할 수 있도록 수평 배치한다
            binding.doctorListRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        return binding.root
    }

    // 뷰가 생성되면 지도 화면에서 병원 위치로 이동하고 병원 자리에 마커를 생성한다
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