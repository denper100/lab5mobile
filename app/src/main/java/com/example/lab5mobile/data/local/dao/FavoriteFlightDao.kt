package com.example.lab5mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lab5mobile.data.local.entity.FavoriteFlightEntity
import com.example.lab5mobile.data.local.model.FlightRouteLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteFlightDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favoriteFlight: FavoriteFlightEntity)

    @Query(
        """
        DELETE FROM favorite
        WHERE departure_code = :departureCode
          AND destination_code = :destinationCode
        """
    )
    suspend fun deleteFavorite(departureCode: String, destinationCode: String)

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM favorite
            WHERE departure_code = :departureCode
              AND destination_code = :destinationCode
        )
        """
    )
    suspend fun isFavorite(departureCode: String, destinationCode: String): Boolean

    @Query(
        """
        SELECT
            dep.iata_code || '-' || arr.iata_code AS routeId,
            dep.iata_code AS departureCode,
            dep.name AS departureName,
            arr.iata_code AS destinationCode,
            arr.name AS destinationName,
            1 AS isFavorite
        FROM favorite
        INNER JOIN airport AS dep ON dep.iata_code = favorite.departure_code
        INNER JOIN airport AS arr ON arr.iata_code = favorite.destination_code
        ORDER BY dep.name ASC, arr.passengers DESC, arr.name ASC
        """
    )
    fun observeFavoriteFlights(): Flow<List<FlightRouteLocal>>
}
