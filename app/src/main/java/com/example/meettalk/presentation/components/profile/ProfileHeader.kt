package com.example.meettalk.presentation.components.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.MenuSelect
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.ui.theme.TextColorGray

private val PROFILE_IMAGE_BORDER_SIZE = 3.dp

/**
 * Seção do cabeçalho do perfil na TopBar.
 * Exibe a imagem de perfil e o menu de opções.
 *
 * @param modifier Modificador para este componente.
 * @param user O usuário logado.
 * @param token O token de autenticação.
 * @param onProfileClicked Callback para o clique no perfil.
 * @param optionsMenuItems Itens do menu de opções.
 */
@Composable
fun ProfileHeader(
    modifier: Modifier = Modifier,
    user: User?,
    token: String,
    onProfileClicked: () -> Unit,
    optionsMenuItems: List<OptionsMenu>
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onProfileClicked,
            modifier = Modifier.border(PROFILE_IMAGE_BORDER_SIZE, TextColorGray, CircleShape)
        ) {
            val profile =
                user?.profileImages?.find { it.slot == 1 } ?: user?.profileImages?.firstOrNull()
            AsynchronousImageWithErrorPrevention(
                imageProfile = profile,
                token = token,
                contentDescription = stringResource(R.string.imagem_do_usu_rio),
                placeholder = painterResource(R.drawable.img),
                error = painterResource(R.drawable.img),
            )
        }
        MenuSelect(optionsMenuItems)
    }
}