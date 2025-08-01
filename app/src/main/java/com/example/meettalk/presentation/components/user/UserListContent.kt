package com.example.meettalk.presentation.components.user

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meettalk.data.local.model.entities.User

/**
 * Exibe a lista de usuários, com suporte a carregamento paginado e indicadores de progresso.
 *
 * @param userList A lista de [User] a ser exibida.
 * @param lazyListState O [rememberLazyListState] para controlar o estado da lista.
 * @param authToken O token de autenticação para carregar as imagens de perfil.
 * @param isLoadingMore Indica se mais usuários estão sendo carregados (para o indicador no final da lista).
 * @param navController O [NavController] para navegação.
 * @param modifier Modificadores para aplicar ao LazyColumn.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserListContent(
    userList: List<User>,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    authToken: String,
    isLoadingMore: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier,
        state = lazyListState
    ) {
        items(userList, key = { it.uuid }) { user ->
            val imageProfile = user.profileImages.firstOrNull()
            UserListItem(
                user = user,
                imageProfile = imageProfile,
                authToken = authToken,
                context = context,
                onProfileClick = { navController.navigate("user/${user.uuid}") },
                onChatClick = { navController.navigate("chat/${user.uuid}") }
            )
            HorizontalDivider(thickness = 2.dp)
        }
        if (isLoadingMore) {
            item {
                LoadingMoreUsersIndicator()
            }
        }
    }
}