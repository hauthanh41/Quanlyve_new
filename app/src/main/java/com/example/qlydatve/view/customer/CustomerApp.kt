package com.example.qlydatve.view.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.example.qlydatve.model.Flight
import com.example.qlydatve.model.Seat
import com.example.qlydatve.model.User
import com.example.qlydatve.view.booking.BookingScreen
import com.example.qlydatve.view.booking.PaymentScreen
import com.example.qlydatve.view.booking.PaymentSuccessScreen
import com.example.qlydatve.view.booking.SeatSelectionScreen
import com.example.qlydatve.viewmodel.BookingViewModel

private sealed class CustomerNav {
    object Home : CustomerNav()
    object Flights : CustomerNav()
    object Bookings : CustomerNav()
    object Profile : CustomerNav()
    object Chat : CustomerNav()
    data class SeatSelection(val flight: Flight) : CustomerNav()
    data class Payment(val flight: Flight, val seat: Seat) : CustomerNav()
    data class PaymentSuccess(val flight: Flight, val seat: Seat, val bookingCode: String) : CustomerNav()
}

enum class CustomerScreen { HOME, FLIGHTS, BOOKINGS, PROFILE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerApp(user: User, onLogout: () -> Unit) {
    var nav by remember { mutableStateOf<CustomerNav>(CustomerNav.Home) }
    var currentTab by remember { mutableStateOf(CustomerScreen.HOME) }
    var showAllFlights by remember { mutableStateOf(false) }

    val bookingVm: BookingViewModel = viewModel()
    val bookingState by bookingVm.uiState.collectAsStateWithLifecycle()

    // Xử lý màn hình chat hỗ trợ
    if (nav is CustomerNav.Chat) {
        CustomerChatScreen(
            currentUserId = user.id,
            userName = user.fullName,
            onBack = { nav = CustomerNav.Profile }
        )
        return
    }

    // Xử lý màn hình chọn ghế
    if (nav is CustomerNav.SeatSelection) {
        val s = nav as CustomerNav.SeatSelection

        // Tải danh sách ghế khi vào màn hình
        LaunchedEffect(s.flight.id) {
            if (s.flight.id > 0) {
                bookingVm.loadSeats(s.flight.airplaneId, s.flight.id)
            }
        }

        SeatSelectionScreen(
            flight = s.flight,
            seats = bookingState.seats,
            isLoading = bookingState.isLoading,
            error = if (s.flight.id <= 0) "Lỗi: ID chuyến bay không hợp lệ (ID=0)" else bookingState.error,
            onRetry = { bookingVm.loadSeats(s.flight.airplaneId, s.flight.id) },
            onConfirm = { seat -> nav = CustomerNav.Payment(s.flight, seat) },
            onBack = {
                bookingVm.clearSeats()
                // Quay lại đúng tab đang đứng
                nav = when(currentTab) {
                    CustomerScreen.HOME -> CustomerNav.Home
                    CustomerScreen.FLIGHTS -> CustomerNav.Flights
                    else -> CustomerNav.Home
                }
            }
        )
        return
    }

    // Xử lý màn hình thanh toán
    if (nav is CustomerNav.Payment) {
        val p = nav as CustomerNav.Payment
        PaymentScreen(
            flight = p.flight,
            seat = p.seat,
            passengerName = user.fullName,
            onPaymentSuccess = { code -> nav = CustomerNav.PaymentSuccess(p.flight, p.seat, code) },
            onBack = { nav = CustomerNav.SeatSelection(p.flight) }
        )
        return
    }

    // Xử lý màn hình thành công
    if (nav is CustomerNav.PaymentSuccess) {
        val ps = nav as CustomerNav.PaymentSuccess
        PaymentSuccessScreen(
            bookingCode = ps.bookingCode,
            flight = ps.flight,
            seat = ps.seat,
            passengerName = user.fullName,
            maskedEmail = user.email.let {
                val at = it.indexOf('@')
                if (at > 3) "ngu***${it.substring(at)}" else it
            },
            onViewTickets = {
                currentTab = CustomerScreen.BOOKINGS
                nav = CustomerNav.Bookings
            },
            onGoHome = {
                currentTab = CustomerScreen.HOME
                nav = CustomerNav.Home
            }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == CustomerScreen.HOME,
                    onClick = { currentTab = CustomerScreen.HOME; nav = CustomerNav.Home },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Trang chủ") }
                )
                NavigationBarItem(
                    selected = currentTab == CustomerScreen.FLIGHTS,
                    onClick = {
                        showAllFlights = false
                        currentTab = CustomerScreen.FLIGHTS
                        nav = CustomerNav.Flights
                    },
                    icon = { Icon(Icons.Default.Search, null) },
                    label = { Text("Tìm vé") }
                )
                NavigationBarItem(
                    selected = currentTab == CustomerScreen.BOOKINGS,
                    onClick = { currentTab = CustomerScreen.BOOKINGS; nav = CustomerNav.Bookings },
                    icon = { Icon(Icons.Default.ConfirmationNumber, null) },
                    label = { Text("Vé của tôi") }
                )
                NavigationBarItem(
                    selected = currentTab == CustomerScreen.PROFILE,
                    onClick = { currentTab = CustomerScreen.PROFILE; nav = CustomerNav.Profile },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Hồ sơ") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                CustomerScreen.HOME -> HomeScreen(
                    user = user,
                    onSearchFlight = {
                        showAllFlights = true
                        currentTab = CustomerScreen.FLIGHTS
                        nav = CustomerNav.Flights
                    },
                    onFlightSelected = { flight -> nav = CustomerNav.SeatSelection(flight) },
                    onViewBookings = { currentTab = CustomerScreen.BOOKINGS; nav = CustomerNav.Bookings }
                )
                CustomerScreen.FLIGHTS -> SearchFlightScreen(
                    onFlightSelected = { flight -> nav = CustomerNav.SeatSelection(flight) },
                    showAllFlights = showAllFlights
                )
                CustomerScreen.BOOKINGS -> BookingScreen(
                    currentUser = user
                )
                CustomerScreen.PROFILE -> ProfileScreen(
                    user = user,
                    onLogout = onLogout,
                    onUserUpdated = {},
                    onContactSupport = { nav = CustomerNav.Chat }
                )
            }
        }
    }
}