package com.example.lab5mobile.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lab5mobile.R
import com.example.lab5mobile.databinding.ItemFlightBinding
import com.example.lab5mobile.domain.model.Flight

class FlightListAdapter(
    private val onFavoriteClick: (Flight) -> Unit
) : ListAdapter<Flight, FlightListAdapter.FlightViewHolder>(FlightDiffCallback()) {

    var showDeparture: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FlightViewHolder(binding, onFavoriteClick)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        holder.bind(getItem(position), showDeparture)
    }

    class FlightViewHolder(
        private val binding: ItemFlightBinding,
        private val onFavoriteClick: (Flight) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(flight: Flight, showDeparture: Boolean) {
            val context = binding.root.context
            binding.flightNumberText.text = flight.flightNumber
            binding.destinationCodeText.text = flight.destinationCode
            binding.destinationNameText.text = "${flight.destinationCode} - ${flight.destinationName}"
            binding.departureText.text = if (showDeparture) {
                context.getString(R.string.departure_full, flight.departureCode, flight.departureName)
            } else {
                context.getString(R.string.departure_short, flight.departureCode)
            }
            binding.routeText.text = context.getString(
                R.string.route_format,
                flight.departureCode,
                flight.destinationCode
            )
            binding.favoriteButton.setImageResource(
                if (flight.isFavorite) {
                    R.drawable.ic_favorite_24
                } else {
                    R.drawable.ic_favorite_border_24
                }
            )
            binding.favoriteButton.contentDescription = if (flight.isFavorite) {
                context.getString(R.string.remove_from_favorites)
            } else {
                context.getString(R.string.add_to_favorites)
            }
            binding.favoriteButton.setOnClickListener {
                onFavoriteClick(flight)
            }
        }
    }

    private class FlightDiffCallback : DiffUtil.ItemCallback<Flight>() {
        override fun areItemsTheSame(oldItem: Flight, newItem: Flight): Boolean {
            return oldItem.routeId == newItem.routeId
        }

        override fun areContentsTheSame(oldItem: Flight, newItem: Flight): Boolean {
            return oldItem == newItem
        }
    }
}
