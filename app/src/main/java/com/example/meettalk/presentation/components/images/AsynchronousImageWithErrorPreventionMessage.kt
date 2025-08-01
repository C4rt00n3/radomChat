package com.example.meettalk.presentation.components.images

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log // Importar Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.ImageMessage
import com.example.meettalk.presentation.viewmodel.UserViewModel
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CancellationException // Importar CancellationException

@Composable
fun AsynchronousImageWithErrorPreventionMessage(
    imageMessage: ImageMessage? = null,
    downloadOn: Boolean = true,
    token: String,
    chatUuid: String,
    contentDescription: String?,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    placeholder: Painter = painterResource(id = R.drawable.img),
    error: Painter = painterResource(id = R.drawable.img),
    fallback: Painter? = error,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    onBytesReady: ((ByteArray?) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    userViewModel: UserViewModel = viewModel(),
) {
    val url =
        stringResource(R.string.baseUrl) + "/message/image/${imageMessage?.uuid}/$chatUuid"
    val fullUrl = stringResource(R.string.baseUrl) + "/message/image/${imageMessage?.uuid}/$chatUuid"


    if (imageMessage?.src == null) {
        val context = LocalContext.current
        val imageLoader = ImageLoader(context)

        val request = remember(url, token) {
            ImageRequest.Builder(context)
                .data(url)
                .apply {
                    if (token.isNotEmpty()) {
                        addHeader("Authorization", token)
                    }
                    placeholder(R.drawable.img)
                    error(R.drawable.img)
                    allowHardware(false)
                }.build()
        }

        LaunchedEffect(url, token) {
            if (imageMessage == null || imageMessage.src != null) return@LaunchedEffect
            if (downloadOn) {
                try {
                    val result = imageLoader.execute(request)
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap

                    if (bitmap != null) {
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        val byteArray = stream.toByteArray()

                        onBytesReady?.invoke(byteArray)

                        userViewModel.saveMessageImage(imageMessage, byteArray)
                    } else {
                        Log.w("ImageDownload", "Falha ao obter bitmap do drawable: ${result.drawable}")
                    }
                } catch (e: CancellationException) {
                    // Ignorar CancellationException - é um comportamento normal do ciclo de vida do Composable
                    Log.d("ImageDownload", "Download de imagem cancelado: ${e.message}")
                } catch (e: Exception) {
                    Log.e("ImageDownload", "URL da Imagem com erro: $fullUrl")
                    Log.e("ImageDownload", "Erro REAL ao baixar imagem: ${e.message}", e)
                }
            }
        }

        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            error = error,
            fallback = fallback,
            onLoading = onLoading,
            onSuccess = onSuccess,
            onError = { state ->
                onError?.invoke(state)
            },
            placeholder = placeholder,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality
        )
    } else {
        AsyncImage(
            model = imageMessage.src,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            error = error,
            fallback = fallback,
            onLoading = onLoading,
            onSuccess = onSuccess,
            onError = { state ->
                onError?.invoke(state)
            },
            placeholder = placeholder,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality
        )
    }
}