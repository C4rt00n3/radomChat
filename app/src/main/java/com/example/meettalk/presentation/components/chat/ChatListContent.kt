package com.example.meettalk.presentation.components.chat

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.presentation.components.LoadingIndicator
import com.example.meettalk.presentation.ui.AppRoutes

/**
 * Conteúdo principal da lista de chats, exibindo os itens de chat.
 *
 * @param displayedChats A lista de [Chat] a ser exibida.
 * @param currentUserUuid O UUID do usuário logado para identificar mensagens lidas/não lidas.
 * @param authToken O token de autenticação para carregar imagens.
 * @param lazyListState O [rememberLazyListState] para controlar o estado da lista.
 * @param isLoadingMore Indica se mais chats estão sendo carregados.
 * @param navController O [NavController] para navegação.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatListContent(
    displayedChats: List<Chat>,
    currentUserUuid: String?,
    authToken: String,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    isLoadingMore: Boolean,
    navController: NavController
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState
    ) {
        items(displayedChats, key = { it.uuid }) { chat ->
            val lastMessage = chat.messages.lastOrNull()
            val participantUser = chat.participants.find { it.userId != currentUserUuid }?.user
            val participantProfileImage = participantUser?.profileImages?.find { it.slot == 1 }
                ?: participantUser?.profileImages?.firstOrNull()

            val unreadMessageCount = chat.messages.count { !it.isRead && it.senderId != currentUserUuid }

            ChatItem(
                chat = chat,
                participantUser = participantUser,
                participantProfileImage = participantProfileImage,
                lastMessage = lastMessage,
                unreadMessageCount = unreadMessageCount,
                authToken = authToken,
                context = context,
                onChatClick = {
                    participantUser?.let {
                        navController.navigate("${AppRoutes.CHAT_DETAIL}/${it.uuid}")
                    }
                },
                onProfileImageClick = {
                    participantUser?.let {
                        if (it.profileImages.isNotEmpty()) {
                            navController.navigate("${AppRoutes.USER_PROFILE}/${it.uuid}")
                        } else {
                            Toast.makeText(context, R.string.no_images_available, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
            HorizontalDivider(thickness = 2.dp)
        }
        if (isLoadingMore) {
            item {
                LoadingIndicator()
            }
        }
    }
}