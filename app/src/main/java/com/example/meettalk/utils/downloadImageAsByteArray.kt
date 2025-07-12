package com.example.meettalk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

suspend fun downloadImageAsByteArray(
    context: Context, imageUrl: String, token: String
): ByteArray? {
    return try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context).data(imageUrl)
            .allowHardware(false)
            .addHeader("Authorization", token)
            .build()

        val result = loader.execute(request)

        if (result is SuccessResult) {
            val bitmap = (result.drawable as BitmapDrawable).bitmap

            withContext(Dispatchers.IO) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.toByteArray()
            }
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

