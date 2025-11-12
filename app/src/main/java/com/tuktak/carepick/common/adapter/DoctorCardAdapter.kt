package com.tuktak.carepick.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.tuktak.carepick.databinding.DoctorCardBinding
import com.tuktak.carepick.data.model.DoctorDetailsResponse
import com.tuktak.carepick.ui.search.result.doctor.DoctorCardViewHolder

//
class DoctorCardAdapter(
    private val doctors: List<DoctorDetailsResponse>,
    private val onItemClicked: (DoctorDetailsResponse) -> Unit // 👈 Fragment에서 전달받은 람다
) : RecyclerView.Adapter<DoctorCardViewHolder>() {

    // ✅ doctors 리스트의 크기를 반환
    override fun getItemCount(): Int = doctors.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorCardViewHolder {
        val binding = DoctorCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // ✅ activity 전달 없이 ViewHolder 생성
        return DoctorCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoctorCardViewHolder, position: Int) {
        val doctor = doctors[position]

        // 1. ViewHolder에 데이터를 바인딩
        holder.bind(doctor)

        // 2. ✅ Adapter에서 클릭 리스너를 설정하고, 람다를 호출
        holder.itemView.setOnClickListener {
            onItemClicked(doctor)
        }
    }
}