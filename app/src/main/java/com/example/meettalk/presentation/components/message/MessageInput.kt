import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meettalk.R // Make sure this R is correctly imported for your project
import com.example.meettalk.presentation.components.message.ButtonSendMessage

/**
 * Composable para exibir um campo de entrada de mensagem, semelhante ao encontrado
 * em aplicativos de chat como o WhatsApp. Ele permite ao usuário digitar texto,
 * e alterna entre os ícones de microfone e enviar/editar.
 *
 * @param onTextChange Callback invocado quando o texto do campo de entrada muda.
 * @param value O texto atual exibido no campo de entrada.
 * @param isEditing Booleano que indica se a mensagem está em modo de edição.
 * @param onSend Callback invocado quando o botão de enviar/editar/microfone é clicado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(
    onTextChange: (String) -> Unit,
    value: String,
    isEditing: Boolean,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { onTextChange(it) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 100.dp),
                placeholder = { Text(
                    stringResource(R.string.mensagem),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ) },
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = false,
                maxLines = 5
            )

            ButtonSendMessage(value, isEditing, onSend)
        }
    }
}

/**
 * Preview para o componente MessageInput.
 * Usa MaterialTheme para garantir que as cores do tema sejam aplicadas corretamente.
 */
@Preview(showBackground = true)
@Composable
fun PreviewMessageInput() {
    MaterialTheme {
        MessageInput(
            onTextChange = {},
            value = "Olá, mundo!",
            isEditing = false,
            onSend = {}
        )
    }
}