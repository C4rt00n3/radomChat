package com.example.meettalk.presentation.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProfileImageBox(
    label: String,
    imageUrl: String,
    size: Dp,
    token: String,
    onClick: () -> Unit
) {
    val image = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .addHeader("Authorization", token)
        .crossfade(true)
        .build()

    Box(
        modifier = Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.background.copy(0.4f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Text(label, modifier = Modifier.align(Alignment.Center))
        AsyncImage(
            model = image,
            contentDescription = "Profile image $label",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
        )
    }
}