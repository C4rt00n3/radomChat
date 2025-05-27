package com.example.meettalk.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.entities.UserEntity
import com.example.meettalk.ui.theme.TextColorGray

@Composable
fun ChatHeader(
    user: UserEntity?,
    width: Dp,
    options: List<OptionsMenu>
) {
    Row (
        modifier = Modifier
            .width(width * 0.9f)
            .padding(vertical = 16.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton (
                onClick = {},
                modifier = Modifier.border(3.dp, TextColorGray, CircleShape)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = "Foto do usuário"
                )
            }
            Text(
                user?.name ?: "Carregando...",
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 7.dp)
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color.Green, CircleShape)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(R.drawable.phone_enabled),
                    contentDescription = "Chamada"
                )
            }
            MenuSelect(options)
        }
    }
}
