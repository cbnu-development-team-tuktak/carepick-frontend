package com.example.carepick.ui.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.databinding.ItemSidoBinding
import com.example.carepick.model.Sido

class SidoAdapter(
    private val onClick: (Sido) -> Unit
) : RecyclerView.Adapter<SidoAdapter.VH>() {

    private val items = mutableListOf<Sido>()

    fun submit(list: List<Sido>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(private val binding: ItemSidoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Sido) {
            binding.btnSido.text = item.name
            binding.btnSido.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSidoBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size
}