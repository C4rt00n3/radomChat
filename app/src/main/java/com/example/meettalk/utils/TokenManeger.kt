package com.example.meettalk.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.meettalk.data.local.model.entities.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TokenManager(context: Context) {
    private val gson = Gson()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", "Bearer $token").apply()
    }

    fun saveUser(user: User) {
        val json = gson.toJson(user)
        sharedPreferences.edit().putString("user", json).apply()
    }

    fun getUser(): User? {
        val json = sharedPreferences.getString("user", null) ?: return null
        return gson.fromJson(json, object : TypeToken<User>() {}.type)
    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun clearToken() {
        sharedPreferences.edit().remove("auth_token").apply()
    }
}