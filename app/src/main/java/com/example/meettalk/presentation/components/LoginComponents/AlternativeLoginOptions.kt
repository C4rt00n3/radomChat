package com.example.meettalk.presentation.components.LoginComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meettalk.R
import com.example.meettalk.ui.theme.TextColorGray

/**
 * Seção para opções de login alternativas (Google, Facebook).
 *
 * @param screenWidth A largura da tela em DP, usada para dimensionar os botões.
 * @param onGoogleSignIn Callback para iniciar o processo de login com Google.
 * @param onFacebookSignIn Callback para iniciar o processo de login com Facebook.
 */
@Composable
fun AlternativeLoginOptions(
    screenWidth: Dp,
    onGoogleSignIn: () -> Unit,
    onFacebookSignIn: () -> Unit
) {
    Text(
        stringResource(R.string.texto_ou_continuar_com),
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = TextColorGray,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 30.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SocialLoginButton(
            iconResId = R.drawable.flat_color_icons_google,
            text = stringResource(R.string.botao_google),
            contentDescription = stringResource(R.string.content_description_login_google),
            modifier = Modifier.width(screenWidth * 0.37f),
            onClick = onGoogleSignIn
        )
        SocialLoginButton(
            iconResId = R.drawable.flat_color_icons_facebook,
            text = stringResource(R.string.botao_facebook),
            contentDescription = stringResource(R.string.content_description_login_facebook),
            modifier = Modifier.width(screenWidth * 0.37f),
            onClick = onFacebookSignIn
        )
    }
}