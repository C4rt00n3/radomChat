package com.example.meettalk.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.utils.formatarDataAmigavel
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.MessageEntity
import com.google.gson.Gson

const val message = """{
                "uuid": "aaca2b93-3744-4f0a-9516-0bab4c283f0e",
                "text": "Oi, tudo bem",
                "type": "T",
                "url": null,
                "chat_uuid": "9a180ed0-6035-468c-a972-453a69c51f51",
                "createdAt": "2025-04-11T17:33:21.216Z",
                "sender_id": "3ca35871-218f-4b9d-b37f-5e24119ba44b",
                "receive_id": "63f9c991-d5b2-4a3c-a241-51087a3927d7",
                "isRead": true
            }"""

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageCard(
    message: MessageEntity,
    messageToReply: MessageEntity?,
    isMe: Boolean,
    onLongPress: () -> Unit,
    onTap: () -> Unit
) {
    val bubbleColor = if (isMe) Color(0xFF4CAF50) else Color(0xFF2B2B2B)

    var isPressed by remember { mutableStateOf(false) }

    val shape =
        if (isMe) {
            RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 0.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            )
        } else {
            RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            )
        }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(horizontal = 16.dp)
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
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if(messageToReply != null) Row(modifier = Modifier.padding(bottom = 8.dp)) {
            Box(
                modifier = Modifier
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = strokeWidth
                        )
                    }
                    .padding(start = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(color = Color(0xFF2B2B2B).copy(0.8f), shape = shape)
                        .padding(vertical = 5.dp, horizontal = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            messageToReply.text,
                            color = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(32.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isMe)
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(24.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_background),
                        contentDescription = "User's image. This image is personally chosen by the user."
                    )
                }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(color = bubbleColor, shape = shape)
                    .padding(vertical = 5.dp, horizontal = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        message.text,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        formatarDataAmigavel(message.createdAt.toString()),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
                    )
                    if (isMe) Icon(
                        painter = painterResource(R.drawable.checkmarkdoneoutline),
                        contentDescription = "A mensagem foi lida",
                        tint = if (message.isRead) Color.Cyan else Color.Black,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            if (isMe)
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(24.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_background),
                        contentDescription = "User's image. This image is personally chosen by the user.",
                    )
                }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MessageCardPreview() {
    MeetTalkTheme {
        val gson = Gson()
        MessageCard(
            gson.fromJson(message, MessageEntity::class.java),
            null,
            true,
            onTap = {},
            onLongPress = {})
    }
}