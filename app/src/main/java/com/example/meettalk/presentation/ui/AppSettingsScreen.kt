package com.example.meettalk.presentation.ui

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.R
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import io.realm.kotlin.Realm

/**
 * Representa uma opção de configuração com conteúdo principal, texto e um checkbox opcional.
 *
 * @param leadingContent Composable para o ícone ou imagem à esquerda da opção.
 * @param headlineText O texto principal da opção.
 * @param isChecked Estado atual do checkbox.
 * @param showCheckbox Define se o checkbox deve ser exibido.
 * @param onCheckedChange Callback para quando o estado do checkbox muda.
 * @param supportingContent Composable para conteúdo adicional abaixo do texto principal (ex: lista de usuários bloqueados).
 */
data class SettingsOption(
    val leadingContent: @Composable () -> Unit,
    val headlineText: String,
    val isChecked: Boolean = false,
    val showCheckbox: Boolean = true,
    val onCheckedChange: (Boolean) -> Unit = {},
    val supportingContent: @Composable () -> Unit = {}
)

/**
 * Composable que gerencia e fornece as opções de configurações dinâmicas.
 * Inclui lógica para carregar mais usuários bloqueados à medida que a lista é rolada.
 *
 * @param realm Instância do Realm (atualmente não utilizada, pode ser removida se não houver uso futuro).
 * @param blockedUsers Lista de usuários bloqueados a serem exibidos.
 * @param userViewModel ViewModel para interagir com os dados do usuário e de bloqueio.
 * @param navController Controlador de navegação para navegar para perfis de usuário.
 * @return Uma lista de `SettingsOption` configuradas.
 */
@Composable
fun rememberAppSettingsOptions(navController: NavController): List<SettingsOption> {
    var isMarkAsSeenChecked by remember { mutableStateOf(false) }

    return listOf(
        SettingsOption(
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.checkmarkdoneoutline), // Supondo que checkmarkdoneoutline seja um ícone de "visto"
                    contentDescription = stringResource(R.string.mark_as_seen_icon_description),
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            headlineText = stringResource(R.string.mark_as_unseen_option),
            isChecked = isMarkAsSeenChecked,
            onCheckedChange = { isMarkAsSeenChecked = it }
        ),
        SettingsOption(
            leadingContent = {
                Icon(
                    Icons.Default.Block,
                    contentDescription = stringResource(R.string.blocked_users_icon_description),
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            headlineText = stringResource(R.string.blocked_users_option),
            showCheckbox = false,
        )
    )
}

/**
 * Tela principal de configurações do aplicativo.
 * Exibe opções configuráveis e uma lista de usuários bloqueados.
 *
 * @param realm Instância do Realm (atualmente não utilizada, pode ser removida se não houver uso futuro).
 * @param userViewModel ViewModel para gerenciar os dados do usuário e as operações relacionadas.
 * @param navController Controlador de navegação para a navegação entre telas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    realm: Realm? = null,
    userViewModel: UserViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val currentUser by userViewModel.myUser.collectAsState(null)
    val userToken by userViewModel.token.collectAsState("")
    val context = LocalContext.current
    val settingsOptions = rememberAppSettingsOptions(navController)

    LaunchedEffect(Unit) {
        userViewModel.build(context, realm)
        userViewModel.fetchBlockedUsers(1, 20)
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .safeDrawingPadding()
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .fillMaxSize()
        ) {
            Scaffold(
                topBar = {
                    Column(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent,
                            ),
                            navigationIcon = {
                                val profileImage = currentUser?.profileImages?.find { it.isPrimary }
                                    ?: currentUser?.profileImages?.firstOrNull()

                                AsynchronousImageWithErrorPrevention(
                                    imageProfile = profileImage,
                                    token = userToken,
                                    contentDescription = stringResource(R.string.user_profile_image_description),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            currentUser?.uuid?.let {
                                                navController.navigate("${Routes.USER_PROFILE}/${it}")
                                            }
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            },
                            title = {}, // Título vazio, pois o design não o exibe.
                            actions = {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.open_options_menu_description)
                                )
                            },
                        )
                        Spacer(Modifier.padding(top = 16.dp))
                    }
                }
            ) { paddingValues ->
                LazyColumn(modifier = Modifier.padding(paddingValues)) {
                    items(settingsOptions) { option ->
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = option.leadingContent,
                            headlineContent = {
                                Text(option.headlineText)
                            },
                            trailingContent = {
                                if (option.showCheckbox) {
                                    Checkbox(
                                        checked = option.isChecked,
                                        onCheckedChange = option.onCheckedChange,
                                    )
                                }
                            },
                            supportingContent = option.supportingContent
                        )
                    }
                }
                // O loader inicial foi comentado/removido, pois não estava sendo ativado.
                // Se houver necessidade, reavalie a lógica de exibição.
                /*
                if (showInitialLoader) {
                    Box(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                */
            }
        }
    }
}

/**
 * Preview da tela de configurações.
 * Exibe a tela de configurações em modo de pré-visualização para temas claro e escuro.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Preview Tela de Configurações - Light Theme")
@Composable
fun AppSettingsScreenPreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MeetTalkTheme(darkTheme = false) {
            AppSettingsScreen()
        }
    }
}