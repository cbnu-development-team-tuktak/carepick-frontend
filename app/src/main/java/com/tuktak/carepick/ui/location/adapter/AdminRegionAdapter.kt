package com.tuktak.carepick.ui.location.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tuktak.carepick.databinding.ItemAdminBackBinding
import com.tuktak.carepick.databinding.ItemAdminRegionBinding
import com.tuktak.carepick.ui.location.model.AdminRegion
import com.tuktak.carepick.ui.location.model.BackItem
import com.tuktak.carepick.ui.location.model.GridItem

// ✨ 어댑터가 두 가지 클릭 이벤트를 처리하도록 수정
class AdminRegionAdapter(
    private val onRegionClick: (AdminRegion) -> Unit,
    private val onBackClick: () -> Unit
) : ListAdapter<GridItem, RecyclerView.ViewHolder>(AdminRegionDiffCallback()) {

    // ✨ 뷰 타입을 구분하기 위한 상수
    private companion object {
        const val VIEW_TYPE_REGION = 1
        const val VIEW_TYPE_BACK = 2
    }

    // ✨ 아이템 종류에 따라 뷰 타입 반환
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AdminRegion -> VIEW_TYPE_REGION
            is BackItem -> VIEW_TYPE_BACK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // ✨ 뷰 타입에 따라 다른 ViewHolder 생성
        return when (viewType) {
            VIEW_TYPE_REGION -> {
                val binding = ItemAdminRegionBinding.inflate(inflater, parent, false)
                RegionViewHolder(binding)
            }
            else -> { // VIEW_TYPE_BACK
                val binding = ItemAdminBackBinding.inflate(inflater, parent, false)
                BackViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        // ✨ ViewHolder 종류에 따라 다른 bind 함수 호출
        when (holder) {
            is RegionViewHolder -> holder.bind(item as AdminRegion, onRegionClick)
            is BackViewHolder -> holder.bind(onBackClick)
        }
    }

    // --- ViewHolder 클래스들 ---
    class RegionViewHolder(private val binding: ItemAdminRegionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AdminRegion, onRegionClick: (AdminRegion) -> Unit) {
            binding.regionName.text = item.name
            itemView.setOnClickListener { onRegionClick(item) }
        }
    }

    class BackViewHolder(binding: ItemAdminBackBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(onBackClick: () -> Unit) {
            itemView.setOnClickListener { onBackClick() }
        }
    }
}

class AdminRegionDiffCallback : DiffUtil.ItemCallback<GridItem>() {
    override fun areItemsTheSame(oldItem: GridItem, newItem: GridItem): Boolean {
        // 아이템의 내용으로 고유성 비교
        return when {
            oldItem is AdminRegion && newItem is AdminRegion -> oldItem.name == newItem.name
            oldItem is BackItem && newItem is BackItem -> true
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: GridItem, newItem: GridItem): Boolean {
        return oldItem == newItem
    }
}