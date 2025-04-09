package com.example.carepick.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.databinding.DoctorCardBinding
import com.example.carepick.dto.doctor.DoctorDetailsResponse
import com.example.carepick.ui.doctor.DoctorDetailFragment
import com.example.carepick.viewHolder.DoctorCardViewHolder

//
class DoctorCardAdapter(
    private val items: List<DoctorDetailsResponse>,
    private val activity: FragmentActivity
) : RecyclerView.Adapter<DoctorCardViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorCardViewHolder {
        val binding = DoctorCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoctorCardViewHolder(binding, activity)
    }

    override fun onBindViewHolder(holder: DoctorCardViewHolder, position: Int) {
        holder.bind(items[position])
    }
}