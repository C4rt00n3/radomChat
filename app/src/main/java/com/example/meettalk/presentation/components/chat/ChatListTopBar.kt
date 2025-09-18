package com.example.meettalk.presentation.components.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.components.rememberDynamicOptionsListChats
import com.example.meettalk.presentation.ui.AppRoutes
import com.example.meettalk.presentation.ui.ChatListScreen
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import io.realm.kotlin.Realm

/**
 * Barra superior da tela de lista de chats.
 * Contém a imagem de perfil do usuário logado e uma barra de pesquisa.
 *
 * @param currentUser O [User] logado.
 * @param authToken O token de autenticação para carregar imagens.
 * @param navController O [NavController] para navegação.
 * @param searchQuery A string de pesquisa atual.
 * @param onSearchQueryChange Callback para atualizar a string de pesquisa.
 * @param isSearchActive Indica se a barra de pesquisa está ativa.
 * @param onSearchActiveChange Callback para mudar o estado de ativação da barra de pesquisa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListTopBar(
    currentUser: User?,
    authToken: String,
    navController: NavController,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    chatViewModel: ChatViewModel = viewModel(),
    realm: Realm?,
    onSearchActiveChange: (Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedChats by chatViewModel.selectedChats.collectAsState(emptyList())
    val dynamicOptions = realm?.let { rememberDynamicOptionsListChats(selectedChats, it) }

    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer).padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
            ),
            navigationIcon = {
                val profileImage = currentUser?.profileImages?.find { it.slot == 1 }
                    ?: currentUser?.profileImages?.firstOrNull()

                AsynchronousImageWithErrorPrevention(
                    imageProfile = profileImage,
                    token = authToken,
                    contentDescription = stringResource(R.string.content_description_user_profile_image),
                    modifier = Modifier
                        .clickable {
                            currentUser?.uuid?.let { navController.navigate("${AppRoutes.USER_PROFILE}/$it") }
                        }
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            },
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { isTraversalGroup = true },
                    contentAlignment = Alignment.Center
                ) {
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = searchQuery,
                                onQueryChange = onSearchQueryChange,
                                expanded = isSearchActive,
                                onExpandedChange = onSearchActiveChange,
                                placeholder = { Text(stringResource(R.string.search_chat_placeholder)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = stringResource(R.string.content_description_search_icon)
                                    )
                                },
                                trailingIcon = {
                                    if (isSearchActive) {
                                        IconButton(onClick = {
                                            if (searchQuery.isNotEmpty()) {
                                                onSearchQueryChange("")
                                            } else {
                                                onSearchActiveChange(false)
                                            }
                                        }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = stringResource(R.string.content_description_clear_search)
                                            )
                                        }
                                    }
                                },
                                onSearch = { onSearchActiveChange(false) },
                            )
                        },
                        expanded = isSearchActive,
                        onExpandedChange = onSearchActiveChange,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .semantics { traversalIndex = 0f }
                            .padding(horizontal = 8.dp),
                        colors = SearchBarDefaults.colors(containerColor = Color.Transparent),
                        content = {}, // Conteúdo da SearchBar quando expandida (resultados da pesquisa)
                    )
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
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(
                                R.string.menu
                            )
                        )
                    }
                    DropdownMenu(expanded = expanded,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onDismissRequest = { expanded = !expanded }) {
                        dynamicOptions?.forEach {
                            DropdownMenuItem(text = { Text(it.text) }, onClick = {
                                expanded = false
                                it.onClick()
                            })
                        }
                    }
                }
            }, // Ações da barra superior (vazio no design atual)
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
        )
        Spacer(Modifier.padding(top = 16.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Preview ChatListScreen - Light Theme")
@Composable
fun ChatListTopBarPreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MeetTalkTheme(darkTheme = false) {
            ChatListTopBar(null, "", rememberNavController(), "", {}, false, realm = null) {}
        }
    }
}