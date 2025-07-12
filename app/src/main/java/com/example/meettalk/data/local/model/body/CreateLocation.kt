package com.example.meettalk.data.local.model.body

data class CreateLocation(
    val latitude: Double,
    val longitude: Double,
    val state: String? = null,
    val city: String? = null,
    val country: String? = null
) {}