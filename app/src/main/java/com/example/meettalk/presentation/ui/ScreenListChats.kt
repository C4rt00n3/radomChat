package com.example.meettalk.presentation.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.data.local.model.body.enums.MessageType
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.ChatParticipant
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.NavigationBarApp
import com.example.meettalk.presentation.components.SingleChoiceSegmentedButton
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.components.message.user
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.BlueOther
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.utils.formatarDataAmigavel
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

object Routes {
    const val CHAT_DETAIL = "chat"
    const val USER_PROFILE = "user"
}

enum class ChatFilterCategory(val index: Int) {
    ALL(0),
    UNREAD(1),
    FAVORITES(2)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScreenListChats(
    realm: Realm? = null,
    chatViewModel: ChatViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current

    val locPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchActive by rememberSaveable { mutableStateOf(false) }

    val myUser by userViewModel.myUser.collectAsState(null)
    val token by userViewModel.token.collectAsState("")

    val allChats by chatViewModel.chatsResult.collectAsState(emptyList())

    var selectedFilterCategoryIndex by remember { mutableIntStateOf(ChatFilterCategory.ALL.index) }

    var showInitialLoader by remember { mutableStateOf(false) }

    val displayList = remember(searchQuery, selectedFilterCategoryIndex, allChats, myUser?.uuid) {
        val filteredBySearch = if (searchQuery.isBlank()) {
            allChats
        } else {
            allChats.filter { chat ->
                chat.participants.any { participant ->
                    participant.user?.name?.contains(searchQuery, ignoreCase = true) == true
                }
            }
        }

        val filteredByCategory = when (selectedFilterCategoryIndex) {
            ChatFilterCategory.ALL.index -> filteredBySearch
            ChatFilterCategory.UNREAD.index -> filteredBySearch.filter { chat ->
                chat.messages.any { message ->
                    !message.isRead && message.senderId != myUser?.uuid
                }
            }
            ChatFilterCategory.FAVORITES.index -> filteredBySearch.filter { chat ->
                chat.fav
            }
            else -> filteredBySearch
        }

        filteredByCategory.sortedByDescending {
            try {
                Instant.parse(it.lastMessageDate)
            } catch (e: DateTimeParseException) {
                Instant.EPOCH
            }
        }
    }

    var isLoadingMore by remember { mutableStateOf(false) }
    var page by remember { mutableIntStateOf(1) }
    val listState = rememberLazyListState()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val ONE_DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1)
        val currentTime = System.currentTimeMillis()

