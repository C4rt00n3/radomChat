package com.example.meettalk.presentation.ui

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.meettalk.R
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.body.enums.MessageType
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.ChatParticipant
import com.example.meettalk.data.local.model.entities.ImageMessage
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.chat.rememberDynamicOptions
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.components.message.MessageBubble
import com.example.meettalk.presentation.viewmodel.MessageViewModel
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.utils.TaskManager
import io.realm.kotlin.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.UUID


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ChatScreen(
    userUuid: String,
    realm: Realm? = null,
    messageViewModel: MessageViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val taskManager = remember { TaskManager() } // remember taskManager
    val context = LocalContext.current
    val myUser by userViewModel.myUser.collectAsState(null)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val selectedMessages = remember { mutableStateListOf<Message>() }
    var textFieldState by remember { mutableStateOf("") }
    var replyMessage: Message? by remember { mutableStateOf(null) }
    var chat: Chat? by remember { mutableStateOf(null) }
    var currentUser: User? by remember {
        mutableStateOf(null)
    }
    var expanded by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()
    var error by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val token by userViewModel.token.collectAsState("")
    val contentResolver = context.contentResolver
    var selectedImageUri: Uri? by remember { mutableStateOf(null) }
    var tempFileForUpload: File? by remember { mutableStateOf(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                selectedImageUri = it // Armazena a URI selecionada
                userViewModel.viewModelScope.launch {
                    try {
                        tempFileForUpload = context.getTempFileFromUri(it)
                        Toast.makeText(
                            context,
                            "Imagem selecionada: ${tempFileForUpload?.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Log.e(
                            "ChatScreen",
                            "Erro ao criar arquivo temporário da imagem: ${e.message}",
                            e
                        )
                        Toast.makeText(context, "Erro ao selecionar imagem.", Toast.LENGTH_SHORT)
                            .show()
                        selectedImageUri = null
                        tempFileForUpload = null
                    }
                }
            }
        }
    )
    val focusRequester = remember { FocusRequester() }
    val lazyColumnState = rememberLazyListState()


    val dynamicOptions = rememberDynamicOptions(
        selectedMessages = selectedMessages,
        myUser = myUser,
        isFav = chat?.fav == true,
        onDeleteMessages = { messagesToDelete ->
            taskManager.addTask {
                messageViewModel.deleteMessages(messagesToDelete)
                selectedMessages.clear()
            }
        },
        onEditMessage = { messageToEdit ->
            replyMessage = messageToEdit
            textFieldState = messageToEdit.text
            selectedMessages.clear()
            keyboardController?.show()
            focusRequester.requestFocus()
        },
        onRespondToMessage = { messageToRespond ->
            replyMessage = messageToRespond
            selectedMessages.clear()
            keyboardController?.show()
            focusRequester.requestFocus()
        },
        onDeleteFoMe = { messagesToDelete ->
            taskManager.addTask {
                messageViewModel.deleteMessages(messagesToDelete, false)
                selectedMessages.clear()
            }
        },
        onFav = {
            if (chat?.uuid?.isNotBlank() == true) {
                messageViewModel.markFav(chat?.uuid!!)
            } else {
                Toast.makeText(context, "Não é possível favoritar ainda", Toast.LENGTH_LONG).show()
            }
        },
        onBlock = {
            if (chat?.uuid?.isNotBlank() == true) {
                Toast.makeText(
                    context,
                    "Funcionalidade de bloquear/desbloquear ainda não implementada.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, "Não é possível bloquear ainda", Toast.LENGTH_LONG).show()
            }
        },
        onClear = {
            if (chat?.uuid?.isNotBlank() == true) {
                taskManager.addTask {
                    messageViewModel.clearChat(chat?.uuid!!)
                    selectedMessages.clear()
                }
            } else {
                Toast.makeText(context, "Não é possível limpar o chat ainda", Toast.LENGTH_LONG)
                    .show()
            }
        }
    )


    LaunchedEffect(Unit) {
        messageViewModel.apply {
            build(context, realm)
            if (chat == null) {
                chat = getChatsByParticipantUserId(userUuid)
            }
        }

        userViewModel.apply {
            build(context, realm)

            chat?.participants?.find { it.userId == userUuid }?.let {
                currentUser = it.user
            }

            if (currentUser == null) {
                currentUser = getUser(userUuid)
            }

            if (currentUser == null) {
                currentUser = getUserRequest(userUuid)
            }
        }

        if (currentUser == null) {
            error = true
            Toast.makeText(
                context,
                "Não foi possível carregar o usuário do chat.",
                Toast.LENGTH_LONG
            ).show()
            navController.popBackStack()
        }

        userViewModel.getUser(userUuid, true).let {
            realm?.writeBlocking {
                println(it)
                val userRealm = query(UserRealm::class, "uuid == $0", it?.uuid).find().firstOrNull()

                if (userRealm != null && it != null) {
                    try {
                        val userRealmUpdatedAt = userRealm.updateAt?.let { Instant.parse(it) }
                        val incomingUserUpdatedAt = it.updateAt?.let { Instant.parse(it) }

                        if (incomingUserUpdatedAt != null &&
                            (userRealmUpdatedAt == null || incomingUserUpdatedAt.isAfter(userRealmUpdatedAt))) {
                            Log.d("UserUpdate", "Atualizando userRealm. userRealm.updateAt: ${userRealm.updateAt} | incoming.updateAt: ${it.updateAt}")
                            it.birthDate.let { userRealm.birthDate = it }
                            it.name.let { userRealm.name = it }
                            it.gender?.let { userRealm.gender = it }
                            it.preference?.let { userRealm.preference = it.toRealm() }

                            it.updateAt.let { userRealm.updateAt = it }

                        } else {
                            Log.d("UserUpdate", "Ignorando atualização de userRealm (dados mais antigos ou iguais). userRealm.updateAt: ${userRealm.updateAt} | incoming.updateAt: ${it.updateAt}")
                        }
                    } catch (e: DateTimeParseException) {
                        Log.e("UserUpdate", "Erro ao parsear data de updateAt: ${e.message}", e)
                    } catch (e: Exception) {
                        Log.e("UserUpdate", "Erro inesperado ao comparar datas de atualização: ${e.message}", e)
                    }
                }
            }
        }
    }

    LaunchedEffect(chat?.uuid) {
        chat?.uuid?.let { chatUuid ->
            messageViewModel.observeChat(chatUuid) { updatedChat ->
                chat = updatedChat
                val messageCount = updatedChat.messages.size
                if (messageCount > 0) {
                    messageViewModel.viewModelScope.launch {
                        lazyColumnState.scrollToItem(messageCount - 1)
                    }
                }

                val unreadMessages = updatedChat.messages
                    .filter { !it.isRead && it.senderId != myUser?.uuid }

                unreadMessages.forEach {
                    Log.d(
                        "ChatRead",
                        "Mensagem não lida: ${it.text}, Sender: ${it.senderId}"
                    )
                }


                if (myUser?.uuid != null && unreadMessages.isNotEmpty()) {
                    currentUser?.let { otherUser ->
                        messageViewModel.markChatAsRead(
                            chatUuid = chatUuid,
                            senderId = otherUser.uuid
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxWidth()
            ) {
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
                                        contentDescription = stringResource(R.string.voltar)
                                    )
                                }
                                IconButton({
                                    currentUser?.uuid?.let {
                                        navController.navigate("user/$it")
                                    }
                                }) {
                                    val imageProfile =
                                        currentUser?.profileImages?.find { it.slot == 1 }
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
                                                currentUser?.uuid?.let {
                                                    navController.navigate("user/$it")
                                                }
                                            }),
                                        contentScale = ContentScale.Crop,
                                        userViewModel = userViewModel,
                                    )
                                }
                                Spacer(Modifier.size(8.dp))
                                Column {
                                    Text(
                                        currentUser?.name ?: "Aguarde..."
                                    )
                                    Text(
                                        if (currentUser?.name?.isNotBlank() == true) stringResource(
                                            R.string.idade, currentUser!!.age
                                        )
                                        else stringResource(R.string.aguarde),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    actions = {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = {
                                expanded = !expanded
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = stringResource(
                                        R.string.menu
                                    )
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
            }
            Spacer(Modifier.padding(top = 16.dp))
        },
        bottomBar = {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (replyMessage != null || selectedImageUri != null)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Column {
                            replyMessage?.let { replyMsg ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Respondendo à ${
                                            if (replyMsg.senderId == myUser?.uuid)
                                                "sua própria mensagem"
                                            else
                                                currentUser?.name
                                        }",
                                        modifier = Modifier.padding(start = 8.dp),
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
                                    replyMsg.text ?: "Carregando...",
                                    modifier = Modifier.padding(
                                        end = 8.dp,
                                        start = 8.dp,
                                        bottom = 4.dp
                                    ),
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.9f)
                                )
                            }

                            selectedImageUri?.let { uri ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            top = 16.dp,
                                            bottom = 80.dp,
                                            end = 16.dp,
                                            start = 16.dp
                                        ),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Imagem selecionada",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.img),
                                        error = painterResource(R.drawable.img)
                                    )
                                    IconButton(
                                        onClick = {
                                            selectedImageUri = null
                                            tempFileForUpload = null
                                        },
                                        modifier = Modifier.offset(
                                            y = (-20).dp,
                                            x = (20).dp
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            "Remover imagem selecionada"
                                        )
                                    }
                                }
                            }
                        }
                    }

                OutlinedTextField(
                    value = textFieldState,
                    trailingIcon = {
                        if (textFieldState.isEmpty() && selectedImageUri == null) {
                            Row {
                                IconButton({
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                }) {
                                    Icon(
                                        Icons.Default.EmojiEmotions,
                                        stringResource(R.string.abrir_emojis)
                                    )
                                }
                                IconButton({
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        stringResource(R.string.anexar_arquivo)
                                    )
                                }
                            }
                        } else {
                            IconButton(onClick = {
                                keyboardController?.hide()
                                myUser?.uuid?.let { myUserId ->
                                    currentUser?.uuid?.let { receiverId ->
                                        val messageType =
                                            if (selectedImageUri != null) MessageType.IMAGE else MessageType.TEXT
                                        val messageText =
                                            if (selectedImageUri != null) "" else textFieldState

                                        val message = Message(
                                            uuid = UUID.randomUUID().toString(),
                                            text = messageText,
                                            createdAt = Instant.now().toString(),
                                            type = messageType,
                                            chatId = chat?.uuid,
                                            senderId = myUserId,
                                            receiverId = receiverId,
                                            replyToId = replyMessage?.uuid,
                                            ImageMessage = if (selectedImageUri != null) ImageMessage(
                                                uuid = UUID.randomUUID().toString(),
                                            ) else null
                                        )

                                        if (chat != null) {
                                            chat =
                                                chat?.copy(messages = chat!!.messages + message)
                                        }
                                        else {
                                            val newChatUuid = UUID.randomUUID().toString()
                                            chat = Chat(
                                                newChatUuid,
                                                lastMessageDate = Instant.now().toString(),
                                                createdAt = Instant.now().toString(),
                                                participants = listOf(
                                                    ChatParticipant(
                                                        newChatUuid,
                                                        receiverId,
                                                        null,
                                                        null
                                                    ),
                                                    ChatParticipant(
                                                        newChatUuid,
                                                        myUserId,
                                                        null,
                                                        null
                                                    )
                                                ).filterNotNull(),
                                                messages = listOf(message)
                                            )
                                        }

                                        val fileToSend: MultipartBody.Part? =
                                            tempFileForUpload?.let { file ->
                                                val type =
                                                    contentResolver.getType(selectedImageUri!!)
                                                        ?: "image/jpeg"
                                                file.asRequestBody(type.toMediaTypeOrNull())
                                                    .let {
                                                        MultipartBody.Part.createFormData(
                                                            "file",
                                                            file.name,
                                                            it
                                                        )
                                                    }
                                            }

                                        messageViewModel.createMessage(
                                            token = token,
                                            message = message,
                                            currentUser = currentUser!!,
                                            file = fileToSend
                                        ) { updatedChatAfterSend ->
                                            if (updatedChatAfterSend != null) {
                                                chat = updatedChatAfterSend
                                            }
                                            textFieldState = ""
                                            replyMessage = null
                                            selectedImageUri = null
                                            tempFileForUpload?.delete()
                                            tempFileForUpload = null
                                        }
                                    }
                                }
                            }) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = stringResource(R.string.enviar_mensagem)
                                )
                            }
                        }
                    },
                    leadingIcon = {
                        IconButton({
                            Toast.makeText(context, "Abrir câmera", Toast.LENGTH_SHORT)
                                .show()
                        }) {
                            Icon(
                                Icons.Default.CameraAlt, "Abrir câmera"
                            )
                        }
                    },
                    onValueChange = {
                        textFieldState = it
                    },
                    placeholder = {
                        Text("Digite uma mensagem...")
                    },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp)),
                    maxLines = 6,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        myUser?.uuid?.let { myUserId ->
                            currentUser?.uuid?.let { receiverId ->
                                val messageType =
                                    if (selectedImageUri != null) MessageType.IMAGE else MessageType.TEXT
                                val messageText =
                                    if (selectedImageUri != null) "" else textFieldState

                                val message = Message(
                                    uuid = UUID.randomUUID().toString(),
                                    text = messageText,
                                    createdAt = Instant.now().toString(),
                                    type = messageType,
                                    chatId = chat?.uuid,
                                    senderId = myUserId,
                                    receiverId = receiverId,
                                    replyToId = replyMessage?.uuid,
                                    isSend = false,
                                    ImageMessage = if (selectedImageUri != null) ImageMessage(
                                        uuid = UUID.randomUUID().toString(),
                                        src = null,
                                        user = null,
                                        message = null,
                                    ) else null
                                )

                                if (chat != null) {
                                    chat = chat?.copy(messages = chat!!.messages + message)
                                } else {
                                    val newChatUuid = UUID.randomUUID().toString()
                                    chat = Chat(
                                        newChatUuid,
                                        lastMessageDate = Instant.now().toString(),
                                        createdAt = Instant.now().toString(),
                                        participants = listOf(
                                            ChatParticipant(
                                                newChatUuid,
                                                receiverId,
                                                null,
                                                null
                                            ),
                                            ChatParticipant(
                                                newChatUuid,
                                                myUserId,
                                                null,
                                                null
                                            )
                                        ).filterNotNull(),
                                        messages = listOf(message)
                                    )
                                }

                                val fileToSend: MultipartBody.Part? =
                                    tempFileForUpload?.let { file ->
                                        val type =
                                            contentResolver.getType(selectedImageUri!!)
                                                ?: "image/jpeg"
                                        file.asRequestBody(type.toMediaTypeOrNull()).let {
                                            MultipartBody.Part.createFormData(
                                                "file",
                                                file.name,
                                                it
                                            )
                                        }
                                    }

                                messageViewModel.createMessage(
                                    token = token,
                                    message = message,
                                    currentUser = currentUser!!,
                                    file = fileToSend
                                ) { updatedChatAfterSend ->
                                    if (updatedChatAfterSend != null) {
                                        chat = updatedChatAfterSend
                                    }
                                    textFieldState = ""
                                    replyMessage = null
                                    selectedImageUri = null
                                    tempFileForUpload?.delete()
                                    tempFileForUpload = null
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
                contentDescription = stringResource(R.string.background_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    reverseLayout = true,
                    state = lazyColumnState
                ) {
                    items(chat?.messages?.sortedByDescending {
                        try {
                            Instant.parse(it.createdAt)
                        } catch (e: DateTimeParseException) {
                            Instant.EPOCH
                        }
                    } ?: emptyList(), key = { UUID.randomUUID() }) { message ->
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
                            userViewModel = userViewModel,
                            token = token,
                            message = message,
                            isSelected = isSelected,
                            isMi = message.senderId == myUser?.uuid,
                            reply = reply,
                            onTap = { tappedMessage ->
                                if (selectedMessages.contains(tappedMessage)) {
                                    selectedMessages.remove(tappedMessage)
                                } else {
                                    if (selectedMessages.isEmpty()) {
                                        selectedMessages += tappedMessage
                                    }
                                }
                            },
                            onLongPress = { longPressedMessage ->
                                if (selectedMessages.contains(longPressedMessage)) {
                                    selectedMessages.remove(longPressedMessage)
                                } else {
                                    selectedMessages.add(longPressedMessage)
                                }
                                if (selectedMessages.isNotEmpty()) {
                                    expanded = true
                                }
                            },
                            navController = navController,
                            onSwipe = { swipedMessage ->
                                selectedMessages.clear()
                                replyMessage = swipedMessage
                                keyboardController?.show()
                                focusRequester.requestFocus()
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Copia o conteúdo de um Uri para um arquivo temporário no diretório de cache do aplicativo.
 *
 * @param context O contexto da aplicação.
 * @param uri O Uri do arquivo original.
 * @return Um objeto File temporário contendo os dados, ou null em caso de erro.
 * É responsabilidade do chamador deletar este arquivo após o uso.
 */
private suspend fun Context.getTempFileFromUri(uri: Uri): File {
    return withContext(Dispatchers.IO) {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        tempFile
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun PreviewChatScreen() {
    MeetTalkTheme {
        ChatScreen(userUuid = "sample-user-uuid")
    }
}