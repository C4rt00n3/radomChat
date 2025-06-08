package com.example.meettalk.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import com.example.meettalk.utils.TokenManager
import com.example.meettalk.utils.getSubFromJwt
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val context: Context, private val realm: Realm) : ViewModel() {
    private val format = FormatRealm()
    private val formatR = FormatClass()
    private val tokenManager = TokenManager(context)

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _token = MutableStateFlow<String>("")
    val token: StateFlow<String> = _token

    fun findUser(uuid: String): User? {
        return try {
            realm.query<UserRealm>("uuid == $0", uuid).first().find()?.let {
                formatR.fromUserRealm(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun observeToken(onChange: (String) -> Unit) {
        viewModelScope.launch {
            _token.collect { token ->
                onChange(token)
            }
        }
    }

    fun pickToken(onSuccess: (User) -> Unit = {}) {
        tokenManager.getToken()?.let { storedToken ->
            viewModelScope.launch(Dispatchers.IO) {
                _token.value = storedToken
                val subject = getSubFromJwt(storedToken)
                subject?.let { sub ->
                    val userRealm = realm.query<UserRealm>("uuid == $0", sub).first().find()
                    userRealm?.let { user ->
                        formatR.fromUserRealm(user).let {
                            _user.value = it
                            onSuccess(it)
                        }
                    }
                }
            }
        }
    }
}