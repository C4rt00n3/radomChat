package com.example.meettalk.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention

/**
 * Exibe um carrossel de imagens em tela cheia com um fundo borrado e indicadores de página.
 *
 * @param images A lista de [ImageProfile] a serem exibidas.
 * @param authToken O token de autenticação para carregar as imagens.
 */
@Composable
fun FullScreenImageCarousel(images: List<ImageProfile>, authToken: String) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(modifier = Modifier.fillMaxSize()) {
        val currentImage = images.getOrNull(pagerState.currentPage)

        if (currentImage != null) {
            AsynchronousImageWithErrorPrevention(
                imageProfile = currentImage,
                token = authToken,
                contentDescription = stringResource(R.string.content_description_fundo_borrado),
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 32.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )


        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val profileImage = images.getOrNull(page)
            AsynchronousImageWithErrorPrevention(
                imageProfile = profileImage,
                token = authToken,
                contentDescription = stringResource(R.string.content_description_imagem_perfil_usuario),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(pagerState.pageCount) { pageIndex ->
                val indicatorColor =
                    if (pagerState.currentPage == pageIndex) Color.White else Color.White.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
        }
    }
}