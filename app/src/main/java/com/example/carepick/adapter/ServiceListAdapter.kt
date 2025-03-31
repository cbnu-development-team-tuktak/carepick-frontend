package com.example.carepick.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.R
import com.example.carepick.databinding.ServiceCardBinding
import com.example.carepick.model.ServiceListData
import com.example.carepick.viewHolder.ServiceListViewHolder

class ServiceListAdapter(
    private val data: MutableList<ServiceListData>,
    private val activity: FragmentActivity
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ServiceListViewHolder(ServiceCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ServiceListViewHolder).binding
        val serviceData = data[position]

        binding.serviceText.text = serviceData.title
        binding.serviceIcon.setImageResource(serviceData.iconResId)

        // 특정 카드를 터치할 경우
        binding.root.setOnClickListener {
            val transaction = activity.supportFragmentManager.beginTransaction()
            // fragment_container(activity_main의 역할을 수행)에서 해당하는 서비스의 Fragment로 화면 전환
            transaction.replace(R.id.fragment_container, serviceData.fragment)
            // 뒤로 가기 버튼을 통해 이전 화면으로 돌아올 수 있다
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
}