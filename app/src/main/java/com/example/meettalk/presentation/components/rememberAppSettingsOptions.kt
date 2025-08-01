package com.example.meettalk.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.SettingsOption
import com.example.meettalk.data.local.model.SettingsOptionType

/**
 * Composable que gerencia e fornece as opções de configurações dinâmicas.
 * Inclui lógica para carregar mais usuários bloqueados à medida que a lista é rolada.
 *
 * @param navController Controlador de navegação para navegar para perfis de usuário.
 * @return Uma lista de `SettingsOption` configuradas.
 */
@Composable
fun rememberAppSettingsOptions(navController: NavController): List<SettingsOption> {
    var isMarkAsSeenChecked by remember { mutableStateOf(false) }
    var receiveImagesOption by remember { mutableStateOf("Permitir todos") }
    var receiveAudioOption by remember { mutableStateOf("Permitir todos") }

    return listOf(
        SettingsOption(
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.checkmarkdoneoutline),
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
            type = SettingsOptionType.NAVIGATE, // Indica que esta opção pode navegar
            onClick = { /* Implementar navegação para a tela de usuários bloqueados */ }
        ),
        SettingsOption(
            leadingContent = {
                Icon(
                    Icons.Default.ImageNotSupported,
                    contentDescription = "Opções de recebimento de imagem",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            headlineText = "Receber imagens",
            showCheckbox = false,
            type = SettingsOptionType.RADIO_GROUP, // Indica que é um grupo de rádio e que o conteúdo de suporte deve ser expandido/colapsado
            supportingContent = listOf(
                SettingsOption(
                    leadingContent = {},
                    headlineText = "Permitir todos",
                    showCheckbox = false,
                    isChecked = receiveImagesOption == "Permitir todos",
                    onClick = { receiveImagesOption = "Permitir todos" },
                    type = SettingsOptionType.NONE
                ),
                SettingsOption(
                    leadingContent = {},
                    headlineText = "Limitar todos",
                    showCheckbox = false,
                    isChecked = receiveImagesOption == "Limitar todos",
                    onClick = { receiveImagesOption = "Limitar todos" },
                    type = SettingsOptionType.NONE
                ),
                SettingsOption(
                    leadingContent = {},
                    headlineText = "Somente se você permitir",
                    showCheckbox = false,
                    isChecked = receiveImagesOption == "Somente se você permitir",
                    onClick = { receiveImagesOption = "Somente se você permitir" },
                    type = SettingsOptionType.NONE
                )
            )
        ),
        SettingsOption(
            leadingContent = {
                Icon(
                    Icons.Default.MicOff,
                    contentDescription = "Opções de recebimento de áudio",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            headlineText = "Receber áudios",
            showCheckbox = false,
            type = SettingsOptionType.RADIO_GROUP,
            supportingContent = listOf(
                SettingsOption(
                    leadingContent = {},
                    headlineText = "Permitir todos",
                    showCheckbox = false,
                    isChecked = receiveAudioOption == "Permitir todos",
                    onClick = { receiveAudioOption = "Permitir todos" },
                    type = SettingsOptionType.NONE
                ),
                SettingsOption(
                    leadingContent = {},
                    headlineText = "Limitar todos",
                    showCheckbox = false,
                    isChecked = receiveAudioOption == "Limitar todos",
                    onClick = { receiveAudioOption = "Limitar todos" },
                    type = SettingsOptionType.NONE
                ),
                SettingsOption(
                    leadingContent = {},
                    headlineText = "Somente se você permitir",
                    showCheckbox = false,
                    isChecked = receiveAudioOption == "Somente se você permitir",
                    onClick = { receiveAudioOption = "Somente se você permitir" },
                    type = SettingsOptionType.NONE
                )
            )
        ),
        SettingsOption(
            leadingContent = {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = "Sair, fazer lougout",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            headlineText = "Sair",
            showCheckbox = false,
            type = SettingsOptionType.NAVIGATE,
        )
    )
}