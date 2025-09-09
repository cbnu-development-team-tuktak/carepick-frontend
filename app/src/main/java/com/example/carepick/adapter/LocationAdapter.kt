package com.example.carepick.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.databinding.ItemAddressBinding
import com.example.carepick.model.AddressDoc

class LocationAdapter(
    private val onClick: (AddressDoc) -> Unit
) : RecyclerView.Adapter<LocationAdapter.VH>()  {
    private val data = mutableListOf<AddressDoc>()

    fun submit(list: List<AddressDoc>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemAddressBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAddressBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(b)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        val jibun = item.address?.let { it.address_name } ?: item.address_name.orEmpty()
        val road  = item.road_address?.address_name.orEmpty()

        holder.b.txtPrimary.text = if (road.isNotBlank()) road else jibun
        holder.b.txtSecondary.text = if (road.isNotBlank() && jibun.isNotBlank()) jibun else ""

        holder.itemView.setOnClickListener { onClick(item) }
    }
}