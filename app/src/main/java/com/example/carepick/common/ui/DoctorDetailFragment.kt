package com.example.carepick.common.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.carepick.MainActivity
import com.example.carepick.R
import com.example.carepick.ui.selfDiagnosis.adapter.SpecialtyAdapter
import com.example.carepick.databinding.FragmentDoctorDetailBinding
import com.example.carepick.data.repository.DoctorRepository
import kotlinx.coroutines.launch

class DoctorDetailFragment: Fragment() {
    private var _binding: FragmentDoctorDetailBinding? = null
    private val binding get() = _binding!!

    private val doctorRepository = DoctorRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch{
            val doctorId = arguments?.getString("doctorId") ?: error("Hospital ID is required")

            val doctor = doctorRepository.getDoctorById(doctorId)

            Log.d("doctorInfo", "$doctor")

            // 의사 이름 데이터를 넣는다
            binding.doctorDetailName.text = doctor?.name ?: "데이터 없음"
            // 의사 경력 데이터를 넣는다
            // 지금은 경력 중 첫번째만 넣는 중
            // TODO("의사의 모든 경력을 출력하도록 수정해야 함")
            // TODO("자격면허를 출력하도록 수정해야 함")

            binding.doctorDetailCareerList.text = doctor?.careers?.firstOrNull() ?: "데이터 없음"
            // 의사가 속한 병원 이름을 넣는다
            binding.doctorDetailHospitalName.text = doctor?.hospitalName
            // 의사 이미지를 넣는다
            Glide.with(binding.root)
                .load(doctor?.profileImage)
                .placeholder(R.drawable.sand_clock)
                .error(R.drawable.doctor_placeholder)
                .into(binding.doctorDetailImage)

            // 진료과를 동적으로 넣는다
            val specialtyAdapter = doctor?.specialties?.let { SpecialtyAdapter(it) }
            binding.doctorDetailSpecialties.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            binding.doctorDetailSpecialties.adapter = specialtyAdapter
        }

        // include된 헤더 내의 뒤로가기 버튼
        val backButton = view.findViewById<ImageButton>(R.id.btn_back)
        backButton.setOnClickListener {
            val manager = requireActivity().supportFragmentManager
            if (manager.backStackEntryCount > 0) {
                manager.popBackStack()
            } else {
                requireActivity().finish() // or moveTaskToBack(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? MainActivity)?.updateNavIcons(-1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}