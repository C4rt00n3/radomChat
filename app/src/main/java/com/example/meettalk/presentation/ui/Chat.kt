package com.example.meettalk.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.meettalk.data.local.AppRoutes
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.chat.ChatList
import com.example.meettalk.presentation.components.chat.ChatTopBar
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme


/**
 * Representa a tela principal de Chat, exibindo a lista de conversas do usuário.
 * Permite buscar mensagens, visualizar o perfil e interagir com as conversas.
 *
 * @param chatViewModel O ViewModel responsável pela lógica de negócios e dados da tela de chat.
 * @param navController O controlador de navegação para transitar para outras telas (ex: tela de mensagens).
 * @param user O objeto do usuário logado. Pode ser nulo se o usuário não estiver carregado.
 * @param token O token de autenticação para carregar recursos protegidos, como imagens de perfil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    navController: NavHostController?
) {
    val chats by chatViewModel.chatsResult.collectAsState(listOf())

    val user: User? by userViewModel.user.collectAsState(null)
    val token by userViewModel.token.collectAsState("")

    var selectedChats by remember { mutableStateOf<List<Chat>>(listOf()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(token) {
        if (token.isNotBlank())
            chatViewModel.findMany(token)
        else
            userViewModel.pickToken()

        chatViewModel.observeChats()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding(),
        topBar = {
            ChatTopBar(
                user = user,
                token = token,
                searchQuery = searchQuery,
                onSearchQueryChanged = { query ->
                    searchQuery = query
                    chatViewModel.search(query)
                },
                onClearSearch = {
                    searchQuery = ""
                    chatViewModel.resetChats()
                },
                onProfileClicked = {
                    if (user != null)
                        navController?.navigate(
                            "user/${user!!.uuid}"
                        )
                },
                optionsMenuItems = listOf(OptionsMenu("Editar perfil") {
                    navController?.navigate(AppRoutes.EDIT_MY_PERFIL)
                }, OptionsMenu("Logout") { /* TODO: Lógica de logout */ })
            )
        }) { paddingValues ->
        if (navController != null) {
            ChatList(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
                chats = chats,
                navigate = navController,
                currentUser = user,
                selectedChats = selectedChats,
                token = token,
                onChatTap = { chat ->
                    if (selectedChats.contains(chat)) {
                        selectedChats = selectedChats - chat
                    } else {
                        navController?.navigate("chat/${chat.uuid}")
                    }
                },
                onChatLongPress = { chat ->
                    selectedChats = selectedChats + chat
                },
                onChatSwipeToSelect = { chat ->
                    selectedChats = selectedChats + chat
                })
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "Chat Screen Preview")
@Composable
fun ChatScreenPreview() {
    MeetTalkTheme {
        val context = LocalContext.current
        val mockNavController: NavHostController? = null // Ou um mock NavController

        val mockUser = User(
            uuid = "user123",
            name = "Usuário Mock",
            profileImages = emptyList(),
            age = 18,
            gender = Gender.M,
            chatParticipants = emptyList()
        )

        ChatScreen(
            chatViewModel = viewModel(),
            userViewModel = viewModel(),
            navController = mockNavController,
        )
    }
}