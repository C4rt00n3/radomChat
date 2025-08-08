package com.example.meettalk.presentation.components.user

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.User

/**
 * Exibe uma camada de informações do usuário sobreposta à imagem de perfil.
 * Inclui nome, gênero e idade.
 *
 * @param user O objeto [User] cujos detalhes serão exibidos.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserProfileOverlay(user: User?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            )
            .clickable(enabled = false) {  },
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = user?.name ?: stringResource(R.string.indisponivel),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.genero_format,
                user?.gender ?: stringResource(R.string.nao_informado)
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = stringResource(
                R.string.idade_format,
                user?.age?.toString() ?: stringResource(R.string.indisponivel)
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}