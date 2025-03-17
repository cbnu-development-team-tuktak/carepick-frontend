package com.example.callrapport.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.callrapport.databinding.ServiceCardBinding
import com.example.callrapport.viewHolder.ServiceViewHolder

class serviceAdapter(val datas: MutableList<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = datas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ServiceViewHolder(ServiceCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ServiceViewHolder).binding
        binding.textView2.text = datas[position]
    }
}