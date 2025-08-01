package com.example.meettalk.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.meettalk.R

/**
 * Exibe um diálogo de informação inicial para o usuário.
 *
 * @param onDismiss Callback invocado quando o diálogo é fechado.
 */
@Composable
fun WelcomeInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(
                Icons.Default.Info,
                contentDescription = stringResource(R.string.icone_informacao),
                tint = MaterialTheme.colorScheme.onBackground
            )
        },
        title = { Text(text = stringResource(R.string.titulo_edite_perfil)) },
        text = { Text(text = stringResource(R.string.texto_edite_perfil_descricao)) },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.botao_fechar))
            }
        }
    )
}