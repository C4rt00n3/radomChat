package com.example.meettalk.presentation.components.user

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.User

/**
 * Item individual na lista de usuários.
 *
 * @param user O [User] a ser exibido.
 * @param imageProfile A [ImageProfile] do usuário (pode ser nula).
 * @param authToken O token de autenticação para carregar a imagem.
 * @param context O [Context] local.
 * @param onProfileClick Callback invocado quando a imagem de perfil é clicada.
 * @param onChatClick Callback invocado quando o item da lista é clicado (para iniciar chat).
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserListItem(
    user: User,
    imageProfile: ImageProfile?,
    authToken: String,
    context: android.content.Context,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Column {
                Text(user.name)
                Text(
                    text = stringResource(R.string.idade_format, user.age.toString()),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        leadingContent = {
            val imageRequest = ImageRequest.Builder(context)
                .data(stringResource(R.string.baseUrl) + "/image-profile/${imageProfile?.uuid}")
                .apply {
                    if (authToken.isNotEmpty()) {
                        addHeader("Authorization", authToken)
                    }
                    placeholder(R.drawable.img)
                    error(R.drawable.img)
                    allowHardware(false)
                }
                .build()
            IconButton(onClick = onProfileClick) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = stringResource(R.string.imagem_do_usuario),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onChatClick)
    )
}