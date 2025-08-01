package com.example.meettalk.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.meettalk.R

/**
 * Botão flutuante para iniciar uma nova conversa aleatória.
 *
 * @param onFindRandomUser Callback invocado quando o botão é clicado para encontrar um usuário aleatório.
 */
@Composable
fun NewChatFloatingActionButton(onFindRandomUser: () -> Unit) {
    FloatingActionButton(onClick = onFindRandomUser) {
        Icon(
            imageVector = Icons.Default.Chat, // Ícone mais apropriado para iniciar chat
            contentDescription = stringResource(R.string.content_description_iniciar_chat_aleatorio),
            tint = Color.White,
        )
    }
}