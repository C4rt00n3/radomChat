package com.example.meettalk.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.presentation.components.FullScreenLoader
import com.example.meettalk.presentation.components.NavigationBarApp
import com.example.meettalk.presentation.components.NewChatFloatingActionButton
import com.example.meettalk.presentation.components.explorer.ExplorerTopAppBar
import com.example.meettalk.presentation.components.user.UserListContent
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import io.realm.kotlin.Realm
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Tela de Exploração de Usuários.
 * Permite ao usuário visualizar uma lista de outros usuários, interagir com seus perfis
 * e iniciar conversas. A tela carrega usuários de forma paginada e mostra o perfil do
 * usuário logado na barra superior.
 *
 * @param realm Instância do Realm para operações de banco de dados (opcional, com valor padrão null).
 * @param userViewModel O [UserViewModel] responsável por gerenciar e fornecer os dados de usuários.
 * @param navController O [NavController] para navegação entre telas.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserExplorerScreen(
    realm: Realm? = null,
    userViewModel: UserViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current
    val currentUser by userViewModel.myUser.collectAsState(null)
    val authToken by userViewModel.token.collectAsState("")
    val userList by userViewModel.users.collectAsState(emptyList())

    var isMenuExpanded by remember { mutableStateOf(false) }
    var isLoadingMoreUsers by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }
    val lazyListState = rememberLazyListState()
    var isInitialLoaderVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.setUsers(emptyList())
        userViewModel.build(realm = realm, context = context)
    }

    LaunchedEffect(currentPage) {
        isLoadingMoreUsers = true
        val newUsers = userViewModel.findRandomUsers(currentPage)
        userViewModel.setUsers(userList + newUsers)
        isLoadingMoreUsers = false
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .map {
                val lastVisibleItemIndex =
                    lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItemsCount = lazyListState.layoutInfo.totalItemsCount
                totalItemsCount > 0 && (lastVisibleItemIndex >= totalItemsCount - 5) && !isLoadingMoreUsers
            }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) {
                    currentPage++
                }
            }
    }

    Scaffold(
        topBar = {
            ExplorerTopAppBar(
                currentUser = currentUser,
                authToken = authToken,
                navController = navController,
                isMenuExpanded = isMenuExpanded,
                onMenuExpandChange = { isMenuExpanded = it }
                // dynamicOptions pode ser adicionado aqui se houver um menu de opções real
            )
        },
        floatingActionButton = {
            NewChatFloatingActionButton(
                onFindRandomUser = {
                    isInitialLoaderVisible = true
                    userViewModel.viewModelScope.launch {
                        userViewModel.randomUser()?.let { foundUser ->
                            userViewModel.setUsers(listOf(foundUser)) // Limpa a lista e mostra só o novo usuário
                            navController.navigate("chat/${foundUser.uuid}")
                        }
                        isInitialLoaderVisible = false
                    }
                }
            )
        },
        bottomBar = { NavigationBarApp(navController) }
    ) { paddingValues ->
        UserListContent(
            userList = userList,
            lazyListState = lazyListState,
            authToken = authToken,
            isLoadingMore = isLoadingMoreUsers,
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )

        if (isInitialLoaderVisible) {
            FullScreenLoader()
        }
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@Composable
@Preview(showBackground = true)
fun UserExplorerScreenPreview() {
    MeetTalkTheme {
        UserExplorerScreen()
    }
}