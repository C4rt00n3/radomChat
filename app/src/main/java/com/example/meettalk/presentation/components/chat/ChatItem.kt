package com.example.meettalk.presentation.components.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.components.message.UnreadMessageCountBadge
import com.example.meettalk.ui.theme.BlueOther
import com.example.meettalk.utils.formatarDataAmigavel

/**
 * Representa um único item na lista de chats.
 *
 * @param chat O [Chat] a ser exibido.
 * @param participantUser O [User] participante do chat (o outro usuário).
 * @param participantProfileImage A [ImageProfile] do participante.
 * @param lastMessage A última [Message] do chat.
 * @param unreadMessageCount O número de mensagens não lidas.
 * @param authToken O token de autenticação para carregar imagens.
 * @param context O [Context] para exibir Toast.
 * @param onChatClick Callback para quando o item do chat é clicado.
 * @param onProfileImageClick Callback para quando a imagem de perfil do participante é clicada.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatItem(
    chat: Chat,
    participantUser: User?,
    participantProfileImage: ImageProfile?,
    lastMessage: Message?,
    unreadMessageCount: Int,
    authToken: String,
    onProfileImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(participantUser?.name ?: stringResource(R.string.unknown_user))
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Box {
                AsynchronousImageWithErrorPrevention(
                    imageProfile = participantProfileImage,
                    token = authToken,
                    contentDescription = stringResource(R.string.content_description_user_profile_image),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onProfileImageClick),
                    contentScale = ContentScale.Crop
                )
                if (chat.fav) {
                    Icon(
                        Icons.Default.Favorite,
                        stringResource(R.string.content_description_favorite_chat),
                        tint = Color.Red,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        },
        trailingContent = {
            if (lastMessage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(formatarDataAmigavel(lastMessage.createdAt))
                    Spacer(Modifier.size(8.dp))
                    if (unreadMessageCount == 0) {
                        Icon(
                            painter = painterResource(R.drawable.checkmarkdoneoutline),
                            contentDescription = if (lastMessage.isRead) stringResource(R.string.message_read) else stringResource(
                                R.string.message_not_read),
                            modifier = Modifier.size(12.dp),
                            tint = if (lastMessage.isRead) BlueOther else Color.Gray
                        )
                    } else {
                        UnreadMessageCountBadge(count = unreadMessageCount)
                    }
                }
            }
        },
        supportingContent = {
            Text(lastMessage?.text ?: "")
        },
        modifier = modifier
            .fillMaxWidth()
    )
}