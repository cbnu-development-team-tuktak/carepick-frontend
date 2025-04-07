package com.example.carepick.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.R

class SpecialtyAdapterNoBg(
    private val specialties: List<String>
) : RecyclerView.Adapter<SpecialtyAdapterNoBg.SpecialtyNoBgViewHolder>() {

    class SpecialtyNoBgViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialtyNoBgViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.specialty_item_no_bg, parent, false) as TextView
        return SpecialtyNoBgViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecialtyNoBgViewHolder, position: Int) {
        holder.view.text = specialties[position]
    }

    override fun getItemCount(): Int = specialties.size
}