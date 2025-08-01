package com.example.meettalk.presentation.components

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.Preference
import com.example.meettalk.data.local.model.entities.User
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Classe selada que representa os diferentes estados da UI ao carregar os dados do usuário.
 * Isso permite que a UI reaja a carregamento, sucesso ou erros.
 */
sealed class UiState {
    data object Loading : UiState()
    data class Success @RequiresApi(Build.VERSION_CODES.O) constructor(
        @SuppressLint("NewApi") val user: User = User(
            uuid = "123e4567-e89b-12d3-a456-426614174000",
            name = "Carregando...",
            birthDate = ZonedDateTime.now().toString(),
            gender = Gender.F,
            chatParticipants = listOf(),
            profileImages = listOf(
                ImageProfile(
                    uuid = "1fe6efc5-d3eb-45c7-aab3-eb791e847a6e",
                    src = null,
                    userUuid = "123e4567-e89b-12d3-a456-426614174000"
                )
            ),
            preference = Preference(
                uuid = UUID.randomUUID().toString(),
                gender = Gender.F,
                maxAge = 25
            )
        )
    ) : UiState()

    data class Error(val message: String) : UiState()
}