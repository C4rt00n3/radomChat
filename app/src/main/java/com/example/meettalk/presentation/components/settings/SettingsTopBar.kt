package com.example.meettalk.presentation.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meettalk.R
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.ui.AppRoutes
import com.example.meettalk.presentation.viewmodel.UserViewModel

/**
 * Barra superior para a tela de configurações.
 * Exibe a imagem de perfil do usuário logado e um ícone de menu de opções.
 *
 * @param currentUser O [User] logado cujos detalhes serão exibidos.
 * @param userToken O token do usuário para carregar a imagem de perfil.
 * @param navController O [NavController] para navegação.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(
    currentUser: com.example.meettalk.data.local.model.entities.User?,
    userToken: String,
    navController: NavController,
    userViewModel: UserViewModel
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp),
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
                    token = userToken,
                    contentDescription = stringResource(R.string.content_description_user_profile_image),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable {
                            currentUser?.uuid?.let {
                                navController.navigate("${AppRoutes.USER_PROFILE}/${it}")
                            }
                        },
                    contentScale = ContentScale.Crop,
                    userViewModel = userViewModel
                )
            },
            title = {},
            actions = {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.content_description_open_options_menu)
                )
            },
        )
        Spacer(Modifier.padding(top = 16.dp))
    }
}