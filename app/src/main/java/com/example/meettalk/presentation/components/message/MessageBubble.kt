package com.example.meettalk.presentation.components.message

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.UserAvatar
import com.example.meettalk.ui.theme.BlueOther
import com.example.meettalk.ui.theme.BrandColor
import com.example.meettalk.ui.theme.BrandGreen
import com.example.meettalk.ui.theme.MessageColor
import com.example.meettalk.utils.formatarDataAmigavel

/**
 * Um componente Composable que exibe um balão de mensagem em um chat.
 * Adapta-se ao tema (claro/escuro) e ao tamanho do conteúdo, garantindo legibilidade.
 * O tamanho do balão agora se ajusta ao texto, com uma largura máxima para mensagens longas.
 * Adiciona funcionalidade de "Ver mais" para mensagens com mais de 150 caracteres.
 * O posicionamento do avatar e o alinhamento da mensagem são controlados por `isMi`.
 *
 * @param message O objeto [Message] contendo o texto e o timestamp.
 * @param user O objeto [User] do remetente da mensagem.
 * @param isMi Booleano indicando se a mensagem foi enviada pelo usuário atual (true) ou por outro (false).
 * @param token Token de autenticação, usado para carregar imagens de perfil, se necessário.
 * @param modifier Um [Modifier] para aplicar a este Composable.
 * @param onLongPress Callback para o evento de clique longo no balão.
 * @param onTap Callback para o evento de clique simples no balão.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageBubble(
    message: Message,
    user: User?,
    isMi: Boolean = false, // isMyMessage
    token: String,
    navigate: NavHostController,
    modifier: Modifier = Modifier,
    reply: @Composable () -> Unit,
    onLongPress: () -> Unit = {},
    onTap: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "PressScaleAnimation"
    )

    val bubbleColor = if (isMi) MessageColor else MaterialTheme.colorScheme.surfaceVariant
    val messageTextColor = if (isMi) Color.White else MaterialTheme.colorScheme.onSurface
    val timestampColor =
        if (isMi) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    val userNameColor = BrandGreen

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxBubbleWidth =
        screenWidth * 0.8f
    val minBubbleWidth =
        screenWidth * 0.4f

    val showReadMore = message.text.length > 150
    val displayedText = if (showReadMore && !expanded) {
        message.text.take(150) + "..."
    } else {
        message.text
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(horizontal = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onLongPress = {
                        onLongPress()
                    },
                    onTap = {
                        onTap()
                    }
                )
            },
        horizontalArrangement = if (isMi) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        user?.profileImages?.getOrNull(0)?.let { firstImage ->
            if(!isMi) UserAvatar(
                userImageUrl = firstImage.uuid,
                userName = user.name,
                token = token,
                src = firstImage.src
            ) {
                navigate.navigate("user/${user.uuid}")
            }
        }
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .widthIn(min = minBubbleWidth, max = maxBubbleWidth)
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMi) 16.dp else 4.dp,
                        bottomEnd = if (isMi) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {

            Text(
                text =  if(isMi) "Você" else user?.name ?: "...",
                color = if(!isMi)userNameColor else BrandColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))

            reply()
            Text(
                text = displayedText,
                color = messageTextColor,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                fontSize = 18.sp
            )

            if (showReadMore && !expanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ver mais",
                    color = messageTextColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(top = 4.dp)
                )
            } else if (showReadMore && expanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ver menos",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { expanded = false }
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formatarDataAmigavel(message.createdAt),
                    color = timestampColor,
                    style = MaterialTheme.typography.labelSmall
                )
                if (isMi)
                    Icon(
                        painter = painterResource(R.drawable.checkmarkdoneoutline),
                        contentDescription = if (message.isRead)
                            stringResource(R.string.mensagem_visualizada)
                        else stringResource(
                            R.string.mensagem_n_o_visualizada
                        ),
                        modifier = Modifier.size(12.dp),
                        tint = if (message.isRead) BlueOther else Color.Gray
                    )
            }
        }

        if (isMi) {
            Spacer(modifier = Modifier.width(8.dp))
            user?.profileImages?.getOrNull(0)
                ?.let { UserAvatar(it.uuid, userName = user.name, token = token, src = it.src) }
        }
    }
}