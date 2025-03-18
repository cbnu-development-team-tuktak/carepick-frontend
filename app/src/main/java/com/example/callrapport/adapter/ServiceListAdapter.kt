package com.example.callrapport.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.callrapport.databinding.ServiceCardBinding
import com.example.callrapport.model.ServiceListData
import com.example.callrapport.viewHolder.ServiceListViewHolder

class ServiceListAdapter(val datas: MutableList<ServiceListData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = datas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ServiceListViewHolder(ServiceCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ServiceListViewHolder).binding
        val serviceData = datas[position]

        binding.serviceText.text = serviceData.title
        binding.serviceIcon.setImageResource(serviceData.iconResId)
    }
}