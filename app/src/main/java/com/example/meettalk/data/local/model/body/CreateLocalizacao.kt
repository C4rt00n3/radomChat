package com.example.meettalk.data.local.model.body

import java.lang.Thread.State

data class CreateLocalizacao(
    val lat: String,
    val lng: String,
    val estado: State? = null,
    val municipio: String? = null
)