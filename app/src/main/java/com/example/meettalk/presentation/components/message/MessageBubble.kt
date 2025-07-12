package com.example.meettalk.presentation.components.message

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.core.Icon
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.data.local.model.body.enums.MessageType
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.ui.theme.BackgroundBlack
import com.example.meettalk.ui.theme.BlueOther
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.ui.theme.MessageColor
import com.example.meettalk.utils.formatarDataAmigavel
import kotlinx.coroutines.delay

val messageExample = Message(
    uuid = "5319357e-5eb4-4bba-bf45-fc25048f107a",
    text = "Lorem ipsum dolor sit amet . Os operadores gráficos e tipográficos sabem disso bem, na realidade, todas as profissões que lidam com o universo da comunicação têm um relacionamento estável com essas palavras, mas o que é? Lorem ipsum é um texto fofo sem qualquer sentido.",
    type = MessageType.TEXT,
    url = null,
    chatId = "ecfae7f0-a196-48a1-8036-712dcaf822e3",
    createdAt = "2025-06-18T15:22:11.598Z",
    senderId = "869d46be-8dea-4b08-a031-ed19cc6ddd04",
    receiverId = "3beba2e2-3fc8-4acc-b7c0-51c78d6ffae3",
    isRead = true,
    replyToId = null,
    isUpdate = false,
    updateAt = "2025-06-18T15:22:11.598Z",
    countUpdate = 0,
    isSend = false
)

val message1 = Message(
    uuid = "5319357e-5eb4-4bba-bf45-fc25048f107a",
    text = "Tô bem, e você?",
    type = MessageType.TEXT,
    url = null,
    chatId = "ecfae7f0-a196-48a1-8036-712dcaf822e3",
    createdAt = "2025-06-18T15:22:11.598Z",
    senderId = "869d46be-8dea-4b08-a031-ed19cc6ddd04",
    receiverId = "3beba2e2-3fc8-4acc-b7c0-51c78d6ffae3",
    isRead = false,
    replyToId = null,
    isUpdate = false,
    updateAt = "2025-06-18T15:22:11.598Z",
    countUpdate = 0,
    isSend = false
)

val user = User(
    uuid = "3beba2e2-3fc8-4acc-b7c0-51c78d6ffae3",
    name = "Sandro Cato",
    gender = Gender.M,
    age = 28,
    profileImages = listOf(
        ImageProfile("028752e1-ffb4-4459-ba32-b234183f43b8"),
        ImageProfile("3c9c6d6f-4a85-401e-bcc2-e661a8713592"),
        ImageProfile("8d6cc6ba-6faa-49e8-9607-2e505229866f"),
    )
)

@Composable
fun Interrogation(message: Message?, isMi: Boolean) {
    if (isMi && message?.isSend == false) {
        var showErrorIcon by remember { mutableStateOf(false) }

        LaunchedEffect (message.uuid) {
            delay(7000L)
            showErrorIcon = true
        }

        Spacer(Modifier.padding(4.dp))

        if (!showErrorIcon) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )

            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Mensagem ainda não enviada",
                modifier = Modifier.size(12.dp),
                tint = Color.Transparent
            )
        } else {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Mensagem não foi enviada",
                modifier = Modifier.size(12.dp),
                tint = Color.Red
            )
        }
    }
}

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
 * @param onSwipe Callback para o evento de arrastar para o lado no balão.
 */
