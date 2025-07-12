package com.example.meettalk.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meettalk.R
import com.example.meettalk.data.local.model.RealmClass.BlockRealm
import com.example.meettalk.data.local.model.RealmClass.ChatParticipantRealm
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.RealmClass.ImageMessageRealm
import com.example.meettalk.data.local.model.RealmClass.ImageProfileRealm
import com.example.meettalk.data.local.model.RealmClass.LocationRealm
import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.RealmClass.PreferenceRealm
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.body.LoginRequest
import com.example.meettalk.data.local.model.entities.AuthResponse
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.data.remote.LoginEndpoint
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LoginViewModel : ViewModel() {
    lateinit var context: Context
    lateinit var url: String
    lateinit var realm: Realm

    private val _loginResult = MutableStateFlow<AuthResponse?>(null)
    val loginResult: StateFlow<AuthResponse?> = _loginResult

    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return emailRegex.matches(email)
    }

    private val formatRealm = FormatRealm()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService: LoginEndpoint by lazy {
        retrofit.create(LoginEndpoint::class.java)
    }

    fun build(context: Context, realm: Realm?) {
        val url = context.getText(R.string.baseUrl).toString()
        this.context = context
        if (realm == null) {
            val config = RealmConfiguration.Builder(
                schema = setOf(
                    UserRealm::class,
                    BlockRealm::class,
                    ChatRealm::class,
                    LocationRealm::class,
                    ImageProfileRealm::class,
                    ChatParticipantRealm::class,
                    ImageMessageRealm::class,
                    MessageRealm::class,
                    PreferenceRealm::class
                )
            ).schemaVersion(1).deleteRealmIfMigrationNeeded().build()

            val realm1 by lazy {
                Realm.open(config)
            }

            this.realm = realm1
        } else {
            this.realm = realm
        }
        this.url = url
    }

    fun login(body: LoginRequest, onFinished: (AuthResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.login(body) // 'apiService' só será inicializado aqui, após a 'url' ser definida
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