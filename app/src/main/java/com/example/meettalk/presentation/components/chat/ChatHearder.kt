package com.example.meettalk.presentation.components.chat

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.MenuSelect
import com.example.meettalk.ui.theme.TextColorGray

@Composable
fun ChatHeader(
    user: User?,
    width: Dp,
    token: String,
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
                val baseUrl = stringResource(R.string.baseUrl)
                val model = "$baseUrl/image-profile/${user?.profileImages?.getOrNull(0)?.uuid}"
                val imageRequest = ImageRequest.Builder(LocalContext.current)
                    .data(model)
                    .addHeader("Authorization", "$token")
                    .crossfade(true)
                    .build()
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "User's image. This image is personally chosen by the user.",
                    placeholder = painterResource(R.drawable.img),
                    error = painterResource(R.drawable.img),
                    contentScale = ContentScale.Crop
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
