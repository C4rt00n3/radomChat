package com.example.meettalk.presentation.components.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meettalk.ui.theme.BlueOther

/**
 * Badge que exibe o número de mensagens não lidas.
 *
 * @param count O número de mensagens não lidas.
 */
@Composable
fun UnreadMessageCountBadge(count: Int) {
    Box(
        modifier = Modifier
            .offset(x = 6.dp, y = (-6).dp)
            .clip(CircleShape)
            .background(BlueOther)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .size(if (count > 9) 20.dp else 16.dp)
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}