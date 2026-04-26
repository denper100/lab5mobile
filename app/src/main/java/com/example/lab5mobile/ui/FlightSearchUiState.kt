package com.example.lab5mobile.ui

import com.example.lab5mobile.domain.model.Airport
import com.example.lab5mobile.domain.model.Flight

data class FlightSearchUiState(
    val query: String = "",
    val suggestions: List<Airport> = emptyList(),
    val selectedAirport: Airport? = null,
    val flights: List<Flight> = emptyList(),
    val isShowingFavorites: Boolean = true,
    val isLoading: Boolean = true,
    val emptyMessage: String? = null,
    val errorMessage: String? = null
)
