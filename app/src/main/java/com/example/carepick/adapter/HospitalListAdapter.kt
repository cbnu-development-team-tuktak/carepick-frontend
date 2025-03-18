package com.example.carepick.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.databinding.HospitalCardBinding
import com.example.carepick.model.HospitalListData
import com.example.carepick.viewHolder.HospitalListViewHolder

class HospitalListAdapter(val datas: MutableList<HospitalListData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = datas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        HospitalListViewHolder(HospitalCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as HospitalListViewHolder).binding
        val hospitalData = datas[position]

        binding.hospitalName.text = hospitalData.name
        binding.hospitalAddress.text = hospitalData.address
        binding.hospitalPicture.setImageResource(hospitalData.imageResId)
    }
}