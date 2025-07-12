package com.example.meettalk.presentation.ui

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.body.UpdateMessage
import com.example.meettalk.data.local.model.body.enums.MessageType
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.ChatParticipant
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.components.message.MessageBubble
import com.example.meettalk.presentation.viewmodel.MessageViewModel
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import com.example.meettalk.utils.TaskManager
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.UUID

/**
 * Define as opções dinâmicas do menu de contexto para as mensagens.
 *
 * @param selectedMessages Lista de mensagens atualmente selecionadas.
 * @param myUser O usuário atual (para verificar se o remetente é o usuário atual).
 * @param onDeleteMessages Callback para deletar mensagens.
 * @param onEditMessage Callback para editar uma única mensagem.
 * @param onRespondToMessage Callback para responder a uma mensagem.
 * @return Uma lista de [OptionsMenu] com base nas mensagens selecionadas.
 */
@Composable
fun rememberDynamicOptions(
    selectedMessages: List<Message>,
    myUser: User?,
    isFav: Boolean = false,
    isBlock: Boolean = false,
    onDeleteMessages: (List<Message>) -> Unit,
    onEditMessage: (Message) -> Unit,
    onRespondToMessage: (Message) -> Unit,
    onDeleteFoMe: (List<Message>) -> Unit,
    onFav: () -> Unit,
    onBlock: () -> Unit,
    onClear: () -> Unit
): List<OptionsMenu> {
    val editLabel = stringResource(R.string.editar)
    val deleteLabel = stringResource(R.string.excluir)
    val respondLabel = stringResource(R.string.responder_mensagem)
    val deleteForMe = stringResource(R.string.deletar_para_mim)
    val clear = stringResource(R.string.limpar_chat)

    return remember(selectedMessages.toList(), myUser) {
        val options = mutableListOf(
            OptionsMenu(if (isFav) "DesFavoritar" else "Favoritar", onFav),
            OptionsMenu(if (isBlock) "Desbloquear" else "Bloquear", onBlock)
        )

        if (selectedMessages.size == 1) {
            if (selectedMessages[0].senderId == myUser?.uuid) {
                options.add(OptionsMenu(editLabel) {
                    onEditMessage(selectedMessages.first())
                })
            }
            options.add(OptionsMenu(respondLabel) {
                onRespondToMessage(selectedMessages.first())
            })
        }

        if (selectedMessages.isNotEmpty()) {
            options.add(OptionsMenu(deleteForMe) {
                onDeleteFoMe(selectedMessages)
            })
            options.add(OptionsMenu(clear) {
                onClear()
            })
        }

        if (myUser != null && selectedMessages.all { it.senderId == myUser.uuid }) {
            options.add(OptionsMenu(deleteLabel) {
                onDeleteMessages(selectedMessages)
            })
        }

        options
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ChatScreen(
    userUuid: String,
    realm: Realm? = null,
    messageViewModel: MessageViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val taskManager = TaskManager()
    val context = LocalContext.current
    val myUser by userViewModel.myUser.collectAsState(null)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val selectedMessages: List<Message> by remember { mutableStateOf(emptyList()) }
    var textFieldState by remember { mutableStateOf("") }
    var replyMessage: Message? by remember { mutableStateOf(null) }
    var chat: Chat? by remember { mutableStateOf(null) }
    var currentUser: User? by remember {
        mutableStateOf(
            null
        )
    }
    var expanded by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()
    var error by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val token by userViewModel.token.collectAsState("")

    val focusRequester = remember { FocusRequester() }

    val dynamicOptions = rememberDynamicOptions(selectedMessages = selectedMessages,
        myUser = myUser,
        onDeleteMessages = { messagesToDelete ->
            taskManager.addTask {
                messageViewModel.deleteMessages(
                    messagesToDelete
                )
            }
        },
        onEditMessage = { messageToEdit ->
            taskManager.addTask {
                messageViewModel.updateMessage(
                    messageToEdit, UpdateMessage(messageToEdit.text)
                ) { textFieldState = "" }
            }
        },
        onRespondToMessage = { messageToRespond -> replyMessage = messageToRespond },
        isFav = chat?.fav == true && chat?.uuid != null,
        onDeleteFoMe = {
            taskManager.addTask { messageViewModel.deleteMessages(it, false) }
        },
        onFav = {
            if (chat?.uuid != null) {
                if (chat?.uuid!!.isNotBlank()) messageViewModel.markFav(chat?.uuid!!)
            } else {
                Toast.makeText(context, "Não é possivel favoritar ainda", Toast.LENGTH_LONG).show()
            }
        },
        onBlock = {},
        onClear = {
            if (chat?.uuid != null) {
                taskManager.addTask {
                    if (chat?.uuid!!.isNotBlank())
                        messageViewModel.clearChat(chat?.uuid!!)
                }
            }
        })


    LaunchedEffect(Unit) {
        messageViewModel.apply {
            build(context, realm)
            if (chat == null) {
                val chat1 = getChatsByParticipantUserId(userUuid)

                chat1.let {
                    chat = it
                }
            }
        }

        userViewModel.apply {
            build(context, realm)

            chat?.participants?.find { it.userId == userUuid }?.let {
                currentUser = it.user
            }

            if (currentUser == null) {
                getUser(userUuid)?.let {
                    currentUser = it
                }
            }

            if (currentUser == null) {
                getUserRequest(userUuid)?.let {
                    currentUser = it
                }
            }

        }

        if (currentUser == null) {
            error = true
        }
    }

    LaunchedEffect(chat) {
        if (chat == null) {
            val chat1 = messageViewModel.getChatsByParticipantUserId(userUuid)

            chat1.let {
                chat = it
            }
        }
        chat?.messages?.filter { !it.isRead && it.senderId != myUser?.uuid }?.let {
            if (it.isNotEmpty())
                currentUser?.let { it1 ->
                    messageViewModel.markChatAsRead(
                        chatUuid = chat?.uuid!!,
                        senderId = it1.uuid
                    )
                }
        }

        chat?.uuid?.let { it ->
            messageViewModel.observeChat(it) {
                chat = it
            }
        }
    }

    Column(
        Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .safeDrawingPadding()
            .fillMaxSize()
    ) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .fillMaxSize()
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        title = {},
                        navigationIcon = {
                            Box(
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton({ navController.popBackStack() }) {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                                            contentDescription = "Voltar"
                                        )
                                    }
                                    IconButton({
                                        navController.navigate("user/${currentUser?.uuid}")
                                    }) {

                                        val imageProfile =
                                            currentUser?.profileImages?.find { it.isPrimary }
                                                ?: currentUser?.profileImages?.firstOrNull()

                                        AsynchronousImageWithErrorPrevention(
                                            imageProfile = imageProfile,
                                            token = userViewModel.token.value,
                                            error = painterResource(R.drawable.img),
                                            placeholder = painterResource(R.drawable.img),
                                            contentDescription = stringResource(R.string.imagem_do_usu_rio),
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .clickable(onClick = {
                                                    navController.navigate("user/${currentUser?.uuid}")
                                                }),
                                            contentScale = ContentScale.Crop,
                                            userViewModel = userViewModel
                                        )
                                    }
                                    Spacer(Modifier.size(8.dp))
                                    Column {
                                        Text(
                                            currentUser?.name ?: "Aguarde..."
                                        )
                                        Text(
                                            if (currentUser?.name?.isNotBlank() == true) "Age: ${currentUser!!.age}"
                                            else "Aguarde...",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                                        )
                                    }
                                }
                            }
                        },
                        actions = {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = {
                                    expanded = !expanded
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu, contentDescription = "Menu"
                                    )
                                }
                                DropdownMenu(expanded = expanded,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    onDismissRequest = { expanded = !expanded }) {
                                    dynamicOptions.forEach {
                                        DropdownMenuItem(text = { Text(it.text) }, onClick = {
                                            expanded = false
                                            it.onClick()
                                        })
                                    }
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior,
                    )
                    Spacer(Modifier.padding(top = 16.dp))
                },
                bottomBar = {
                    Box(
                        contentAlignment = Alignment.BottomCenter,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (replyMessage != null) Box(
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondary,
                                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                )
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Respondendo à ${
                                            if (replyMessage?.senderId == myUser?.uuid)
                                                "sua própria mensagem"
                                            else
                                                currentUser?.name
                                        }",
                                        modifier = Modifier.padding(
                                            top = 16.dp, start = 16.dp
                                        ),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    IconButton({
                                        replyMessage = null
                                    }) {
                                        Icon(
                                            Icons.Default.Close, "Cancelar resposta"
                                        )
                                    }
                                }
                                Text(
                                    replyMessage?.text ?: "Carregando...",
                                    modifier = Modifier.padding(horizontal = 18.dp),
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.9f)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = textFieldState,
                            trailingIcon = {
                                if (textFieldState.isEmpty()) Row {
                                    IconButton({
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                    }) {
                                        Icon(
                                            Icons.Default.EmojiEmotions, "Abrir emojis"
                                        )
                                    }
                                    IconButton({
                                        println("Abrir galeria")
                                    }) {
                                        Icon(
                                            Icons.Default.AttachFile, "Abrir camera"
                                        )
                                    }
                                }
                                else IconButton(onClick = {
                                    keyboardController?.hide()
                                    textFieldState = ""
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                                }
                            },
                            leadingIcon = {
                                IconButton({
                                    println("Abrir camera")
                                }) {
                                    Icon(
                                        Icons.Default.CameraAlt, "Abrir camera"
                                    )
                                }
                            },
                            onValueChange = {
                                textFieldState = it
                            },
                            placeholder = {
                                Text("Digite...")
                            },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .padding(16.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(32.dp)),
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = {
                                myUser?.uuid?.let {
                                    val message = currentUser?.uuid?.let { it1 ->
                                        Message(
                                            text = textFieldState,
                                            createdAt = Instant.now().toString(),
                                            type = MessageType.TEXT,
                                            chatId = chat?.uuid,
                                            senderId = it,
                                            receiverId = it1,
                                            replyToId = replyMessage?.uuid,
                                            isSend = false
                                        )
                                    }

                                    if (message != null) {
                                        if (chat != null)
                                            chat = chat?.copy(messages = chat!!.messages + message)
                                        else {
                                            val chat_uuid = UUID.randomUUID().toString()
                                            chat = Chat(
                                                chat_uuid,
                                                lastMessageDate = Instant.now().toString(),
                                                createdAt = Instant.now().toString(),
                                                participants = listOf(
                                                    currentUser?.let { it1 ->
                                                        ChatParticipant(
                                                            chat_uuid,
                                                            it1.uuid,
                                                            null,
                                                            null
                                                        )
                                                    },
                                                    myUser?.let { it1 ->
                                                        ChatParticipant(
                                                            chat_uuid,
                                                            it1.uuid,
                                                            null,
                                                            null
                                                        )
                                                    }).filterNotNull(),
                                                messages = listOf(message)
                                            )
                                        }

                                        currentUser?.let { it1 ->
                                            messageViewModel.createMessage(
                                                token = token, message = message, currentUser = it1
                                            ) { chat1 ->
                                                if (chat1 != null) {
                                                    chat = chat1
                                                }

                                                textFieldState = ""
                                            }
                                        }
                                    }
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent,
                                errorBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = if (!isDarkTheme) painterResource(id = R.drawable.walpaper) else painterResource(
                            id = R.drawable.wallpaper_light
                        ),
                        contentDescription = "Background image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.3f
                    )
                    Column(
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(reverseLayout = true) {
                                items(chat?.messages?.sortedByDescending {
                                    try {
                                        Instant.parse(it.createdAt)
                                    } catch (e: DateTimeParseException) {
                                        Instant.EPOCH
                                    }
                                } ?: emptyList()) { message ->
                                    val reply =
                                        chat?.messages?.find { it.uuid == message.replyToId }
                                    val isSelected =
                                        selectedMessages.any { it.uuid == message.uuid }

                                    Spacer(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(.16.dp)
                                    )

                                    MessageBubble(
                                        message = message,
                                        isSelected = isSelected,
                                        isMi = message.senderId == myUser?.uuid,
                                        reply = reply,
                                        onTap = {
                                            if (selectedMessages.contains(it)) {
                                                selectedMessages - it
                                            }
                                        },
                                        onLongPress = {
                                            if (selectedMessages.contains(it)) {
                                                selectedMessages - it
                                            } else {
                                                selectedMessages + it
                                            }
                                        },
                                        onSwipe = {
                                            if (reply?.uuid != it.uuid) {
                                                replyMessage = it
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Preview(showBackground = true, name = "Preview Tela de Chats - Light Theme")
@Composable
fun ChatScreenPreview() {
    MeetTalkTheme(
        darkTheme = true
    ) {
        ChatScreen("")
    }
}