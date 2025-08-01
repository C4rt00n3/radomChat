package com.example.meettalk.presentation.components.LoginComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meettalk.R
import com.example.meettalk.ui.theme.DarkBlueLine

/**
 * Campo de entrada para a senha do usuário.
 *
 * @param password O valor atual do campo de senha.
 * @param onPasswordChange Callback invocado quando o valor da senha muda.
 * @param isError Indica se há um erro de validação na senha.
 * @param errorMessage A mensagem de erro a ser exibida, se houver.
 */
@Composable
fun PasswordInputField(
    password: String,
    onPasswordChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String
) {
    Column {
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            isError = isError,
            label = { Text(stringResource(R.string.label_senha), modifier = Modifier.padding(vertical = 8.dp)) },
            placeholder = { Text(stringResource(R.string.placeholder_senha)) },
            modifier = Modifier.padding(bottom = if (isError) 8.dp else 0.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password), // Adicionado para senhas
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = Color.LightGray, // Pode ser ajustado ou removido se não usado
                errorTextColor = Color.Red,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Gray, // Pode ser ajustado ou removido se não usado
                errorContainerColor = Color.Red.copy(alpha = 0.2f),
                cursorColor = DarkBlueLine,
                errorCursorColor = Color.Red,
                focusedIndicatorColor = DarkBlueLine,
                unfocusedIndicatorColor = DarkBlueLine,
                // disabledIndicatorColor = Color.Transparent, // Removido se não usado
                errorIndicatorColor = Color.Red
            )
        )
        if (isError) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 24.dp)
            )
        }
    }
}