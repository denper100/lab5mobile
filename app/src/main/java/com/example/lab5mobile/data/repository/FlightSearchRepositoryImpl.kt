package com.example.lab5mobile.data.repository

import com.example.lab5mobile.data.local.dao.AirportDao
import com.example.lab5mobile.data.local.dao.FavoriteFlightDao
import com.example.lab5mobile.data.local.entity.FavoriteFlightEntity
import com.example.lab5mobile.data.local.model.FlightRouteLocal
import com.example.lab5mobile.data.preferences.SearchPreferences
import com.example.lab5mobile.domain.model.Airport
import com.example.lab5mobile.domain.model.Flight
import com.example.lab5mobile.domain.repository.FlightSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlightSearchRepositoryImpl(
    private val airportDao: AirportDao,
    private val favoriteFlightDao: FavoriteFlightDao,
    private val searchPreferences: SearchPreferences
) : FlightSearchRepository {

    override fun observeAirportSuggestions(query: String): Flow<List<Airport>> {
        val trimmed = query.trim()
        return airportDao.observeAirportSuggestions(
            query = trimmed,
            prefix = trimmed.uppercase()
        ).map { airports -> airports.map { it.toDomain() } }
    }

    override suspend fun getAirportByCode(iataCode: String): Airport? {
        return airportDao.getAirportByCode(iataCode.trim())?.toDomain()
    }

    override fun observeFlightsFromAirport(departureCode: String): Flow<List<Flight>> {
        return airportDao.observeFlightsFromAirport(departureCode.trim().uppercase())
            .map { routes -> routes.map { it.toDomain() } }
    }

    override fun observeFavoriteFlights(): Flow<List<Flight>> {
        return favoriteFlightDao.observeFavoriteFlights()
            .map { routes -> routes.map { it.toDomain() } }
    }

    override suspend fun toggleFavorite(departureCode: String, destinationCode: String) {
        val normalizedDeparture = departureCode.trim().uppercase()
        val normalizedDestination = destinationCode.trim().uppercase()
        if (favoriteFlightDao.isFavorite(normalizedDeparture, normalizedDestination)) {
            favoriteFlightDao.deleteFavorite(normalizedDeparture, normalizedDestination)
        } else {
            favoriteFlightDao.insertFavorite(
                FavoriteFlightEntity(
                    departureCode = normalizedDeparture,
                    destinationCode = normalizedDestination
                )
            )
        }
    }

    override val lastSavedSearchCode: Flow<String?> = searchPreferences.lastSearchCode

    override suspend fun saveLastSearchCode(iataCode: String?) {
        searchPreferences.saveLastSearchCode(iataCode?.trim()?.uppercase())
    }
}

private fun com.example.lab5mobile.data.local.entity.AirportEntity.toDomain(): Airport {
    return Airport(
        id = id,
        iataCode = iataCode,
        name = name,
        passengers = passengers
    )
}

private fun FlightRouteLocal.toDomain(): Flight {
    return Flight(
        routeId = routeId,
        departureCode = departureCode,
        departureName = departureName,
        destinationCode = destinationCode,
        destinationName = destinationName,
        isFavorite = isFavorite
    )
}
