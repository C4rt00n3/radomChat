package com.example.meettalk.presentation.components.profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.ui.theme.Purple40
import kotlinx.coroutines.launch

/**
 * Diálogo de tela cheia para editar os detalhes do perfil do usuário.
 * Permite a edição do nome, idade e gênero do usuário.
 *
 * @param user O objeto [User] a ser editado.
 * @param onDismiss Callback invocado quando o diálogo deve ser fechado (ex: botão "Fechar").
 * @param onUserSaved Callback invocado quando as edições são salvas com sucesso, passando o [User] atualizado.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileEditDialog(
    user: User,
    onDismiss: () -> Unit,
    onUserSaved: (User) -> Unit
) {
    var nameInput by remember { mutableStateOf(user.name) }
    var ageInput by remember { mutableIntStateOf(user.age) }
    val genderOptions = Gender.entries.toTypedArray()
    val (selectedGenderOption, onGenderOptionSelected) = remember { mutableStateOf(user.gender) }
    val snackbarState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.titulo_editar_perfil),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.fechar_edicao),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(stringResource(R.string.label_nome)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = ageInput.toString(),
                    onValueChange = { newAgeString ->
                        if (newAgeString.all { it.isDigit() } && newAgeString.length <= 3) {
                            ageInput = newAgeString.toIntOrNull() ?: 0
                        } else if (newAgeString.isEmpty()) {
                            ageInput = 0
                        }
                    },
                    label = { Text(stringResource(R.string.label_idade)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(R.string.label_genero),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column(Modifier.selectableGroup()) {
                    genderOptions.forEach { genderOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (genderOption == selectedGenderOption),
                                    onClick = { onGenderOptionSelected(genderOption) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (genderOption == selectedGenderOption), onClick = null
                            )
                            Text(
                                text = genderOption.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val message_penchant_campos = stringResource(R.string.mensagem_preencha_campos)

                    Button(
                        onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(stringResource(R.string.botao_cancelar))
                    }
                    Spacer(Modifier.size(8.dp))
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank() && ageInput > 0) {
                                val newuser = user.copy(
                                    name = nameInput,
                                    birthDate = user.calcularDataNascimentoFormatada(ageInput) ?: "",
                                    gender = selectedGenderOption
                                )
                                onUserSaved(newuser)
                            } else {
                                coroutineScope.launch {
                                    snackbarState.showSnackbar(
                                        message_penchant_campos
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple40, contentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.botao_salvar))
                    }
                }
            }
        }
    }
}