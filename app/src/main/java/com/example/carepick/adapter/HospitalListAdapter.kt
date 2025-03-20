package com.example.carepick.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.R
import com.example.carepick.databinding.HospitalCardBinding
import com.example.carepick.model.HospitalListData
import com.example.carepick.ui.HospitalDetailFragment
import com.example.carepick.viewHolder.HospitalListViewHolder

class HospitalListAdapter(
    private val datas: MutableList<HospitalListData>,
    private val activity: FragmentActivity
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = datas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        HospitalListViewHolder(HospitalCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as HospitalListViewHolder).binding
        val hospitalData = datas[position]

        binding.hospitalCardName.text = hospitalData.name
        binding.hospitalCardAddress.text = hospitalData.address
        binding.hospitalPicture.setImageResource(hospitalData.imageResId)

        binding.root.setOnClickListener {
            // 병원 카드를 선택하면 전환될 Fragment 지정
            val hospitalDetailFragment = HospitalDetailFragment()

            // 병원 상세 정보 Fragment에게 전달할 매개변수를 bundle에 담는다
            val bundle = Bundle().apply {
                putString("name", hospitalData.name)
                putString("address", hospitalData.address)
            }
            hospitalDetailFragment.arguments = bundle

            val transaction = activity.supportFragmentManager.beginTransaction()
            // 원래는 R.id.content 대신에 activity_main이 들어가야
            transaction.replace(R.id.fragment_container, hospitalDetailFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
}