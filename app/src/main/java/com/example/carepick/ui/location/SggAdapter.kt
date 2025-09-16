package com.example.carepick.ui.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.databinding.ItemSidoBinding
import com.example.carepick.model.Sgg

class SggAdapter(
    private val currentSido: () -> String,
    private val onBackClick: () -> Unit,
    private val onItemClick: (Sgg) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_BACK = 0
        private const val TYPE_ITEM = 1
    }

    private val items = mutableListOf<Sgg>()

    fun submit(list: List<Sgg>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        if (position == 0) TYPE_BACK else TYPE_ITEM

    override fun getItemCount(): Int = items.size + 1 // +1 = 뒤로가기

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        val binding = ItemSidoBinding.inflate(inf, parent, false)
        return if (viewType == TYPE_BACK) BackVH(binding) else ItemVH(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_BACK) {
            (holder as BackVH).bind("◀ ${currentSido()} (시/도로 돌아가기)")
        } else {
            val item = items[position - 1]
            (holder as ItemVH).bind(item)
        }
    }

    inner class BackVH(private val binding: ItemSidoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.btnSido.text = text
            binding.btnSido.setOnClickListener { onBackClick() }
        }
    }

    inner class ItemVH(private val binding: ItemSidoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Sgg) {
            binding.btnSido.text = item.name
            binding.btnSido.setOnClickListener { onItemClick(item) }
        }
    }
}
