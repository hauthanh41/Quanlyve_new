package com.example.qlydatve.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qlydatve.service.BookingService
import com.example.qlydatve.service.FlightService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalFlights: Int = 0,
    val totalBookings: Int = 0,
    val confirmedBookings: Int = 0,
    val cancelledBookings: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel : ViewModel() {

    private val flightService = FlightService()
    private val bookingService = BookingService()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init { loadStats() }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState(isLoading = true)
            val flightsDeferred = async { flightService.getAllFlights() }
            val bookingsDeferred = async { bookingService.getAllBookings() }

            val flights = flightsDeferred.await().getOrElse { emptyList() }
            val bookings = bookingsDeferred.await().getOrElse { emptyList() }

            _uiState.value = DashboardUiState(
                totalFlights = flights.size,
                totalBookings = bookings.size,
                confirmedBookings = bookings.count { it.status == "CONFIRMED" },
                cancelledBookings = bookings.count { it.status == "CANCELLED" }
            )
        }
    }
}
