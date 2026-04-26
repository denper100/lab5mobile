package com.example.lab5mobile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.lab5mobile.data.local.FlightSearchDatabase
import com.example.lab5mobile.data.preferences.SearchPreferences
import com.example.lab5mobile.data.repository.FlightSearchRepositoryImpl
import com.example.lab5mobile.domain.repository.FlightSearchRepository
import com.example.lab5mobile.ui.FlightSearchViewModel

class FlightSearchApplication : Application() {

    val database: FlightSearchDatabase by lazy {
        FlightSearchDatabase.create(this)
    }

    val searchPreferences: SearchPreferences by lazy {
        SearchPreferences(this)
    }

    val repository: FlightSearchRepository by lazy {
        FlightSearchRepositoryImpl(
            airportDao = database.airportDao(),
            favoriteFlightDao = database.favoriteFlightDao(),
            searchPreferences = searchPreferences
        )
    }

    val viewModelFactory: ViewModelProvider.Factory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                if (modelClass.isAssignableFrom(FlightSearchViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return FlightSearchViewModel(this@FlightSearchApplication, repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
