package com.example.carepick.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class AutoCompleteAdapter(
    context: Context,
    private val fullList: List<String>
) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, fullList.toMutableList()) {
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (!constraint.isNullOrEmpty()) {
                    val filtered = fullList.filter {
                        it.contains(constraint, ignoreCase = true)
                    }
                    results.values = filtered
                    results.count = filtered.size
                } else {
                    results.values = fullList
                    results.count = fullList.size
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                clear()
                if (results?.values != null) {
                    @Suppress("UNCHECKED_CAST")
                    addAll(results.values as List<String>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}