        if (isGranted) {
            if(user.location == null || (currentTime - (user.location.updatedAt) >= ONE_DAY_IN_MILLIS))getLastUserLocation(context, {
                userViewModel.insertLocation(it)
            }) {}
        } else {
            println("Permissão de localização negada.")
        }
    }

    LaunchedEffect(Unit) {
        userViewModel.build(context, realm)
        chatViewModel.build(context, realm)
        showInitialLoader = false
    }

    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            chatViewModel.connectSocket()
            chatViewModel.observeChats()
        }
    }

    LaunchedEffect(page) {
        if (page > 0) {
            isLoadingMore = true
            chatViewModel.findMany(page)
            isLoadingMore = false
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { _ ->
                val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItemsCount = listState.layoutInfo.totalItemsCount
                totalItemsCount > 0 && (lastVisibleItemIndex >= totalItemsCount - 5) && !isLoadingMore
            }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) {
                    page++
                }
            }
    }

    LaunchedEffect(locPermissionState) {
        when {
            locPermissionState.status.isGranted -> {
                getLastUserLocation(context, {
                    userViewModel.insertLocation(it)
                }) {}
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Box(
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
                    Column(
                        Modifier.background(
                            MaterialTheme.colorScheme.secondaryContainer
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent,
                            ),
                            navigationIcon = {
                                val imageProfile =
                                    myUser?.profileImages?.find { it.isPrimary }
                                        ?: myUser?.profileImages?.firstOrNull() // Usar firstOrNull

                                AsynchronousImageWithErrorPrevention(
                                    imageProfile = imageProfile,
                                    token = token,
                                    contentDescription = stringResource(R.string.imagem_do_usu_rio),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                            },
                            title = {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .semantics {
                                            isTraversalGroup = true
                                        }, contentAlignment = Alignment.Center
                                ) {
                                    val onActiveChange: (Boolean) -> Unit = { newActiveState ->
                                        searchActive = newActiveState
                                        if (!newActiveState) {
                                            searchQuery = ""
                                        }
                                    }

                                    SearchBar(
                                        inputField = {
                                            SearchBarDefaults.InputField(
                                                query = searchQuery,
                                                onQueryChange = { newQuery ->
                                                    searchQuery = newQuery
                                                },
                                                expanded = searchActive,
                                                onExpandedChange = onActiveChange,
                                                placeholder = {
                                                    Text("Pesquisar chat...")
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Search,
                                                        contentDescription = "Pesquisar"
                                                    )
                                                },
                                                trailingIcon = {
                                                    if (searchActive) {
                                                        IconButton(onClick = {
                                                            if (searchQuery.isNotEmpty()) {
                                                                searchQuery = ""
                                                            } else {
                                                                searchActive = false
                                                            }
                                                        }) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Limpar pesquisa"
                                                            )
                                                        }
                                                    }
                                                },
                                                onSearch = { searchActive = false },
                                            )
                                        },
                                        expanded = searchActive,
                                        onExpandedChange = onActiveChange,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .semantics { traversalIndex = 0f }
                                            .padding(horizontal = 8.dp),
                                        colors = SearchBarDefaults.colors(containerColor = Color.Transparent),
                                        content = {},
                                    )
                                }
                            },
                            actions = {},
                            scrollBehavior = scrollBehavior,
                        )
                        Spacer(Modifier.padding(top = 16.dp))
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        showInitialLoader = !showInitialLoader
                        userViewModel.apply {
                            viewModelScope.launch {
                                randomUser()?.let{
                                    setUsers(listOf(it))
                                    navController.navigate("chat/${it.uuid}")
                                }
                            }
                        }
                        showInitialLoader = !showInitialLoader
                    }) {
                        Icon(
                            Icons.Default.Edit,
                            "Editar perfil",
                            tint = Color.White,
                        )
                    }
                },
                bottomBar = {
                    NavigationBarApp(navController)
                }) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    SingleChoiceSegmentedButton(modifier = Modifier.padding(vertical = 16.dp)) { index, _ ->
                        selectedFilterCategoryIndex = index
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        items(displayList, key = { it.uuid }) { chat ->
                            val message = chat.messages.lastOrNull()
                            val participantUser =
                                chat.participants.find { it.userId != myUser?.uuid }?.user
                            val imageProfile = participantUser?.profileImages?.find { it.isPrimary }
                                ?: participantUser?.profileImages?.firstOrNull()

                            val unreadMessageCount =
                                chat.messages.count { !it.isRead && it.senderId != myUser?.uuid }

                            ListItem(headlineContent = {
                                Text(participantUser?.name ?: stringResource(R.string.usu_rio_desconhecido))
                            },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                ),
                                leadingContent = {
                                    Box {
                                        AsynchronousImageWithErrorPrevention(
                                            imageProfile = imageProfile,
                                            token = token,
                                            contentDescription = stringResource(R.string.imagem_do_usu_rio),
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    participantUser?.let {
                                                        navController.navigate("${Routes.USER_PROFILE}/${it.uuid}")
                                                    }
                                                },
                                            contentScale = ContentScale.Crop
                                        )
                                        if (chat.fav)
                                            Icon(
                                                Icons.Default.Favorite,
                                                stringResource(R.string.estar_entre_os_favoritos),
                                                tint = Color.Red,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .align(Alignment.BottomEnd)
                                            )
                                    }
                                },
                                trailingContent = {
                                    if (message != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(formatarDataAmigavel(message.createdAt))
                                            Spacer(Modifier.size(8.dp))
                                            if (unreadMessageCount == 0) {
                                                Icon(
                                                    painter = painterResource(R.drawable.checkmarkdoneoutline),
                                                    contentDescription = if (message.isRead) stringResource(
                                                        R.string.mensagem_visualizada
                                                    )
                                                    else stringResource(R.string.mensagem_n_o_visualizada),
                                                    modifier = Modifier.size(12.dp),
                                                    tint = if (message.isRead) BlueOther else Color.Gray
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .offset(x = 6.dp, y = (-6).dp)
                                                        .clip(CircleShape)
                                                        .background(BlueOther)
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        .size(if (unreadMessageCount > 9) 20.dp else 16.dp)
                                                        .wrapContentSize(Alignment.Center)
                                                ) {
                                                    Text(
                                                        text = if (unreadMessageCount > 99) "99+" else unreadMessageCount.toString(),
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                supportingContent = {
                                    Text(message?.text ?: "")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        chat.participants.find { it.userId != myUser?.uuid }
                                            ?.let { participant ->
                                                navController.navigate("${Routes.CHAT_DETAIL}/${participant.userId}")
                                            }
                                    })
                            HorizontalDivider(thickness = 2.dp)
                        }
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showInitialLoader) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Preview Tela de Chats - Light Theme")
@Composable
fun ScreenListChatsPreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MeetTalkTheme(darkTheme = false) {
            ScreenListChats()
        }
    }
}