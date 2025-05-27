package com.example.meettalk.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.LoginRequest
import com.example.meettalk.data.local.model.entities.AuthResponse
import com.example.meettalk.data.remote.LoginEndpoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LoginViewModel(context: Context) : ViewModel() {
    private val url = context.getText(R.string.baseUrl).toString()

    private val _loginResult = MutableStateFlow<AuthResponse?>(null)
    val loginResult: StateFlow<AuthResponse?> = _loginResult

    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return emailRegex.matches(email)
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(LoginEndpoint::class.java)

    fun login(body: LoginRequest, onFinished: (AuthResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val response: Response<AuthResponse> = apiService.login(body)
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    _loginResult.value = result
                    onFinished(result)
                } else {
                    _loginResult.value = null
                    println(response)
                    Log.e("Login", "Erro: ${response.code()} - ${response.errorBody()?.string()}")
                    Log.e("Login", "Erro na resposta: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _loginResult.value = null
                Log.e("Login", "Erro na requisição: ${e.message}")
            }
        }
    }
}