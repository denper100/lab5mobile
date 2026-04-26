package com.example.lab5mobile.domain.model

data class Airport(
    val id: Int,
    val iataCode: String,
    val name: String,
    val passengers: Int
) {
    val displayLabel: String
        get() = "$iataCode - $name"

    override fun toString(): String = displayLabel
}
