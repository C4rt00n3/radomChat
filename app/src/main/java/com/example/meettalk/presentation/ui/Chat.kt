package com.example.meettalk.presentation.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.entities.ChatEntity
import com.example.meettalk.data.local.model.entities.UserEntity
import com.example.meettalk.presentation.components.MenuSelect
import com.example.meettalk.presentation.components.SwipeableUserChatCard
import com.example.meettalk.presentation.components.UserChatCard
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.ui.theme.TextColorGray
import com.example.meettalk.utils.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(chatViewModel: ChatViewModel, navController: NavHostController?) {
    val context = LocalContext.current;
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    var token by remember { mutableStateOf("") }
    var user: UserEntity? by remember { mutableStateOf(null) }
    var selectedChats: List<ChatEntity> by remember { mutableStateOf(listOf()) }
    var search by remember { mutableStateOf("") }
    val chats by chatViewModel.chatsResult.collectAsState(listOf())

    LaunchedEffect(Unit) {
        TokenManager(context).apply {
            getToken()?.let {
                token = it
            }
            getUser().let {
                user = it
            }
        }
    }

    LaunchedEffect(chats) {
        if (token.isNotBlank()) chatViewModel.findMany(token)
    }

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        Scaffold(modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(), topBar = {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = screenWidth * 0.05f,
                        end = screenWidth * 0.05f,
                    )
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = strokeWidth
                        )
                    }) {
                Row(
                    modifier = Modifier
                        .width(screenWidth * 0.9f)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.border(3.dp, TextColorGray, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_background),
                            contentDescription = "User's image. This image is personally chosen by the user."
                        )
                    }
                    MenuSelect(
                        listOf(
                            OptionsMenu("Editar perfil") {},
                            OptionsMenu("Logout") {}
                        )
                    )
                }
                Box {
                    TextField(
                        value = search,
                        onValueChange = {
                            search = it
                            chatViewModel.search(it)
                        },
                        placeholder = {
                            Text("Search messages...")
                        },
                        maxLines = 1,
                        trailingIcon = {
                            IconButton(onClick = {
                                if (search.isNotBlank(

                                    )
                                ) {
                                    chatViewModel.resetChats()
                                    search = ""
                                }
                            }) {
                                Icon(
                                    painter = if (search.isBlank()) painterResource(R.drawable.search)
                                    else painterResource(R.drawable.close),
                                    contentDescription = "Search or clear search input",
                                )
                            }
                        }, shape = CircleShape, colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.12f
                            ),
                            errorContainerColor = MaterialTheme.colorScheme.errorContainer,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            errorCursorColor = MaterialTheme.colorScheme.error,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = MaterialTheme.colorScheme.error
                        ), modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth()
                )
            }
        }) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(), contentPadding = paddingValues
            ) {
                user?.let { user ->
                    items(chats) { chat ->
                        val filteredUser = chat.participants.find {
                            it.userId != user.uuid
                        }
                        if (filteredUser != null) {
                            filteredUser.user?.let {
                                SwipeableUserChatCard({
                                    selectedChats += chat
                                }) {
                                    UserChatCard(
                                        user = it,
                                        chat = chat,
                                        onTap = {
                                            if (selectedChats.contains(chat)) {
                                                selectedChats -= chat
                                            } else {
                                                navController?.navigate(Screen.Message.withUuid(chat.uuid))
                                            }
                                        },
                                        onLongPress = {
                                            selectedChats += chat
                                        },
                                        background = if (selectedChats.contains(chat)) MaterialTheme.colorScheme.onBackground.copy(
                                            alpha = 0.3f
                                        )
                                        else Color.Transparent
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

@Preview(showBackground = true)
@Composable
fun ChatPreview() {
    MeetTalkTheme {
        val context = LocalContext.current
        val chatViewModel = ChatViewModel(context)
        Chat(chatViewModel, null)
    }
}