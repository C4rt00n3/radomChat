package com.example.meettalk.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.meettalk.data.local.model.entities.ChatEntity
import com.example.meettalk.ui.theme.TextColorGray
import com.example.meettalk.utils.formatDateTimeForChat

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatInfo(
    chat: ChatEntity?,
    selectedCount: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            },
        horizontalArrangement = Arrangement.Center
    ) {
        val message = chat?.messages?.firstOrNull()
        if (message == null) {
            Text("Nenhuma mensagem ainda", fontWeight = FontWeight.Bold, color = TextColorGray)
        } else {
            val (date, time) = formatDateTimeForChat(message.createdAt)
            Text("$date $time", fontWeight = FontWeight.Bold, color = TextColorGray)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp, end = 16.dp, bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if(selectedCount > 0){
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        onCheckedChange(it)
                    }
                )
                Text(
                    text = "Selecionar tudo",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Text("$selectedCount", fontWeight = FontWeight.SemiBold)
        }
    }
}
