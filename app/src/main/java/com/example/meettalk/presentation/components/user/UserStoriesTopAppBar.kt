package com.example.meettalk.presentation.components.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.ui.UserStoriesScreen

/**
 * Componente da barra de topo para a [UserStoriesScreen].
 * Exibe a imagem de perfil do usuário, nome e um menu de opções.
 *
 * @param user O objeto [User] cujos detalhes serão exibidos.
 * @param userAuthToken O token de autenticação para carregar imagens.
 * @param navController O [NavHostController] para navegação.
 * @param isMenuExpanded Indica se o menu de opções está expandido.
 * @param onMenuExpandChange Callback para mudar o estado de expansão do menu.
 * @param menuOptions Uma lista de [OptionsMenu] para o DropdownMenu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserStoriesTopAppBar(
    user: User?,
    userAuthToken: String,
    navController: NavController,
    isMenuExpanded: Boolean,
    onMenuExpandChange: (Boolean) -> Unit,
    menuOptions: List<OptionsMenu>
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.padding(top = 16.dp),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        title = {},
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                        contentDescription = stringResource(R.string.content_description_voltar)
                    )
                }
                AsynchronousImageWithErrorPrevention(
                    imageProfile = user?.profileImages?.firstOrNull(),
                    token = userAuthToken,
                    contentDescription = stringResource(R.string.imagem_do_usuario),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = user?.name ?: stringResource(R.string.carregando_dados),
                    color = Color.White,
                    fontSize = 18.sp,
                    maxLines = 1
                )
            }
        },
        actions = {
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = { onMenuExpandChange(!isMenuExpanded) }) {
                    Icon(
                        painter = painterResource(R.drawable.menu_burger),
                        contentDescription = stringResource(R.string.content_description_menu_opcoes),
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onDismissRequest = { onMenuExpandChange(false) }
                ) {
                    menuOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.text) },
                            onClick = {
                                onMenuExpandChange(false)
                                option.onClick()
                            }
                        )
                    }
                }
            }
        },
    )
}
