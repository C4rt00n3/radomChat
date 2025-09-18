package com.example.meettalk.presentation.components.chat

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Chat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.presentation.components.LoadingIndicator
import com.example.meettalk.presentation.ui.AppRoutes
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import io.realm.kotlin.Realm



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
    lazyListState: LazyListState,
    isLoadingMore: Boolean,
    navController: NavController,
    chatViewModel: ChatViewModel
) {
    val context = LocalContext.current
    val selectedChats by chatViewModel.selectedChats.collectAsState(emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(), state = lazyListState
    ) {
        items(displayedChats, key = { it.uuid }) { chat ->
            // CORREÇÃO: Garante que 'messages' nunca seja nulo.
            // Se chat.messages for nulo, 'messages' será uma lista vazia.
            val messages = chat.messages ?: emptyList()

            // Agora, 'messages.isEmpty()' e 'messages.lastOrNull()' são seguros para chamar,
            // pois 'messages' nunca será nulo.
            val lastMessage = if (messages.isEmpty()) null else messages.lastOrNull()
            val participantUser = chat.participants.find { it.userId != currentUserUuid }?.user
            val participantProfileImage = participantUser?.profileImages?.find { it.slot == 1 }
                ?: participantUser?.profileImages?.firstOrNull()

            val unreadMessageCount = messages.count { !it.isRead && it.senderId != currentUserUuid }

            ChatItem(
                chat = chat,
                participantUser = participantUser,
                participantProfileImage = participantProfileImage,
                lastMessage = lastMessage,
                unreadMessageCount = unreadMessageCount,
                authToken = authToken,
                onProfileImageClick = {
                    participantUser?.let {
                        if (it.profileImages.isNotEmpty()) {
                            navController.navigate("${AppRoutes.USER_PROFILE}/${it.uuid}")
                        } else {
                            Toast.makeText(context, R.string.no_images_available, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                },
                modifier = Modifier
                    .background(
                        if (selectedChats.contains(chat)) Color.White.copy(0.5f) else Color.Transparent
                    )
                    .combinedClickable(onClick = {
                        if (selectedChats.contains(chat))
                            chatViewModel.removeSelectedChats(chat)
                        else
                            participantUser?.let {
                                navController.navigate("${AppRoutes.CHAT_DETAIL}/${it.uuid}")
                            }
                    }, onLongClick = {
                        if (selectedChats.contains(chat)) chatViewModel.removeSelectedChats(chat)
                        else chatViewModel.addSelectedChats(chat)
                    })
            )
        }
        if (isLoadingMore) {
            item {
                LoadingIndicator()
            }
        }
    }
}