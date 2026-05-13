package com.example.qlydatve.mapper

import com.example.qlydatve.model.Flight

data class FlightDisplayModel(
    val id: Int,
    val flightNumber: String,
    val route: String,
    val departureTime: String,
    val arrivalTime: String,
    val price: String,
    val status: String
)

object FlightMapper {

    fun toDisplayModel(flight: Flight): FlightDisplayModel {
        return FlightDisplayModel(
            id = flight.id,
            flightNumber = flight.flightNumber,
            route = "${flight.departureAirport} → ${flight.arrivalAirport}",
            departureTime = flight.departureTime,
            arrivalTime = flight.arrivalTime,
            price = "%.0f VNĐ".format(flight.price),
            status = flight.status
        )
    }

    fun toDisplayList(flights: List<Flight>): List<FlightDisplayModel> =
        flights.map { toDisplayModel(it) }
}
