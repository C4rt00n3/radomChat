package com.example.meettalk.presentation.components.explorer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.ui.UserExplorerScreen

/**
 * Barra de topo para a [UserExplorerScreen].
 * Exibe a imagem de perfil e nome do usuário logado, e um menu de opções.
 *
 * @param currentUser O [User] logado cujos detalhes serão exibidos.
 * @param authToken O token de autenticação para carregar imagens.
 * @param navController O [NavController] para navegação.
 * @param isMenuExpanded Indica se o menu de opções está expandido.
 * @param onMenuExpandChange Callback para mudar o estado de expansão do menu.
 * @param menuOptions Uma lista de [OptionsMenu] para o DropdownMenu (atualmente não usada no código original).
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerTopAppBar(
    currentUser: User?,
    authToken: String,
    navController: NavController,
    isMenuExpanded: Boolean,
    onMenuExpandChange: (Boolean) -> Unit,
    menuOptions: List<OptionsMenu> = emptyList()
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
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
                IconButton(onClick = { navController.navigate("user/${currentUser?.uuid}") }) {
                    val profileImage = currentUser?.profileImages?.find { it.slot == 1 }
                        ?: currentUser?.profileImages?.firstOrNull()

                    AsynchronousImageWithErrorPrevention(
                        imageProfile = profileImage,
                        token = authToken,
                        contentDescription = stringResource(R.string.imagem_do_usuario),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(onClick = { navController.navigate("user/${currentUser?.uuid}") }),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.size(8.dp))
                Column {
                    Text(
                        text = currentUser?.name ?: stringResource(R.string.carregando_dados) // Fallback para nome
                    )
                    Text(
                        text = stringResource(
                            R.string.idade_format,
                            currentUser?.age?.toString() ?: stringResource(R.string.indisponivel)
                        ),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                    )
                }
            }
        },
        actions = {
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = { onMenuExpandChange(!isMenuExpanded) }) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.content_description_menu_opcoes)
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