package com.example.meettalk.presentation.components.message

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.meettalk.R

/**
 * Barra de busca de mensagens.
 *
 * @param modifier Modificador para este componente.
 * @param searchQuery O texto atual da busca.
 * @param onSearchQueryChanged Callback para alteração do texto.
 * @param onClearSearch Callback para limpar a busca.
 */
@Composable
fun SearchMessagesBar(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = { Text(stringResource(R.string.pesquisar)) },
        maxLines = 1,
        trailingIcon = {
            IconButton(onClick = {
                if (searchQuery.isNotBlank()) {
                    onClearSearch()
                }
            }) {
                Icon(
                    painter = if (searchQuery.isBlank()) painterResource(R.drawable.search) // Ícone de busca
                    else painterResource(R.drawable.baseline_close_24), // Ícone de fechar
                    contentDescription =
                    stringResource(
                        if (searchQuery.isBlank()) R.string.pesquisar
                        else R.string.cancelar_pesquisa
                    )
                )
            }
        },
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        modifier = modifier
    )
}