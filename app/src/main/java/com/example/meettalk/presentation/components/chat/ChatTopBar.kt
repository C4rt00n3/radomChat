package com.example.meettalk.presentation.components.chat

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.message.SearchMessagesBar
import com.example.meettalk.presentation.components.profile.ProfileHeader
import com.example.meettalk.ui.theme.MeetTalkTheme

private const val HORIZONTAL_PADDING_FRACTION = 0.05f
private const val TOP_BAR_CONTENT_WIDTH_FRACTION = 0.9f
private val DEFAULT_SPACER_HEIGHT = 16.dp

/**
 * Barra superior da tela de Chat.
 * Contém a imagem de perfil do usuário, um menu de opções e uma barra de busca.
 *
 * @param user O usuário logado.
 * @param token O token de autenticação.
 * @param searchQuery O texto atual da busca.
 * @param onSearchQueryChanged Callback para quando o texto da busca é alterado.
 * @param onClearSearch Callback para limpar o campo de busca.
 * @param onProfileClicked Callback para quando a imagem de perfil é clicada.
 * @param optionsMenuItems Lista de opções para o menu.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ChatTopBar(
    user: User?,
    token: String,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    showInput: Boolean = false,
    modifier: Modifier = Modifier,
    onClearSearch: () -> Unit,
    onProfileClicked: () -> Unit,
    optionsMenuItems: List<OptionsMenu>
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = screenWidth * HORIZONTAL_PADDING_FRACTION
    val contentWidth = screenWidth * TOP_BAR_CONTENT_WIDTH_FRACTION

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = horizontalPadding, end = horizontalPadding)
            .apply {
                showInput ?: drawBottomBorder()
            }
    ) {
        ProfileHeader(
            modifier = Modifier
                .width(contentWidth)
                .padding(bottom = DEFAULT_SPACER_HEIGHT),
            user = user,
            token = token,
            onProfileClicked = onProfileClicked,
            optionsMenuItems = optionsMenuItems
        )
        showInput ?: SearchMessagesBar(
            modifier = Modifier.fillMaxWidth(),
            searchQuery = searchQuery,
            onSearchQueryChanged = onSearchQueryChanged,
            onClearSearch = onClearSearch
        )
        showInput ?: Spacer(modifier = Modifier.height(DEFAULT_SPACER_HEIGHT))
    }
}


/**
 * Modificador customizado para desenhar uma linha na parte inferior do Composable.
 *
 * @param color A cor da linha.
 * @param strokeWidth A espessura da linha.
 */
fun Modifier.drawBottomBorder(
    color: Color = Color.LightGray, // Poderia vir do Theme
    strokeWidth: Float = 1.dp.value // Usar dp.toPx() dentro do drawBehind se precisar de conversão
): Modifier = this.then(Modifier.drawBehind {
    drawLine(
        color = color, start = Offset(
            0f, size.height - strokeWidth / 2
        ), end = Offset(size.width, size.height - strokeWidth / 2), strokeWidth = strokeWidth
    )
})

/**
 * Preview do Composable `ChatTopBar`.
 * Permite visualizar o componente no Android Studio sem a necessidade de rodar no dispositivo.
 */
@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun ChatTopBarPreview() {
    MeetTalkTheme {
        ChatTopBar(
            null,
            token = "",
            searchQuery = "TODO()",
            onSearchQueryChanged = {},
            showInput = false,
            onClearSearch = {},
            onProfileClicked = {},
            optionsMenuItems = listOf()
        )
    }
}