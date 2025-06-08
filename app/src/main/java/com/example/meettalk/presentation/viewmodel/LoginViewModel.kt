package com.example.meettalk.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.LoginRequest
import com.example.meettalk.data.local.model.entities.AuthResponse
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.data.remote.LoginEndpoint
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LoginViewModel(context: Context, private val realm: Realm) : ViewModel() {
    private val url = context.getText(R.string.baseUrl).toString()

    private val _loginResult = MutableStateFlow<AuthResponse?>(null)
    val loginResult: StateFlow<AuthResponse?> = _loginResult

    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return emailRegex.matches(email)
    }

    private val formatRealm = FormatRealm()
    private val formatClass = FormatClass()

    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(LoginEndpoint::class.java)


    fun login(body: LoginRequest, onFinished: (AuthResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.login(body)
                val result = response.body()

                if (response.isSuccessful && result != null) {
                    _loginResult.value = result

                    saveUserToRealm(result.user)

                    onFinished(result)
                } else {
                    _loginResult.value = null
                    val errorMsg = response.errorBody()?.string() ?: "Resposta sem corpo"
                    Log.e("Login", "Erro: ${response.code()} - $errorMsg")
                }

            } catch (e: Exception) {
                _loginResult.value = null
                Log.e("Login", "Erro na requisição", e)
            }
        }
    }

    private fun saveUserToRealm(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                realm.write {
                    val userRealm = formatRealm.toUserRealm(user)
                    userRealm?.let {
                        it.owner = true
                        copyToRealm(it, UpdatePolicy.ALL)
                    }
                }
            } catch (e: Exception) {
                Log.e("Realm", "Erro ao salvar usuário no Realm", e)
            }
        }
    }
}