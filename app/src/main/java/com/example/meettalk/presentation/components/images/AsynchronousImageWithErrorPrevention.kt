package com.example.meettalk.presentation.components.images

import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter

@Composable
fun AsynchronousImageWithErrorPrevention(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = error,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    imageInCaseOfError: ByteArray? = null
) {
    var isError by remember { mutableStateOf(false) }

    if (!isError)
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            onError = {
                if (onError != null) {
                    onError(it)
                }
                isError = true
            },
            placeholder = placeholder,
            fallback = fallback,
            onLoading = onLoading,
            onSuccess = onSuccess,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality
        )
    else
        AsyncImage(
            model = imageInCaseOfError,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            onError = {
                if (onError != null) {
                    onError(it)
                }
                isError = true
            },
            placeholder = placeholder,
            fallback = fallback,
            onLoading = onLoading,
            onSuccess = onSuccess,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality
        )
}