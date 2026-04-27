package com.example.lab5mobile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab5mobile.R
import com.example.lab5mobile.domain.model.Airport
import com.example.lab5mobile.domain.repository.FlightSearchRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FlightSearchViewModel(
    application: Application,
    private val repository: FlightSearchRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FlightSearchUiState())
    val uiState: StateFlow<FlightSearchUiState> = _uiState.asStateFlow()

    private val queryInput = MutableStateFlow("")
    private var flightsJob: Job? = null
    private val appContext = getApplication<Application>()

    init {
        observeSavedSearch()
        observeSuggestions()
    }

    fun onQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                query = query,
                errorMessage = null
            )
        }
        queryInput.value = query
        if (query.isBlank()) {
            showFavorites(clearPersistedSearch = true)
        }
    }

    fun onAirportSuggestionSelected(airport: Airport) {
        _uiState.update { state ->
            state.copy(
                query = airport.iataCode,
                selectedAirport = airport,
                suggestions = emptyList()
            )
        }
        queryInput.value = airport.iataCode
        observeFlightsForAirport(airport)
    }

    fun onSearchRequested() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) {
            showFavorites(clearPersistedSearch = true)
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.getAirportByCode(query)
            }.onSuccess { airport ->
                if (airport == null) {
                    _uiState.update { state ->
                        state.copy(
                            selectedAirport = null,
                            flights = emptyList(),
                            isShowingFavorites = false,
                            emptyMessage = appContext.getString(R.string.airport_not_found),
                            errorMessage = null
                        )
                    }
                } else {
                    onAirportSuggestionSelected(airport)
                }
            }.onFailure { error ->
                showError(error)
            }
        }
    }

    fun onFavoriteToggle(departureCode: String, destinationCode: String) {
        viewModelScope.launch {
            runCatching {
                repository.toggleFavorite(departureCode, destinationCode)
            }.onFailure { error ->
                showError(error)
            }
        }
    }

    private fun observeSavedSearch() {
        viewModelScope.launch {
            repository.lastSavedSearchCode
                .catch { error ->
                    showError(error)
                }
                .collectLatest { savedCode ->
                    if (savedCode.isNullOrBlank()) {
                        _uiState.update { it.copy(query = "", isLoading = false) }
                        showFavorites(clearPersistedSearch = false)
                    } else {
                        _uiState.update { it.copy(query = savedCode) }
                        val airport = repository.getAirportByCode(savedCode)
                        if (airport != null) {
                            onAirportSuggestionSelected(airport)
                        } else {
                            showFavorites(clearPersistedSearch = true)
                        }
                    }
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSuggestions() {
        viewModelScope.launch {
            queryInput
                .debounce(250)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(suggestions = emptyList()) }
                    } else {
                        repository.observeAirportSuggestions(query)
                            .catch { error -> showError(error) }
                            .collect { suggestions ->
                                _uiState.update { state ->
                                    state.copy(suggestions = suggestions)
                                }
                            }
                    }
                }
        }
    }

    private fun observeFlightsForAirport(airport: Airport) {
        flightsJob?.cancel()
        flightsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedAirport = airport,
                    isShowingFavorites = false,
                    isLoading = true,
                    errorMessage = null
                )
            }
            repository.saveLastSearchCode(airport.iataCode)
            repository.observeFlightsFromAirport(airport.iataCode)
                .catch { error ->
                    showError(error)
                }
                .collect { flights ->
                    _uiState.update { state ->
                        state.copy(
                            flights = flights,
                            isShowingFavorites = false,
                            isLoading = false,
                            emptyMessage = if (flights.isEmpty()) {
                                appContext.getString(
                                    R.string.search_empty_message,
                                    airport.iataCode
                                )
                            } else {
                                null
                            }
                        )
                    }
                }
        }
    }

    private fun showFavorites(clearPersistedSearch: Boolean) {
        flightsJob?.cancel()
        flightsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedAirport = null,
                    suggestions = emptyList(),
                    isShowingFavorites = true,
                    isLoading = true,
                    errorMessage = null
                )
            }
            if (clearPersistedSearch) {
                repository.saveLastSearchCode(null)
            }
            repository.observeFavoriteFlights()
                .catch { error ->
                    showError(error)
                }
                .collect { favorites ->
                    _uiState.update { state ->
                        state.copy(
                            flights = favorites,
                            isShowingFavorites = true,
                            isLoading = false,
                            emptyMessage = if (favorites.isEmpty()) {
                                appContext.getString(R.string.favorites_empty_message)
                            } else {
                                null
                            }
                        )
                    }
                }
        }
    }

    private fun showError(error: Throwable) {
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                errorMessage = error.message ?: appContext.getString(R.string.database_error)
            )
        }
    }
}
