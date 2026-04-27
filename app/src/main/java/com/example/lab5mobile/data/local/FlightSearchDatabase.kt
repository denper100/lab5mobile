package com.example.lab5mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.lab5mobile.data.local.dao.AirportDao
import com.example.lab5mobile.data.local.dao.FavoriteFlightDao
import com.example.lab5mobile.data.local.entity.AirportEntity
import com.example.lab5mobile.data.local.entity.FavoriteFlightEntity

@Database(
    entities = [AirportEntity::class, FavoriteFlightEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FlightSearchDatabase : RoomDatabase() {

    abstract fun airportDao(): AirportDao

    abstract fun favoriteFlightDao(): FavoriteFlightDao

    companion object {
        private const val DATABASE_NAME = "flight_search.db"
        private const val DATABASE_ASSET_PATH = "database/flight_search.db"

        fun create(context: Context): FlightSearchDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FlightSearchDatabase::class.java,
                DATABASE_NAME
            )
                .createFromAsset(DATABASE_ASSET_PATH)
                .fallbackToDestructiveMigration(false)
                .build()
        }
    }
}
