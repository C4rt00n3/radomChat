package com.example.meettalk.data.local.model.body

data class ImageUploadResponse(
    val message: String,
    val filename: String,
    val size: Long,
    val url: String?
)