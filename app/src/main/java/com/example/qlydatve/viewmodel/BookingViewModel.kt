package com.example.qlydatve.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qlydatve.model.Booking
import com.example.qlydatve.model.CreateBookingRequest
import com.example.qlydatve.model.Seat
import com.example.qlydatve.service.BookingService
import com.example.qlydatve.service.PaymentService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BookingUiState(
    val bookings: List<Booking> = emptyList(),
    val seats: List<Seat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class BookingViewModel : ViewModel() {

    private val bookingService = BookingService()
    private val paymentService = PaymentService()

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            bookingService.getAllBookings()
                .onSuccess { _uiState.value = _uiState.value.copy(bookings = it, isLoading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
        }
    }

    fun loadSeats(airplaneId: Int, flightId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, seats = emptyList())
            bookingService.getSeats(airplaneId, flightId)
                .onSuccess { _uiState.value = _uiState.value.copy(seats = it, isLoading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
        }
    }

    fun createBooking(request: CreateBookingRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            bookingService.createBooking(request)
                .onSuccess {
                    loadBookings()
                    _uiState.value = _uiState.value.copy(successMessage = it)
                }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
        }
    }

    fun cancelBooking(id: Int) {
        viewModelScope.launch {
            bookingService.cancelBooking(id)
                .onSuccess { loadBookings(); _uiState.value = _uiState.value.copy(successMessage = it) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun processPayment(bookingId: Int, amount: Double) {
        viewModelScope.launch {
            paymentService.pay(bookingId, amount)
                .onSuccess { loadBookings(); _uiState.value = _uiState.value.copy(successMessage = it) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    fun clearSeats() {
        _uiState.value = _uiState.value.copy(seats = emptyList())
    }

    fun holdSeat(flightId: Int, seatId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            bookingService.holdSeat(flightId, seatId)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Lỗi giữ ghế") }
        }
    }

    fun releaseSeat(flightId: Int, seatId: Int) {
        viewModelScope.launch {
            bookingService.releaseSeat(flightId, seatId)
        }
    }
}
