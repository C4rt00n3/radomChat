package com.example.meettalk.presentation.ui

import MessageInput
import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.body.CreateMessage
import com.example.meettalk.data.local.model.body.UpdateMessage
import com.example.meettalk.data.local.model.body.enums.MessageType
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.SwipeableUserChatCard
import com.example.meettalk.presentation.components.chat.ChatHeader
import com.example.meettalk.presentation.components.chat.ChatInfo
import com.example.meettalk.presentation.components.message.MessageBubble
import com.example.meettalk.presentation.components.message.ReplyMessage
import com.example.meettalk.presentation.components.message.ReplyMessageSection
import com.example.meettalk.presentation.viewmodel.MessageViewModel
import com.example.meettalk.presentation.viewmodel.UserViewModel
import java.time.Instant

/**
 * Define as opções dinâmicas do menu de contexto para as mensagens.
 *
 * @param selectedMessages Lista de mensagens atualmente selecionadas.
 * @param currentUser O usuário atual (para verificar se o remetente é o usuário atual).
 * @param onDeleteMessages Callback para deletar mensagens.
 * @param onEditMessage Callback para editar uma única mensagem.
 * @param onRespondToMessage Callback para responder a uma mensagem.
 * @return Uma lista de [OptionsMenu] com base nas mensagens selecionadas.
 */
