package com.example.meettalk.presentation.components.LoginComponents

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.meettalk.ui.theme.ButtonColorGray

/**
 * Botão genérico para login social (Google, Facebook).
 *
 * @param iconResId O ID do recurso do ícone a ser exibido.
 * @param text O texto do botão.
 * @param contentDescription A descrição de conteúdo para acessibilidade do ícone.
 * @param modifier O [Modifier] a ser aplicado ao botão.
 * @param onClick Callback invocado quando o botão é clicado.
 */
@Composable
fun SocialLoginButton(
    @DrawableRes iconResId: Int,
    text: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ButtonColorGray)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = contentDescription,
                    modifier = Modifier.padding(end = 8.dp),
                    tint = Color.Unspecified // Ícones de cor fixa
                )
                Text(text, color = Color(0xFF475569)) // Cor específica para o texto do botão social
            }
        }
    }
}