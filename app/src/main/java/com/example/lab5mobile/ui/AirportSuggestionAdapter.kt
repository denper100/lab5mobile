package com.example.lab5mobile.ui

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.example.lab5mobile.domain.model.Airport

class AirportSuggestionAdapter(context: Context) :
    ArrayAdapter<Airport>(context, android.R.layout.simple_dropdown_item_1line) {

    private val items = mutableListOf<Airport>()

    fun submitList(newItems: List<Airport>) {
        items.clear()
        items.addAll(newItems)
        clear()
        addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Airport = items[position]

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults().apply {
                    values = items
                    count = items.size
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }
}
