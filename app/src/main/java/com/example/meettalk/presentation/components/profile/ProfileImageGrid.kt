package com.example.meettalk.presentation.components.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.meettalk.R
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme

/**
 * Espaçamento padrão entre os itens da grade.
 */
private val GRID_SPACING = 8.dp

/**
 * Número de colunas na grade.
 */
private const val GRID_COLUMNS = 3

/**
 * Preenchimento horizontal e vertical aplicado à grade.
 */
private val SCREEN_PADDING = 20.dp

/**
 * `ProfileImageGrid` é um Composable que exibe as imagens de perfil do usuário em um layout de grade.
 * Ele busca os dados do usuário a partir de um `ChatViewModel` e permite interações de clique
 * em cada slot de imagem.
 *
 * @param userViewModel O ViewModel que fornece os dados do usuário, incluindo as imagens de perfil.
 * @param onImageClick Um callback que é invocado quando uma imagem na grade é clicada.
 * Recebe o rótulo (ex: "1", "2") da imagem clicada.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ProfileImageGrid(
    userViewModel: UserViewModel,
    token: String,
    onImageClick: (String) -> Unit = {},
) {
    val baseUrl = stringResource(R.string.baseUrl)
    val user by userViewModel.user.collectAsState(initial = null)

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val totalSpacing = GRID_SPACING * (GRID_COLUMNS - 1)
    val itemSize = (screenWidth - totalSpacing - (SCREEN_PADDING * 2)) / GRID_COLUMNS

    val imageUrls = remember(user) {
        user?.profileImages?.map { "${baseUrl}/image-profile/${it.uuid}" }
            .orEmpty()
            .toMutableList()
            .apply {
                while (size < 6) {
                    add("")
                }
            }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
        modifier = Modifier.padding(SCREEN_PADDING)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileImageBox(
                label = "1",
                token = token,
                imageUrl = imageUrls.getOrElse(0) { "" },
                modifier = Modifier
                    .width(itemSize * 2 + GRID_SPACING)
                    .height(itemSize * 2 + GRID_SPACING),
                onClick = { onImageClick("1") }
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
                modifier = Modifier.height(itemSize * 2 + GRID_SPACING)
            ) {
                ProfileImageBox(
                    label = "2",
                    token = token,
                    imageUrl = imageUrls.getOrElse(1) { "" },
                    modifier = Modifier.size(itemSize),
                    onClick = { onImageClick("2") }
                )
                ProfileImageBox(
                    label = "3",
                    token = token,
                    imageUrl = imageUrls.getOrElse(2) { "" },
                    modifier = Modifier.size(itemSize),
                    onClick = { onImageClick("3") }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
            modifier = Modifier.fillMaxWidth()
        ) {
            (3..5).forEach { index ->
                ProfileImageBox(
                    label = (index + 1).toString(),
                    token = token,
                    imageUrl = imageUrls.getOrElse(index) { "" },
                    modifier = Modifier.size(itemSize),
                    onClick = { onImageClick((index + 1).toString()) }
                )
            }
        }
    }
}

/**
 * `ProfileImageBox` é um Composable que representa uma única caixa de imagem de perfil na grade.
 * Ele exibe uma imagem ou um texto de placeholder se a imagem não estiver disponível.
 *
 * @param label O rótulo da imagem (ex: "1", "2"), usado para acessibilidade e como placeholder.
 * @param imageUrl A URL da imagem a ser carregada. Se vazia, um placeholder será exibido.
 * @param modifier O modificador a ser aplicado ao Box.
 * @param onClick Um callback que é invocado quando a caixa de imagem é clicada.
 */
@Composable
private fun ProfileImageBox(
    label: String,
    token: String,
    imageUrl: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .addHeader("Authorization", token)
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagem de perfil $label",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// --- Preview ---
/**
 * Preview para o Composable `ProfileImageGrid`.
 * Permite visualizar o componente no Android Studio sem a necessidade de rodar no dispositivo.
 */
@Preview(showBackground = true)
@Composable
fun ProfileImageGridPreview() {
    MeetTalkTheme {
        val chatViewModel: UserViewModel =
            viewModel()
        ProfileImageGrid(chatViewModel, "") { /* Sem ação no preview */ }
    }
}