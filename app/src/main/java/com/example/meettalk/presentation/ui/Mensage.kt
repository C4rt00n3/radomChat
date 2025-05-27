package com.example.meettalk.presentation.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.body.CreateMessage
import com.example.meettalk.data.local.model.body.UpdateMessage
import com.example.meettalk.data.local.model.body.enums.MessageType
import com.example.meettalk.data.local.model.entities.MessageEntity
import com.example.meettalk.data.local.model.entities.UserEntity
import com.example.meettalk.presentation.components.ChatHeader
import com.example.meettalk.presentation.components.ChatInfo
import com.example.meettalk.presentation.components.InputBar
import com.example.meettalk.presentation.components.MessageCard
import com.example.meettalk.presentation.components.SwipeableUserChatCard
import com.example.meettalk.presentation.viewmodel.BlockViewModel
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.utils.TokenManager

@Composable
fun rememberDynamicOptions(
    selectedMessages: List<MessageEntity>,
    user: UserEntity?,
    onDeleteMessages: (List<MessageEntity>) -> Unit,
    onEditMessage: (MessageEntity) -> Unit,
    onResponding: (MessageEntity) -> Unit
): List<OptionsMenu> {
    val editLabel = stringResource(R.string.editar)
    val deleteLabel = stringResource(R.string.excluir)
    val respondLabel = stringResource(R.string.responder_mensagem)

    val options = mutableListOf<OptionsMenu>()

    if (selectedMessages.size == 1) {
        options.add(OptionsMenu(editLabel) {
            onEditMessage(selectedMessages.first())
        })
        options.add(OptionsMenu(respondLabel) {
            onResponding(selectedMessages.first())
        })
    }

    if (user != null && selectedMessages.all { it.senderId == user.uuid }) {
        options.add(OptionsMenu(deleteLabel) {
            onDeleteMessages(selectedMessages)
        })
    }

    return options
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageScreen(
    uuid: String,
    viewModel: ChatViewModel,
    blockViewModel: BlockViewModel
) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp


    var user by remember { mutableStateOf<UserEntity?>(null) }
    var token by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var editOn by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(false) }
    var respondingToMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var selectedMessages by remember { mutableStateOf(emptyList<MessageEntity>()) }
    var filteredUser by remember { mutableStateOf<UserEntity?>(null) }

    val chat by viewModel.chat.collectAsState()
    val blocks by blockViewModel.blocks.collectAsState()

    val messages = chat?.messages ?: emptyList()
    val myMessages = messages.filter { it.senderId == user?.uuid }
    val isBlocked = blocks.any { it.blockedUserId == filteredUser?.uuid }

    val dynamicOptions = rememberDynamicOptions(
        selectedMessages = selectedMessages,
        user = user,
        onDeleteMessages = {
            viewModel.deleteMany(token, it)
            selectedMessages = emptyList()
        },
        onEditMessage = {
            text = it.text
            editOn = true
        },
        onResponding = {
            respondingToMessage = it
        }
    )
    val listState = rememberLazyListState(messages.size)

    LaunchedEffect(Unit) {
        TokenManager(context).apply {
            token = getToken().orEmpty()
            user = getUser()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(uuid, token) {
        viewModel.findChat(uuid)?.let { loadedChat ->
            filteredUser = loadedChat.participants.firstOrNull { it.userId != user?.uuid }?.user
            viewModel.setChat(loadedChat)
        }
        blockViewModel.findAllBlock(token)
    }

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
                    user = filteredUser,
                    width = screenWidth,
                    options = dynamicOptions
                )
                ChatInfo(
                    chat = chat,
                    selectedCount = selectedMessages.size,
                    isChecked = checked
                ) { selected ->
                    selectedMessages = if (selected) myMessages else emptyList()
                    checked = selected
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .padding(horizontal = screenWidth * 0.05f)
                    .width(screenWidth * 0.9f)
            ) {
                respondingToMessage?.let {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.responder_mensagem),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.3f)
                            )
                            IconButton(onClick = { respondingToMessage = null }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_close_24),
                                    contentDescription = "Fechar",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = it.text,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                InputBar(
                    value = text,
                    isEditing = editOn,
                    onTextChange = { text = it },
                    onSend = {
                        if (text.isBlank()) return@InputBar

                        if (editOn) {
                            selectedMessages.firstOrNull()?.let {
                                viewModel.update(token, it.uuid, UpdateMessage(text))
                                editOn = false
                                text = ""
                                selectedMessages = emptyList()
                            }
                        } else {
                            filteredUser?.uuid?.let { receiverId ->
                                viewModel.create(
                                    token,
                                    CreateMessage(
                                        text = text,
                                        type = MessageType.TEXT,
                                        receiverId = receiverId,
                                        replyToId = respondingToMessage?.uuid
                                    )
                                ) {
                                    text = ""
                                    respondingToMessage = null
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            reverseLayout = true,
            state = listState
        ) {
            items(messages, key = { it.uuid }) { message ->
                val isMe = message.senderId == user?.uuid
                val isSelected = selectedMessages.contains(message)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onBackground.copy(0.2f)
                            else Color.Transparent
                        ),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    SwipeableUserChatCard(
                        onSwiped = {
                            respondingToMessage = if (message == respondingToMessage) {
                                null
                            } else {
                                message
                            }
                        }
                    ) {
                        Column {
                            val messageToReply = messages.find { it.uuid == message.replyToId }
                            MessageCard(
                                message = message,
                                messageToReply = messageToReply,
                                isMe = isMe,
                                onLongPress = {
                                    if (message !in selectedMessages) {
                                        selectedMessages += message
                                    }
                                }
                            ) {
                                if (message in selectedMessages) {
                                    selectedMessages -= message
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MessagePreview() {
    MeetTalkTheme {
        val context = LocalContext.current;
        val chatViewModel = ChatViewModel(context)
        val blockViewModel = BlockViewModel(context)
        MessageScreen("", chatViewModel, blockViewModel)
    }
}

