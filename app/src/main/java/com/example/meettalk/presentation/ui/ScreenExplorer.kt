package com.example.meettalk.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.NavigationBarApp
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import io.realm.kotlin.Realm
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenExplorer(
    realm: Realm? = null,
    userViewModel: UserViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val currentUser by userViewModel.myUser.collectAsState(null)
    val token by userViewModel.token.collectAsState("")
    val userLists by userViewModel.users.collectAsState(emptyList())
    var isLoadingMore by remember { mutableStateOf(false) }
    var page by remember { mutableIntStateOf(1) }
    val listState = rememberLazyListState()
    var showInitialLoader by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.build(realm = realm, context = context)
    }

    LaunchedEffect(page) {
        isLoadingMore = true
        val newUsers = userViewModel.findRandomUsers(page)
        userViewModel.setUsers(userLists + newUsers)
        isLoadingMore = false
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.map {
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            totalItemsCount > 0 && (lastVisibleItemIndex >= totalItemsCount - 5) && !isLoadingMore
        }.distinctUntilChanged().collect { shouldLoadMore ->
            if (shouldLoadMore) {
                page++
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
                                            token = token,
                                            contentDescription = stringResource(R.string.imagem_do_usu_rio),
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .clickable(onClick = {
                                                    navController.navigate("user/${currentUser?.uuid}")
                                                }),
                                            contentScale = ContentScale.Crop
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
//                                    dynamicOptions.forEach {
//                                        DropdownMenuItem(text = { Text(it.text) }, onClick = {
//                                            expanded = false
//                                            it.onClick()
//                                        })
//                                    }
                                }
                            }
                        },
                    )
                    Spacer(Modifier.padding(top = 16.dp))
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
                bottomBar = { NavigationBarApp(navController) }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier.padding(paddingValues),
                    state = listState
                ) {
                    items(userLists) { user ->
                        val imageProfile =
                            user.profileImages.firstOrNull()
                        ListItem(headlineContent = {
                            Column {
                                Text(user.name ?: "Usuário Desconhecido")
                                Text(text = "idade: " + user.age, fontSize = 12.sp)
                            }
                        }, colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ), leadingContent = {
                            val req = ImageRequest.Builder(context)
                                .data(stringResource(R.string.baseUrl) + "/image-profile/${imageProfile?.uuid}")
                                .apply {
                                    if (token.isNotEmpty()) {
                                        addHeader("Authorization", token)
                                    }
                                    placeholder(R.drawable.img)
                                    error(R.drawable.img)
                                    allowHardware(false)
                                }
                                .build()
                            IconButton({
                                navController.navigate("user/${user.uuid}")
                            }) {
                                AsyncImage(
                                    model = req,
                                    contentDescription = stringResource(R.string.imagem_do_usu_rio),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("chat/${user.uuid}")
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
                                Text("Carregando mais usuários...")
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
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
@Preview
fun ScreenExplorerPreview() {
    MeetTalkTheme {
        ScreenExplorer()
    }
}