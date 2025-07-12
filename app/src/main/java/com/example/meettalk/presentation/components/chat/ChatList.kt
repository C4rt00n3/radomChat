package com.example.meettalk.presentation.components.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.SwipeableUserChatCard
import com.example.meettalk.presentation.components.UserChatCard
import java.time.Instant

private const val SELECTED_CHAT_BACKGROUND_ALPHA = 0.3f

/**
 * Lista de conversas (chats).
 *
 * @param modifier Modificador para este componente.
 * @param chats Lista de chats a serem exibidos.
 * @param currentUser O usuário logado, para identificar o outro participante.
 * @param selectedChats Lista de chats atualmente selecionados.
 * @param token Token de autenticação para carregar dados (ex: imagem do outro usuário).
 * @param onChatTap Callback para quando um chat é tocado.
 * @param onChatLongPress Callback para quando um chat é pressionado longamente.
 * @param onChatSwipeToSelect Callback para quando um chat é selecionado via swipe.
 * @param navigate NavHostController para navegação.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    navigate: NavHostController,
    chats: List<Chat>,
    currentUser: User?,
    selectedChats: List<Chat>,
    token: String,
    onChatTap: (Chat) -> Unit,
    onChatLongPress: (Chat) -> Unit,
    onChatSwipeToSelect: (Chat) -> Unit
) {
    if (currentUser == null) {
        return
    }

    LazyColumn(modifier = modifier) {
        items(
            items = chats.distinctBy { it.uuid }.sortedByDescending { Instant.parse(it.createdAt) },
            key = { chat -> chat.uuid }
        ) { chat ->
            val otherUser = chat.participants.firstOrNull { participant ->
                participant.userId != currentUser.uuid
            }?.user
            otherUser?.let { user ->
                val isSelected = selectedChats.contains(chat)
                val backgroundColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = SELECTED_CHAT_BACKGROUND_ALPHA)
                } else {
                    Color.Transparent
                }

                SwipeableUserChatCard(
                    onSwiped = { onChatSwipeToSelect(chat) }
                ) {
                    UserChatCard(
                        user = user,
                        chat = chat,
                        token = token,
                        onTap = { onChatTap(chat) },
                        onTapUser = { userId -> navigate.navigate("user/$userId") },
                        onLongPress = { onChatLongPress(chat) },
                        background = backgroundColor
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }
    }
}