package com.example.lab5mobile

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab5mobile.databinding.ActivityMainBinding
import com.example.lab5mobile.ui.AirportSuggestionAdapter
import com.example.lab5mobile.ui.FlightListAdapter
import com.example.lab5mobile.ui.FlightSearchViewModel
import androidx.core.widget.doAfterTextChanged
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: FlightSearchViewModel
    private lateinit var flightAdapter: FlightListAdapter
    private lateinit var suggestionAdapter: AirportSuggestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as FlightSearchApplication
        viewModel = ViewModelProvider(this, app.viewModelFactory)[FlightSearchViewModel::class.java]

        setupWindowInsets()
        setupUi()
        observeState()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupUi() {
        suggestionAdapter = AirportSuggestionAdapter(this)
        flightAdapter = FlightListAdapter { flight ->
            viewModel.onFavoriteToggle(flight.departureCode, flight.destinationCode)
        }

        binding.searchInput.setAdapter(suggestionAdapter)
        binding.searchInput.threshold = 1
        binding.searchInput.setOnItemClickListener { parent, _, position, _ ->
            val airport = parent.getItemAtPosition(position) as? com.example.lab5mobile.domain.model.Airport
            if (airport != null) {
                viewModel.onAirportSuggestionSelected(airport)
            }
        }
        binding.searchInput.doAfterTextChanged {
            viewModel.onQueryChanged(it?.toString().orEmpty())
        }
        binding.searchInput.setOnEditorActionListener { _, _, _ ->
            viewModel.onSearchRequested()
            true
        }
        binding.searchButton.setOnClickListener {
            viewModel.onSearchRequested()
        }

        binding.flightsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.flightsRecyclerView.adapter = flightAdapter
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (binding.searchInput.text?.toString() != state.query) {
                        binding.searchInput.setText(state.query)
                        binding.searchInput.setSelection(state.query.length)
                    }

                    suggestionAdapter.submitList(state.suggestions)
                    if (state.suggestions.isNotEmpty()
                        && binding.searchInput.hasFocus()
                        && state.query.isNotBlank()
                    ) {
                        binding.searchInput.showDropDown()
                    }

                    binding.resultsTitle.text = if (state.isShowingFavorites) {
                        getString(R.string.favorite_flights_title)
                    } else {
                        getString(
                            R.string.search_results_title,
                            state.selectedAirport?.iataCode ?: state.query
                        )
                    }

                    binding.progressBar.isVisible = state.isLoading
                    binding.messageText.isVisible =
                        state.emptyMessage != null || state.errorMessage != null
                    binding.messageText.text = state.errorMessage ?: state.emptyMessage.orEmpty()

                    flightAdapter.showDeparture = state.isShowingFavorites
                    flightAdapter.submitList(state.flights)
                }
            }
        }
    }
}
