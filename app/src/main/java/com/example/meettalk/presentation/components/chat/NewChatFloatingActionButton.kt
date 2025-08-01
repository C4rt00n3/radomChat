package com.example.meettalk.presentation.components.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.meettalk.R

/**
 * Botão flutuante para iniciar um novo chat com um usuário aleatório.
 *
 * @param onFindRandomUser Callback invocado quando o botão é clicado.
 */
@Composable
fun NewChatFloatingActionButton(onFindRandomUser: () -> Unit) {
    FloatingActionButton(onClick = onFindRandomUser) {
        Icon(
            imageVector = Icons.Default.Chat, // Ícone de chat é mais apropriado
            contentDescription = stringResource(R.string.content_description_start_random_chat),
            tint = Color.White,
        )
    }
}