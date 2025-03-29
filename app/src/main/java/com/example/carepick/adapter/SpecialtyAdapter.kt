package com.example.carepick.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.R

// 특정 진료과 텍스트 정보를 받아서 그걸 specialty_background 아이콘에 넣어준다
class SpecialtyAdapter(
    private val specialties: List<String>
) : RecyclerView.Adapter<SpecialtyAdapter.SpecialtyViewHolder>() {

    class SpecialtyViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialtyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.specialty_item, parent, false) as TextView
        return SpecialtyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecialtyViewHolder, position: Int) {
        holder.view.text = specialties[position]
    }

    override fun getItemCount(): Int = specialties.size
}