package com.example.qlydatve.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qlydatve.model.Airport
import com.example.qlydatve.model.Airplane
import com.example.qlydatve.model.Flight
import com.example.qlydatve.service.FlightService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FlightUiState(
    val flights: List<Flight> = emptyList(),
    val airports: List<Airport> = emptyList(),
    val airplanes: List<Airplane> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class FlightViewModel : ViewModel() {

    private val flightService = FlightService()

    private val _uiState = MutableStateFlow(FlightUiState())
    val uiState: StateFlow<FlightUiState> = _uiState

    init {
        loadFlights()
        loadAirports()
        loadAirplanes()
    }

    fun loadFlights() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            flightService.getAllFlights()
                .onSuccess { _uiState.value = _uiState.value.copy(flights = it, isLoading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
        }
    }

    fun loadAirports() {
        viewModelScope.launch {
            flightService.getAirports()
                .onSuccess { _uiState.value = _uiState.value.copy(airports = it) }
                .onFailure { /* non-critical */ }
        }
    }

    fun loadAirplanes() {
        viewModelScope.launch {
            flightService.getAirplanes()
                .onSuccess { _uiState.value = _uiState.value.copy(airplanes = it) }
                .onFailure { /* non-critical */ }
        }
    }

    fun search(from: String, to: String, date: String = "") {
        if (from.isBlank() && to.isBlank()) { loadFlights(); return }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            flightService.searchFlights(from, to, date)
                .onSuccess { _uiState.value = _uiState.value.copy(flights = it, isLoading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
        }
    }

    fun addFlight(flight: Flight) {
        viewModelScope.launch {
            flightService.addFlight(flight)
                .onSuccess { loadFlights(); _uiState.value = _uiState.value.copy(successMessage = it) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun updateFlight(flight: Flight) {
        viewModelScope.launch {
            flightService.updateFlight(flight)
                .onSuccess { loadFlights(); _uiState.value = _uiState.value.copy(successMessage = it) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun deleteFlight(id: Int) {
        viewModelScope.launch {
            flightService.deleteFlight(id)
                .onSuccess { loadFlights(); _uiState.value = _uiState.value.copy(successMessage = it) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
