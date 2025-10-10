package com.example.carepick.ui.search.filter.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.R

class SpecialtyAdapter(
    private val specialties: List<String>,
    private val selectedSpecialties: MutableSet<String>
) : RecyclerView.Adapter<SpecialtyAdapter.SpecialtyViewHolder>(){
    inner class SpecialtyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val buttonText: TextView = itemView.findViewById(R.id.specialty_button_text)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val specialty = specialties[position]
                    // 선택된 목록에 있으면 제거, 없으면 추가
                    if (selectedSpecialties.contains(specialty)) {
                        selectedSpecialties.remove(specialty)
                    } else {
                        selectedSpecialties.add(specialty)
                    }
                    // UI 갱신을 위해 해당 아이템만 변경 알림
                    notifyItemChanged(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialtyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_specialty_button, parent, false)
        return SpecialtyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecialtyViewHolder, position: Int) {
        val specialty = specialties[position]
        holder.buttonText.text = specialty
        // 선택된 목록에 포함되어 있는지 여부에 따라 아이템의 selected 상태 변경
        holder.itemView.isSelected = selectedSpecialties.contains(specialty)
    }

    override fun getItemCount() = specialties.size
}