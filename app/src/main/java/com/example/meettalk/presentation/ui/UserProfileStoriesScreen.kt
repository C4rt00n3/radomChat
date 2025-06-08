package com.example.meettalk.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.data.local.model.entities.ImageProfile // Importe ImageProfile
import com.example.meettalk.data.local.model.body.enums.Gender // Importe Gender
import com.example.meettalk.presentation.components.chat.ChatTopBar
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.viewmodel.UserViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserProfileStoriesScreen(uuid: String, userViewModel: UserViewModel) {
    var user: User? by remember { mutableStateOf(null) }
    val token by userViewModel.token.collectAsState("")
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(uuid) {
        userViewModel.findUser(uuid)?.let {
            user = it
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                user = user,
                token = token,
                modifier = Modifier.safeContentPadding() ,
                searchQuery = searchQuery,
                showInput = true,
                onSearchQueryChanged = { query -> searchQuery = query },
                onClearSearch = { /* Lógica de limpar busca */ },
                onProfileClicked = { /* Lógica ao clicar no perfil */ },
                optionsMenuItems = listOf(
                    OptionsMenu("Editar perfil") { /* Lógica de editar perfil */ },
                    OptionsMenu("Logout") { /* Lógica de logout */ }
                )
            )
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val imageUrls = user?.profileImages?.map { imageProfile ->
                imageProfile.uuid.let { uuid ->
                    "${stringResource(R.string.baseUrl)}/image-profile/${uuid}"
                }
            }?.filterNotNull() ?: emptyList()

            ImageCarouselFullScreen(images = imageUrls)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 0f,
                            endY = 500f
                        )
                    )
                    .padding(
                        bottom = 32.dp,
                        start = 16.dp,
                        end = 16.dp,
                        top = 64.dp
                    ),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = user?.name ?: "...",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gênero: ${user?.gender?.name ?: "..."}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "Idade: ${user?.age ?: "..."} anos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarouselFullScreen(images: List<String>) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val imageUrl = images[page]
            AsynchronousImageWithErrorPrevention(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .placeholder(R.drawable.img)
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagem de perfil do usuário",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}