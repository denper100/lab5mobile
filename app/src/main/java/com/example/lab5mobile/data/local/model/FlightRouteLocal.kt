package com.example.lab5mobile.data.local.model

data class FlightRouteLocal(
    val routeId: String,
    val departureCode: String,
    val departureName: String,
    val destinationCode: String,
    val destinationName: String,
    val isFavorite: Boolean
)
