package com.example.meettalk.presentation.components.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.meettalk.R

@Composable
fun ButtonSendMessage(
    value: String,
    isEditing: Boolean,
    onSend: () -> Unit
) {
    IconButton(
        onClick = { onSend() },
        modifier = Modifier
            .padding(start = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50)
            )
            .size(48.dp)
    ) {
        if (!isEditing) {
            Icon(
                imageVector = if (value.isNotBlank()) Icons.Default.Send else Icons.Rounded.Mic,
                contentDescription = if (value.isNotBlank()) stringResource(R.string.enviar_mensagem) else stringResource(
                    R.string.gravar_udio
                ),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = stringResource(R.string.editar_mensagem),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}