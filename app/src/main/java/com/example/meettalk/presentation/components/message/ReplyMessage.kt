package com.example.meettalk.presentation.components.message

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.ui.theme.BlueOther
import com.example.meettalk.ui.theme.BrandColor

/**
 * Componente Composable para exibir o preview de uma mensagem que está sendo respondida.
 * Simula a interface de resposta do WhatsApp com uma barra lateral colorida e o conteúdo da mensagem.
 *
 * @param message O objeto Message que está sendo respondido. Pode ser nulo se não houver resposta.
 * @param currentUser O objeto User do usuário atualmente logado. Usado para determinar se a mensagem
 * respondida foi enviada pelo próprio usuário (para cores e texto "Você").
 * @param modifier Modificador para aplicar a este componente.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ReplyMessage(
    message: Message?,
    currentUser: User?,
    isMi: Boolean,
    modifier: Modifier = Modifier,
    max: Boolean = false
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val context = LocalContext.current
    message?.let { msg ->
        Row(
            modifier = modifier
                .then(
                    Modifier
                        .run {
                            if (!max) wrapContentWidth() else fillMaxWidth()
                        }
                        .widthIn(min = screenWidth * 0.4f, max = screenWidth * 0.85f)
                        .height(65.dp)
                        .drawBehind {
                            drawLine(
                                color = if (isMi) BrandColor else BlueOther,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 8f
                            )
                        }
                        .clip(RoundedCornerShape(8.dp)) // mantém o mesmo arredondamento
                        .background(
                            Color.White.copy(0.3f)
                        )
                        .clickable {
                            Toast.makeText(context, "Resposta: ${msg.text}", Toast.LENGTH_SHORT).show()
                        }
                        .padding(start = 8.dp, end = 8.dp) // afastar texto da borda
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .background(
                        color = if (isMi) BrandColor else BlueOther,
                        shape = RoundedCornerShape(topStart = 15.dp, bottomStart = 15.dp)
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = if (isMi) "Eu" else currentUser?.name ?: "",
                    fontWeight = FontWeight.Bold,
                    color = if (isMi) BrandColor else BlueOther,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = msg.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReplyMessagePreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            ReplyMessage(
                message = Message(
                    uuid = "uuid_msg1",
                    senderId = "user_other_id",
                    text = "Aí depende do tamanho do resumo q vá fazer",
                    chatId = "chat_123",
                    createdAt = "2025-05-29T22:00:00Z"
                ),
                currentUser = User(
                    uuid = "user_me_id",
                    name = "Daniel",
                    gender = Gender.M,
                    age = 19
                ),
                modifier = Modifier.padding(bottom = 8.dp),
                isMi = false
            )

            ReplyMessage(
                message = Message(
                    uuid = "uuid_msg2",
                    senderId = "user_me_id",
                    text = "Oi",
                    chatId = "chat_123",
                    createdAt = "2025-05-29T22:01:00Z"
                ),
                currentUser = User(uuid = "1", name = "Eu", gender = Gender.M, age = 18),
                modifier = Modifier.padding(bottom = 8.dp),
                isMi = true
            )
        }
    }
}