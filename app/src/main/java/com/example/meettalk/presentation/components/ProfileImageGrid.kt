package com.example.meettalk.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.meettalk.presentation.ui.Chat
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme

@Composable
fun ProfileImageBox(
    label: String,
    imageUrl: String,
    size: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.background.copy(0.4f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Text(label, modifier = Modifier.align(Alignment.Center))
        AsyncImage(
            model = imageUrl,
            contentDescription = "Profile image $label",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun ProfileImageGrid(
    chatViewModel: ChatViewModel,
    onImageClick: (String) -> Unit = {},
) {
    val user by chatViewModel.user.collectAsState(null)

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val spacing = 8.dp
    val columns = 3
    val totalSpacing = spacing * (columns - 1)
    val itemSize = (screenWidth - totalSpacing - 40.dp) / columns

    val imagesUrl = listOf(
        "https://via.placeholder.com/300x300.png?text=1",
        "https://via.placeholder.com/150.png?text=2",
        "https://via.placeholder.com/150.png?text=3",
        "https://via.placeholder.com/150.png?text=4",
        "https://via.placeholder.com/150.png?text=5",
        "https://via.placeholder.com/150.png?text=6"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier.padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(itemSize * 2 + spacing)
                    .height(itemSize * 2 + spacing)
                    .background(MaterialTheme.colorScheme.background.copy(0.4f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .clickable { onImageClick("1") }
            ) {
                Text("1", modifier = Modifier.align(Alignment.Center))
                AsyncImage(
                    model = imagesUrl[0],
                    contentDescription = "Profile image 1",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier.height(itemSize * 2 + spacing)
            ) {
                ProfileImageBox(
                    label = "2",
                    imageUrl = imagesUrl[1],
                    size = itemSize,
                    onClick = { onImageClick("2") }
                )
                ProfileImageBox(
                    label = "3",
                    imageUrl = imagesUrl[2],
                    size = itemSize,
                    onClick = { onImageClick("3") }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("4", "5", "6").forEachIndexed { index, label ->
                ProfileImageBox(
                    label = label,
                    imageUrl = imagesUrl[index + 3],
                    size = itemSize,
                    onClick = { onImageClick(label) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileImageGridPreview() {
    MeetTalkTheme {
        val context = LocalContext.current
        val chatViewModel = ChatViewModel(context)
        ProfileImageGrid(chatViewModel) {}
    }
}
