package com.example.meettalk.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

fun showToast(message: String, context: Context) {
    context.let { ctx ->
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()
        }
    }
}