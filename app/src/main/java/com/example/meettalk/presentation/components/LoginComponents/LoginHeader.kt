package com.example.meettalk.presentation.components.LoginComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.meettalk.R

/**
 * Componente de cabeçalho da tela de login, contendo a imagem de fundo e o título "Login".
 *
 * @param screenHeight A altura da tela em DP, usada para dimensionar a imagem de fundo.
 */
@Composable
fun LoginHeader(screenHeight: Dp) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(R.drawable.subtract),
            contentDescription = stringResource(R.string.imagem_decorativa_login),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.35f)
        )
        Text(
            text = stringResource(R.string.titulo_login),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}