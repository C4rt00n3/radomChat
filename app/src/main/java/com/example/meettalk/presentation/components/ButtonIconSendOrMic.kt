package com.example.meettalk.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.meettalk.R

@Composable
fun ButtonIconSendOrMic(isBlank: Boolean, editOn: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = {
            onClick()
        },
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = Color.Unspecified
        )
    ) {
        if (isBlank && !editOn)
            Icon(
                painter = painterResource(R.drawable.send),
                contentDescription = "Send message icon",
                tint = Color.Unspecified,
                modifier = Modifier.size(35.dp)
            )
        if (!isBlank && !editOn) Icon(
            painter = painterResource(R.drawable.mic),
            contentDescription = "Microphone icon: tap to record a voice message.",
            tint = Color.White,
            modifier = Modifier.size(35.dp)
        )
        if (editOn) Box(
            modifier = Modifier
                .border(BorderStroke(3.dp, Color.White), CircleShape)
        ) {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = "Microphone icon: tap to record a voice message.",
                tint = Color.White,
                modifier = Modifier.size(35.dp).padding(5.dp)
            )
        }
    }
}