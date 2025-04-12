package com.example.carepick.ui.doctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.carepick.MainActivity
import com.example.carepick.R
import com.example.carepick.adapter.SpecialtyAdapter
import com.example.carepick.databinding.FragmentDoctorDetailBinding

class DoctorDetailFragment: Fragment() {
    private var _binding: FragmentDoctorDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorDetailBinding.inflate(inflater, container, false)

        val name = arguments?.getString("name")
        val profileImage = arguments?.getString("profileImage")
        val career = arguments?.getString("career") ?: "경력 정보 없음"
        val specialty = arguments?.getString("specialty") ?: ""
        val specialtyArray= specialty.split(",").map { it.trim() } // 공백 제거 포함
        val educationLicense = arguments?.getStringArrayList("educationLicense")

        // 의사 이름 데이터를 넣는다
        binding.doctorDetailName.text = name ?: "데이터 없음"
        // 의사 경력 데이터를 넣는다
        binding.doctorDetailCareerList.text = career
        // 의사 이미지를 넣는다
        Glide.with(binding.root)
            .load(profileImage)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.warning)
            .into(binding.doctorDetailImage)

        // 진료과를 동적으로 넣는다
        val specialtyAdapter = SpecialtyAdapter(specialtyArray)
        binding.doctorDetailSpecialties.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.doctorDetailSpecialties.adapter = specialtyAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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