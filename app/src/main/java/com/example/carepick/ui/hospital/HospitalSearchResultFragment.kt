package com.example.carepick.ui.hospital

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carepick.adapter.HospitalSearchListAdapter
import com.example.carepick.databinding.FragmentHospitalSearchResultBinding
import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.repository.HospitalRepository
import kotlinx.coroutines.launch

class HospitalSearchResultFragment: Fragment() {

    private var _binding: FragmentHospitalSearchResultBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHospitalSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = arguments?.getString("search_query") ?: return
        val hospitals = arguments?.getParcelableArrayList<HospitalDetailsResponse>("hospitals") ?: return

        val filteredHospitals = hospitals.filter { hospital ->
            hospital.name.contains(query, ignoreCase = true)
        }.toMutableList()

        // RecyclerView에 필터된 병원 정보 세팅
        binding.hospitalSearchResultRecyclerView.adapter = HospitalSearchListAdapter(filteredHospitals, requireActivity())
        binding.hospitalSearchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

//        val query = arguments?.getString("search_query") ?: return
//
//        lifecycleScope.launch {
//            val hospitals = hospitalRepository.fetchHospitals()
//
//            // 병원 정보 전체에서 이름을 정리하고 query로 필터링
//            val filteredHospitals = hospitals.filter { hospital ->
//                val cleanedName = hospital.name.replace(Regex("""^["'(【\[].*?["')】\]]\s*"""), "")
//                cleanedName.contains(query, ignoreCase = true)
//            }.toMutableList()
//
//            Log.e("Searched Hospital", "$filteredHospitals.size")
//
//            // RecyclerView에 필터된 병원 정보 세팅
//            binding.recyclerView.adapter = HospitalSearchListAdapter(filteredHospitals, requireActivity())
//            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}