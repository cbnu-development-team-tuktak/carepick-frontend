package com.tuktak.carepick.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.tuktak.carepick.databinding.DoctorCardBinding
import com.tuktak.carepick.data.model.DoctorDetailsResponse
import com.tuktak.carepick.ui.search.result.DoctorCardViewHolder

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