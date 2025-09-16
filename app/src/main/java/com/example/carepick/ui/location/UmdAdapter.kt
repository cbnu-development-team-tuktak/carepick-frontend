package com.example.carepick.ui.location

// UmdAdapter.kt
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.databinding.ItemSidoBinding
import com.example.carepick.model.Umd

class UmdAdapter(
    private val currentSgg: () -> String,
    private val onBackClick: () -> Unit,
    private val onItemClick: (Umd) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_BACK = 0
        private const val TYPE_ITEM = 1
    }

    private val items = mutableListOf<Umd>()

    fun submit(list: List<Umd>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size + 1
    override fun getItemViewType(position: Int): Int =
        if (position == 0) TYPE_BACK else TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        val binding = ItemSidoBinding.inflate(inf, parent, false)
        return if (viewType == TYPE_BACK) BackVH(binding) else ItemVH(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_BACK) {
            (holder as BackVH).bind("◀ ${currentSgg()} (시/군/구로 돌아가기)")
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
        fun bind(item: Umd) {
            binding.btnSido.text = item.name
            binding.btnSido.setOnClickListener { onItemClick(item) }
        }
    }
}
