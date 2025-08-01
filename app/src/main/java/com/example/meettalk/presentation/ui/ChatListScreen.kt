package com.example.meettalk.presentation.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.R
import com.example.meettalk.presentation.components.FullScreenLoader
import com.example.meettalk.presentation.components.NavigationBarApp
import com.example.meettalk.presentation.components.NewChatFloatingActionButton
import com.example.meettalk.presentation.components.chat.ChatFilterSegmentedButtons
import com.example.meettalk.presentation.components.chat.ChatListContent
import com.example.meettalk.presentation.components.chat.ChatListTopBar
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.utils.getLastUserLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.realm.kotlin.Realm
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

/**
 * Define as rotas de navegação usadas na aplicação.
 */
object AppRoutes {
    const val CHAT_DETAIL = "chat"
    const val USER_PROFILE = "user"
}

/**
 * Categorias de filtro para a lista de chats.
 */
enum class ChatFilterCategory(val index: Int) {
    ALL(0),
    UNREAD(1),
    FAVORITES(2)
}

/**
 * Tela principal que exibe a lista de chats do usuário.
 * Permite pesquisar, filtrar chats e navegar para detalhes do chat ou perfil do usuário.
 * Também gerencia permissões de localização e atualização.
 *
 * @param realm Instância do Realm para operações de banco de dados (opcional, com valor padrão null).
 * @param chatViewModel O [ChatViewModel] responsável por gerenciar os dados dos chats.
 * @param userViewModel O [UserViewModel] responsável por gerenciar os dados do usuário.
 * @param navController O [NavController] para navegação entre telas.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatListScreen(
    realm: Realm? = null,
    chatViewModel: ChatViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    val currentUser by userViewModel.myUser.collectAsState(null)
    val authToken by userViewModel.token.collectAsState("")
    val allChats by chatViewModel.chatsResult.collectAsState(emptyList())

    var selectedFilterCategoryIndex by remember { mutableIntStateOf(ChatFilterCategory.ALL.index) }
    var isInitialLoaderVisible by remember { mutableStateOf(false) }

    // Filtra e ordena a lista de chats com base na pesquisa e categoria selecionada
    val displayedChats =
        remember(searchQuery, selectedFilterCategoryIndex, allChats, currentUser?.uuid) {
            val filteredBySearch = if (searchQuery.isBlank()) {
                allChats
            } else {
                allChats.filter { chat ->
                    chat.participants.any { participant ->
                        participant.user?.name?.contains(searchQuery, ignoreCase = true) == true
                    }
                }
            }

            when (selectedFilterCategoryIndex) {
                ChatFilterCategory.ALL.index -> filteredBySearch
                ChatFilterCategory.UNREAD.index -> filteredBySearch.filter { chat ->
                    chat.messages.any { message ->
                        !message.isRead && message.senderId != currentUser?.uuid
                    }
                }

                ChatFilterCategory.FAVORITES.index -> filteredBySearch.filter { chat ->
                    chat.fav
                }

                else -> filteredBySearch
            }.sortedByDescending { chat ->
                try {
                    Instant.parse(chat.lastMessageDate)
                } catch (e: DateTimeParseException) {
                    Instant.EPOCH // Retorna uma data de época em caso de erro de parsing
                }
            }
        }

    var isLoadingMoreChats by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }
    val lazyListState = rememberLazyListState()

    // Launcher para solicitar permissão de localização
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val oneDayInMillis = TimeUnit.DAYS.toMillis(1)
        val currentTime = System.currentTimeMillis()

        if (isGranted) {
            // Verifica se a localização do usuário precisa ser atualizada (a cada 24h)
            currentUser?.location?.let { userLocation ->
                if (currentTime - userLocation.updatedAt >= oneDayInMillis) {
                    getLastUserLocation(context, { userViewModel.insertLocation(it) }) {}
                }
            } ?: run { // Se não houver localização, tenta obter
                getLastUserLocation(context, { userViewModel.insertLocation(it) }) {}
            }
        } else {
            Toast.makeText(context, R.string.permission_denied_location, Toast.LENGTH_SHORT).show()
        }
    }

    // Efeitos de inicialização
    LaunchedEffect(Unit) {
        userViewModel.build(context, realm)
        chatViewModel.build(context, realm)
        isInitialLoaderVisible = false // Garante que o loader inicial esteja oculto ao iniciar
    }

    // Conecta ao socket e observa chats quando o token está disponível
    LaunchedEffect(authToken) {
        if (authToken.isNotBlank()) {
            chatViewModel.connectSocket()
            chatViewModel.observeChats()
        }
    }

    // Carrega mais chats quando a página muda
    LaunchedEffect(currentPage) {
        if (currentPage > 0) {
            isLoadingMoreChats = true
            chatViewModel.findMany(currentPage)
            isLoadingMoreChats = false
        }
    }

    // Monitora o scroll para carregar mais chats
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .map {
                val lastVisibleItemIndex =
                    lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItemsCount = lazyListState.layoutInfo.totalItemsCount
                // Carrega mais se o usuário estiver a 5 itens do final e não houver carregamento ativo
                totalItemsCount > 0 && (lastVisibleItemIndex >= totalItemsCount - 5) && !isLoadingMoreChats
            }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) {
                    currentPage++
                }
            }
    }

    // Solicita permissão de localização na inicialização
    LaunchedEffect(locationPermissionState) {
        when {
            locationPermissionState.status.isGranted -> {
                // Se a permissão já foi concedida, tenta obter a localização
                currentUser?.location?.let { userLocation ->
                    val oneDayInMillis = TimeUnit.DAYS.toMillis(1)
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - userLocation.updatedAt >= oneDayInMillis) {
                        getLastUserLocation(context, { userViewModel.insertLocation(it) }) {}
                    }
                } ?: run {
                    getLastUserLocation(context, { userViewModel.insertLocation(it) }) {}
                }
            }

            else -> {
                // Se a permissão não foi concedida, solicita
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    Scaffold(
        topBar = {
            ChatListTopBar(
                currentUser = currentUser,
                authToken = authToken,
                navController = navController,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                isSearchActive = isSearchActive,
                onSearchActiveChange = { newActiveState ->
                    isSearchActive = newActiveState
                    if (!newActiveState) searchQuery = "" // Limpa a pesquisa ao desativar
                }
            )
        },
        floatingActionButton = {
            NewChatFloatingActionButton(
                onFindRandomUser = {
                    isInitialLoaderVisible = true
                    userViewModel.viewModelScope.launch {
                        userViewModel.randomUser()?.let { foundUser ->
                            // Navega para o chat com o novo usuário
                            navController.navigate("${AppRoutes.CHAT_DETAIL}/${foundUser.uuid}")
                        } ?: run {
                            Toast.makeText(
                                context,
                                R.string.no_random_user_found,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        isInitialLoaderVisible = false
                    }
                }
            )
        },
        bottomBar = { NavigationBarApp(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            ChatFilterSegmentedButtons(
                onCategorySelected = { index, _ -> selectedFilterCategoryIndex = index }
            )
            ChatListContent(
                displayedChats = displayedChats,
                currentUserUuid = currentUser?.uuid,
                authToken = authToken,
                lazyListState = lazyListState,
                isLoadingMore = isLoadingMoreChats,
                navController = navController
            )
        }
    }

    if (isInitialLoaderVisible) {
        FullScreenLoader()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Preview ChatListScreen - Light Theme")
@Composable
fun ChatListScreenPreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MeetTalkTheme(darkTheme = false) {
            ChatListScreen()
        }
    }
}