@Composable
fun rememberDynamicOptions(
    selectedMessages: List<Message>,
    currentUser: User?,
    onDeleteMessages: (List<Message>) -> Unit,
    onEditMessage: (Message) -> Unit,
    onRespondToMessage: (Message) -> Unit
): List<OptionsMenu> {
    val editLabel = stringResource(R.string.editar)
    val deleteLabel = stringResource(R.string.excluir)
    val respondLabel = stringResource(R.string.responder_mensagem)

    return remember(selectedMessages, currentUser) {
        val options = mutableListOf<OptionsMenu>()

        if (selectedMessages.size == 1) {
            options.add(OptionsMenu(editLabel) {
                onEditMessage(selectedMessages.first())
            })
            options.add(OptionsMenu(respondLabel) {
                onRespondToMessage(selectedMessages.first())
            })
        }

        if (currentUser != null && selectedMessages.isNotEmpty() &&
            selectedMessages.all { it.senderId == currentUser.uuid }
        ) {
            options.add(OptionsMenu(deleteLabel) {
                onDeleteMessages(selectedMessages)
            })
        }
        options
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun MessageInputSection(
    messageText: String,
    isEditingMessage: Boolean,
    editingMessage: Message?,
    respondingToMessage: Message?,
    chatState: Chat?,
    otherChatParticipantUuid: String?,
    token: String,
    viewModel: MessageViewModel,
    onSuccess: () -> Unit = {}
) {
    val context = LocalContext.current

    MessageInput(
        value = messageText,
        isEditing = isEditingMessage,
        onTextChange = viewModel::onMessageTextChange,
        onSend = {
            if (messageText.isBlank()) return@MessageInput

            if (isEditingMessage) {
                editingMessage?.let { msgToEdit ->
                    chatState?.let { chat ->
                        viewModel.updateMessage(
                            token, msgToEdit, UpdateMessage(messageText), chat
                        )
                    }
                    viewModel.stopEditingMessage()
                }
            } else {
                otherChatParticipantUuid?.let { receiverId ->
                    viewModel.createMessage(
                        token = token,
                        message = CreateMessage(
                            text = messageText,
                            type = MessageType.TEXT,
                            receiverId = receiverId,
                            replyToId = respondingToMessage?.uuid
                        ),
                        error = { errorResult ->
                            Toast.makeText(
                                context,
                                errorResult.message.orEmpty(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) { /* Opcional: lidar com sucesso/falha de criação */ }
                    viewModel.onDismissReply()
                }
            }
            viewModel.clearMessageInput()
            onSuccess()
        }
    )
}

/**
 * Tela de chat que exibe mensagens e permite enviá-las e interagir com elas.
 * Lida com carregamento de dados, seleção de mensagens, edição e resposta.
 *
 * @param uuid O UUID do chat a ser exibido.
 * @param userViewModel O [UserViewModel] para gerenciar o estado e as operações do usuario.
 * @param viewModel O [MessageViewModel] para gerenciar o estado e as operações do chat.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MessageScreen(
    uuid: String,
    navigate: NavHostController,
    userViewModel: UserViewModel,
    viewModel: MessageViewModel,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val chatState by viewModel.chat.collectAsState(null)
    val uiState by viewModel.uiState.collectAsState()

    val token by userViewModel.token.collectAsState("")
    val currentUser by userViewModel.user.collectAsState(null)

    val (messageText, editingMessage, respondingToMessage, selectedMessages) = uiState
    val isEditingMessage = editingMessage != null

    val messages = remember(chatState?.messages) {
        (chatState?.messages ?: emptyList()).sortedByDescending { message ->
            message.createdAt.let { Instant.parse(it) } ?: Instant.EPOCH
        }
    }
    val myMessages = remember(messages, currentUser) {
        messages.filter { it.senderId == currentUser?.uuid }
    }
    val otherChatParticipant = remember(chatState, currentUser) {
        chatState?.participants?.firstOrNull { it.userId != currentUser?.uuid }?.user
    }

    val listState = rememberLazyListState()

    val lastRelevantItemIndex = remember(messages, currentUser) {
        messages.indexOfFirst { it.senderId == currentUser?.uuid }
    }

    LaunchedEffect(listState) {
        if (lastRelevantItemIndex != -1) {
            listState.animateScrollToItem(lastRelevantItemIndex)
        }
        viewModel.observeChat(uuid)
    }

    LaunchedEffect(chatState) {
        viewModel.findChat(uuid) { foundChat ->
            viewModel.setChat(foundChat)

            val unreadCount =
                foundChat.messages.filter { !it.isRead && it.senderId != currentUser?.uuid }

            if (unreadCount.isNotEmpty()) {
                foundChat.participants.firstOrNull { it.userId != currentUser?.uuid }?.let {
                    viewModel.markChatAsRead(
                        uuid,
                        token,
                        it.userId
                    )
                }
            }
        }
    }

    val dynamicOptions = rememberDynamicOptions(
        selectedMessages = selectedMessages,
        currentUser = currentUser,
        onDeleteMessages = { messagesToDelete ->
            viewModel.deleteMessages(token, messagesToDelete)
            viewModel.clearMessageSelection()
        },
        onEditMessage = { messageToEdit ->
            viewModel.onStartEditingMessage(messageToEdit)
            viewModel.clearMessageSelection()
        },
        onRespondToMessage = { messageToRespond ->
            viewModel.onStartRespondingToMessage(messageToRespond)
            viewModel.clearMessageSelection()
        }
    )

    Scaffold(
        modifier = Modifier.safeContentPadding(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChatHeader(
                    user = otherChatParticipant,
                    width = screenWidth,
                    token = token,
                    options = dynamicOptions,
                )
                ChatInfo(
                    chat = chatState,
                    selectedCount = selectedMessages.size,
                    isChecked = selectedMessages.isNotEmpty() && selectedMessages.size == myMessages.size,
                    onSelectAllMyMessages = { selected ->
                        viewModel.toggleSelectAllMyMessages(selected, myMessages)
                    }
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .padding(horizontal = screenWidth * 0.05f)
                    .fillMaxWidth()
            ) {
                val activeContextMessage = editingMessage ?: respondingToMessage
                activeContextMessage?.let { message ->
                    ReplyMessageSection(
                        message = message,
                        currentUser = currentUser,
                        chat = chatState,
                        onDismissReply = {
                            if (editingMessage != null) viewModel.stopEditingMessage()
                            else viewModel.onDismissReply()
                        },
                        inputMessage = {
                            MessageInputSection(
                                messageText = messageText,
                                isEditingMessage = isEditingMessage,
                                editingMessage = editingMessage,
                                respondingToMessage = respondingToMessage,
                                chatState = chatState,
                                otherChatParticipantUuid = otherChatParticipant?.uuid,
                                token = token,
                                viewModel = viewModel
                            )
                        }
                    )
                }
                MessageInputSection(
                    messageText = messageText,
                    isEditingMessage = isEditingMessage,
                    editingMessage = editingMessage,
                    respondingToMessage = respondingToMessage,
                    chatState = chatState,
                    otherChatParticipantUuid = otherChatParticipant?.uuid,
                    token = token,
                    viewModel = viewModel,
                    onSuccess = {

                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            reverseLayout = true,
            state = listState,
        ) {
            var count = 0
            items(messages, key = { count++ }) { message ->
                val isCurrentUserMessage = message.senderId == currentUser?.uuid
                val isMessageSelected = selectedMessages.contains(message)

                val senderUser =
                    chatState?.participants?.find { it.userId == message.senderId }?.user

                SwipeableUserChatCard(onSwiped = { viewModel.onStartRespondingToMessage(message) }) {
                    MessageBubble(
                        message = message,
                        isMi = isCurrentUserMessage,
                        token = token,
                        user = senderUser,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .background(
                                if (isMessageSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        navigate = navigate,
                        onLongPress = { viewModel.toggleMessageSelection(message) },
                        reply = {
                            messages.firstOrNull { it.uuid == message.replyToId }
                                ?.let { repliedMessage ->
                                    val isRepliedMessageFromCurrentUser =
                                        repliedMessage.senderId == currentUser?.uuid
                                    val repliedSenderUser = remember(
                                        repliedMessage.senderId,
                                        currentUser,
                                        chatState?.participants
                                    ) {
                                        if (isRepliedMessageFromCurrentUser) currentUser
                                        else chatState?.participants?.find { it.userId == repliedMessage.senderId }?.user
                                    }
                                    ReplyMessage(
                                        repliedMessage,
                                        repliedSenderUser,
                                        isRepliedMessageFromCurrentUser
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                        },
                        onTap = {
                            if (selectedMessages.isNotEmpty()) {
                                viewModel.toggleMessageSelection(message)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}