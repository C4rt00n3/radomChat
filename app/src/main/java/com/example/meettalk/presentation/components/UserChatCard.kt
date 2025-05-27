package com.example.meettalk.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.ChatEntity
import com.example.meettalk.data.local.model.entities.UserEntity
import com.example.meettalk.utils.formatToHourMinuteAmPm

@Composable
fun UserChatCard(
    user: UserEntity,
    chat: ChatEntity,
    background: Color,
    onLongPress: () -> Unit,
    onTap: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val message = chat.messages.getOrNull(0)
    val screenWidth = configuration.screenWidthDp.dp
    val messageNotRead =
        chat.messages.count { it.senderId == user.uuid && !it.isRead }
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = ""
    )

    Row(
        Modifier
            .background(background)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(8.dp))
                .padding(
                    vertical = 4.dp,
                    horizontal = 16.dp
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onLongPress = {
                            onLongPress()
                        },
                        onTap = {
                            onTap()
                        }
                    )
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.width(screenWidth * 0.90f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.ic_launcher_background),
                        contentDescription = stringResource(R.string.imagem_do_usuario),
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            user.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        if (message != null) {
                            Text(
                                message.text,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (message != null) {
                        Text(
                            formatToHourMinuteAmPm(message.createdAt.toString()),
                            color = Color(0xFF477BFF),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    if (messageNotRead > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0xFF477BFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = messageNotRead.toString(),
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                }
            }
        }
    }
}