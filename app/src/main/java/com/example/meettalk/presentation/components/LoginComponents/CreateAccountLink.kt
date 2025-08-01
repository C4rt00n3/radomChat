package com.example.meettalk.presentation.components.LoginComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.meettalk.R
import com.example.meettalk.ui.theme.TextColorGrayLight

/**
 * Link para criar uma nova conta.
 *
 * @param onClick Callback invocado quando o link é clicado.
 */
@Composable
fun CreateAccountLink(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.Center) { // Centralizar o texto dentro da Row
            Text(stringResource(R.string.texto_nao_tem_conta), fontSize = 14.sp, color = TextColorGrayLight)
            Text(
                stringResource(R.string.texto_criar_agora),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary // Use a cor primária do tema para o link
            )
        }
    }
}