package com.example.carepick.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.databinding.HospitalCardBinding
import com.example.carepick.dto.hospital.HospitalDetailsResponse
import com.example.carepick.viewHolder.HospitalCardViewHolder

// 병원 카드와 데이터를 연결하는 어댑터
// hospital_card.xml 레이아웃과 병원 정보(HospitalDetailResponse)를 연결함을 나타낸다
// hospital_card.xml 레이아웃 어디에 어떤 데이터가 들어가는지는 HospitalListViewHolder에 나타나있다
class HospitalCardAdapter(
    private val items: MutableList<HospitalDetailsResponse>,
    private val activity: FragmentActivity
): RecyclerView.Adapter<HospitalCardViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalCardViewHolder {
        val binding = HospitalCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HospitalCardViewHolder(binding, activity)
    }
    override fun onBindViewHolder(holder: HospitalCardViewHolder, position: Int) {
        holder.bind(items[position])
    }

}