@SuppressLint("ConfigurationScreenWidthHeight", "UseOfNonLambdaOffsetOverload")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageBubble(
    message: Message? = messageExample,
    reply: Message? = message1,
    isMi: Boolean = true,
    isSelected: Boolean = false,
    clickInReply: (Message) -> Unit = { _ -> },
    onTap: (Message) -> Unit = {},
    onLongPress: (Message) -> Unit = {},
    onSwipe: (Message) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val isDarkTheme = isSystemInDarkTheme()
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxOffsetX = 50.dp
    val swipeThreshold = 300.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val colorSelected = (if (isDarkTheme) Color.Black else Color.White).copy(0.1f)
    var isMessageExpanded by remember { mutableStateOf(false) }
    val maxLinesCollapsed = 4
    val messageTextLimit = 150

    val swipeThresholdPx = with(density) { swipeThreshold.toPx() }

    Row(horizontalArrangement = if (isMi) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) colorSelected else Color.Transparent)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(onDragEnd = {
                    with(density) {
                        println(offsetX)
                        println(swipeThresholdPx)
                        if (offsetX >= (swipeThresholdPx / 6) || offsetX <= -(swipeThresholdPx / 6)) {
                            message?.let { onSwipe(it) }
                        }
                        offsetX = 0f
                    }
                }, onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    val newOffsetX = (offsetX + dragAmount)
                    with(density) {
                        offsetX = if (isMi) {
                            newOffsetX.coerceAtMost(0f).coerceAtLeast(-maxOffsetX.toPx())
                        } else {
                            newOffsetX.coerceAtLeast(0f).coerceAtMost(maxOffsetX.toPx())
                        }
                    }
                })
            }
            .offset(x = offsetX.dp)
            .combinedClickable(onClick = { message?.let { onTap(it) } },
                onLongClick = { message?.let { onLongPress(it) } })
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            reply?.let {
                Row(
                    modifier = Modifier
                        .widthIn(min = screenWidth * 0.2f, max = screenWidth * 0.7f)
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = {
                            clickInReply(it)
                        })
                        .background(BackgroundBlack)
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .background(MaterialTheme.colorScheme.primary)
                            .align(Alignment.CenterVertically)
                            .height(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = it.text,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Box(
                modifier = Modifier
                    .widthIn(min = screenWidth * 0.3f, max = screenWidth * 0.8f)
                    .background(
                        color = if (isMi) MessageColor else BackgroundBlack,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomEnd = if (!isMi) 8.dp else 0.dp,
                            bottomStart = if (isMi) 8.dp else 0.dp
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = message?.text ?: "Aguarde...",
                        color = Color.White,
                        fontSize = 16.sp,
                        maxLines = if (isMessageExpanded) Int.MAX_VALUE else maxLinesCollapsed,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if ((message?.text?.length ?: 0) > messageTextLimit && !isMessageExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Ver mais",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable { isMessageExpanded = true }
                                .padding(top = 4.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            formatarDataAmigavel(message?.createdAt ?: ""),
                            fontSize = 12.sp,
                            color = Color.Transparent
                        )
                        if (isMi && message?.isSend == true) {
                            Spacer(Modifier.padding(4.dp))
                            Icon(
                                painter = painterResource(R.drawable.checkmarkdoneoutline),
                                modifier = Modifier.size(16.dp),
                                contentDescription = if (message.isRead == true) "Visualizada" else "Não visualizada",
                                tint = Color.Transparent
                            )
                        }
                        if (isMi && message?.isSend == false) {
                            Spacer(Modifier.size(12.dp))
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(
                        Alignment.BottomEnd
                    )
                ) {
                    Text(
                        formatarDataAmigavel(message?.createdAt ?: ""),
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    if (isMi && message?.isSend == true) {
                        Spacer(Modifier.padding(4.dp))
                        Icon(
                            painter = painterResource(R.drawable.checkmarkdoneoutline),
                            modifier = Modifier.size(16.dp),
                            contentDescription = if (message.isRead) "Visualizada" else "Não visualizada",
                            tint = if (message.isRead) BlueOther else Color.LightGray
                        )
                    }

                    if (isMi && message?.isSend == false) {
                        Interrogation(message, isMi)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "Preview Tela de Chats - Light Theme")
@Composable
fun MessageBubblePreview() {
    MeetTalkTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            MessageBubble()
        }
    }
}