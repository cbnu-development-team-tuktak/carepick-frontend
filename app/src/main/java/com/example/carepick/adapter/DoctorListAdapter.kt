package com.example.carepick.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.databinding.DoctorCardBinding
import com.example.carepick.dto.doctor.DoctorDetailsResponse
import com.example.carepick.viewHolder.DoctorListViewHolder

class DoctorListAdapter(
    private val data: List<DoctorDetailsResponse>,
    private val activity: FragmentActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        DoctorListViewHolder(DoctorCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as DoctorListViewHolder).binding

        // 의사 정보를 담는 객체
        val doctorData = data[position]

        binding.doctorName.text = doctorData.name
        // 카드에 이미지를 url을 통해서 집어넣는다
        Glide.with(binding.root)
            .load(doctorData.profileImage)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.warning)
            .into(binding.doctorImage)

        // 카드를 선택했을 때 다음의 동작들을 수행한다
        binding.root.setOnClickListener {
            // TODO: 의사 상세 페이지로 넘어가도록 구현해야 함
        }
    }
}