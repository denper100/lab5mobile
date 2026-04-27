package com.example.lab5mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.lab5mobile.data.local.entity.AirportEntity
import com.example.lab5mobile.data.local.model.FlightRouteLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    @Query(
        """
        SELECT * FROM airport
        WHERE iata_code LIKE :prefix || '%' COLLATE NOCASE
           OR name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY passengers DESC, name ASC
        LIMIT 10
        """
    )
    fun observeAirportSuggestions(query: String, prefix: String): Flow<List<AirportEntity>>

    @Query(
        """
        SELECT * FROM airport
        WHERE iata_code = :query COLLATE NOCASE
           OR name = :query COLLATE NOCASE
        LIMIT 1
        """
    )
    suspend fun getAirportByCode(query: String): AirportEntity?

    @Query(
        """
        SELECT
            dep.iata_code || '-' || arr.iata_code AS routeId,
            dep.iata_code AS departureCode,
            dep.name AS departureName,
            arr.iata_code AS destinationCode,
            arr.name AS destinationName,
            CASE WHEN favorite.id IS NULL THEN 0 ELSE 1 END AS isFavorite
        FROM airport AS dep
        INNER JOIN airport AS arr ON arr.id != dep.id
        LEFT JOIN favorite
            ON favorite.departure_code = dep.iata_code
           AND favorite.destination_code = arr.iata_code
        WHERE dep.iata_code = :departureCode
        ORDER BY arr.passengers DESC, arr.name ASC
        """
    )
    fun observeFlightsFromAirport(departureCode: String): Flow<List<FlightRouteLocal>>
}
