package com.example.meettalk.presentation.components.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User

/**
 * Composable for displaying the reply message section within the chat input.
 *
 * @param message The message being replied to.
 * @param currentUser The current logged-in user.
 * @param chat The current chat details.
 * @param onDismissReply Callback to dismiss the reply section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyMessageSection(
    message: Message,
    currentUser: User?,
    chat: Chat?,
    onDismissReply: () -> Unit,
    inputMessage: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = {
            onDismissReply()
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .heightIn(max = 300.dp)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.respondendo_a),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                IconButton(onClick = onDismissReply) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_close_24),
                        contentDescription = stringResource(R.string.fechar),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            val participant = chat?.participants?.firstOrNull { it.userId == message.senderId }
            ReplyMessage(
                message = message,
                currentUser = participant?.user,
                isMi = participant?.userId == currentUser?.uuid,
                modifier = Modifier.padding(horizontal = 16.dp),
                max = true
            )
            Spacer(modifier = Modifier.padding(9.dp))
            inputMessage()
        }
    }
}