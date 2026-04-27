package com.example.lab5mobile.domain.repository

import com.example.lab5mobile.domain.model.Airport
import com.example.lab5mobile.domain.model.Flight
import kotlinx.coroutines.flow.Flow

interface FlightSearchRepository {
    fun observeAirportSuggestions(query: String): Flow<List<Airport>>
    suspend fun getAirportByCode(iataCode: String): Airport?
    fun observeFlightsFromAirport(departureCode: String): Flow<List<Flight>>
    fun observeFavoriteFlights(): Flow<List<Flight>>
    suspend fun toggleFavorite(departureCode: String, destinationCode: String)
    val lastSavedSearchCode: Flow<String?>
    suspend fun saveLastSearchCode(iataCode: String?)
}
