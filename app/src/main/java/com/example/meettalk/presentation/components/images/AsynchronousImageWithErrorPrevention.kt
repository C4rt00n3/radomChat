package com.example.meettalk.presentation.components.images

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.utils.downloadImageAsByteArray
import io.realm.kotlin.Realm
import java.io.ByteArrayOutputStream

@Composable
fun AsynchronousImageWithErrorPrevention(
    imageProfile: ImageProfile? = null,
    downloadOn: Boolean = true,
    token: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
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
    val url = stringResource(R.string.baseUrl) + "/image-profile/${imageProfile?.uuid}"
    val context = LocalContext.current
    val imageLoader = ImageLoader(context)
    val request = remember(url, token) {
        ImageRequest.Builder(context).data(url).apply {
            if (token.isNotEmpty()) {
                addHeader("Authorization", token)
            }
            placeholder(R.drawable.img)
            error(R.drawable.img)
            allowHardware(false)
        }.build()
    }

    LaunchedEffect(url, token, imageProfile) {
        if (imageProfile == null) return@LaunchedEffect
        if (imageProfile.src == null && downloadOn) {
            try {
                val result = imageLoader.execute(request)
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap

                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val byteArray = stream.toByteArray()

                    onBytesReady?.invoke(byteArray)

                    userViewModel.saveProfileImage(imageProfile, byteArray)
                } else {
                    println("Falha ao obter bitmap do drawable: ${result.drawable}")
                }
            } catch (e: Exception) {
                println(url)
                println("Não baixou")
                e.printStackTrace()
                println("Erro ao baixar imagem: ${e.message}")
            }
        }
    }
    val model = imageProfile?.src ?: request
    AsyncImage(
        model = model,
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