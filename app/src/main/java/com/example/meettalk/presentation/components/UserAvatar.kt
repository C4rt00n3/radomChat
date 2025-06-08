package com.example.meettalk.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.meettalk.R
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention

/**
 * Composable auxiliar para exibir o avatar do usuário.
 *
 * @param userImageUrl A URL da imagem de perfil do usuário. Pode ser nula.
 * @param userName O nome do usuário, usado para o placeholder se a imagem não carregar.
 * @param token Token de autenticação para a requisição da imagem, se necessário.
 */
@Composable
fun UserAvatar(
    userImageUrl: String?,
    src: ByteArray? = null,
    userName: String?,
    token: String,
    onTap: () -> Unit = {},
) {
    var error by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val baseUrl = stringResource(R.string.baseUrl)

    val imageRequest = ImageRequest.Builder(context)
        .data("$baseUrl/image-profile/${userImageUrl}")
        .apply {
            if (token.isNotBlank()) {
                addHeader("Authorization", token)
            }
            placeholder(R.drawable.img)
            error(R.drawable.img)
            crossfade(true)
            transformations(CircleCropTransformation())
        }
        .build()

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {

        AsynchronousImageWithErrorPrevention(
            model = imageRequest,
            contentDescription = "Imagem de perfil do usuário ${userName ?: ""}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onTap()
                        }
                    )
                },
            imageInCaseOfError = src
        )

    }
}