package com.example.carepick.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.carepick.MainActivity
import com.example.carepick.R
import com.example.carepick.databinding.FragmentUserProfileBinding

class UserProfileFragment: Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? MainActivity)?.updateNavIcons(R.id.nav_profile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}