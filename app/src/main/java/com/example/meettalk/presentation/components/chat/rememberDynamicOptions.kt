package com.example.meettalk.presentation.components.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User

/**
 * Define as opções dinâmicas do menu de contexto para as mensagens.
 *
 * @param selectedMessages Lista de mensagens atualmente selecionadas.
 * @param myUser O usuário atual (para verificar se o remetente é o usuário atual).
 * @param onDeleteMessages Callback para deletar mensagens.
 * @param onEditMessage Callback para editar uma única mensagem.
 * @param onRespondToMessage Callback para responder a uma mensagem.
 * @return Uma lista de [OptionsMenu] com base nas mensagens selecionadas.
 */
@Composable
fun rememberDynamicOptions(
    selectedMessages: List<Message>,
    myUser: User?,
    isFav: Boolean = false,
    isBlock: Boolean = false,
    onDeleteMessages: (List<Message>) -> Unit,
    onEditMessage: (Message) -> Unit,
    onRespondToMessage: (Message) -> Unit,
    onDeleteFoMe: (List<Message>) -> Unit,
    onFav: () -> Unit,
    onBlock: () -> Unit,
    onClear: () -> Unit
): List<OptionsMenu> {
    val editLabel = stringResource(R.string.editar)
    val deleteLabel = stringResource(R.string.excluir)
    val respondLabel = stringResource(R.string.responder_mensagem)
    val deleteForMe = stringResource(R.string.deletar_para_mim)
    val clear = stringResource(R.string.limpar_chat)

    return remember(
        selectedMessages.toList(),
        myUser,
        isFav,
        isBlock
    ) { // Adicionado isFav e isBlock para reatividade
        val options = mutableListOf(
            OptionsMenu(if (isFav) "DesFavoritar" else "Favoritar", onFav),
            OptionsMenu(if (isBlock) "Desbloquear" else "Bloquear", onBlock)
        )

        if (selectedMessages.size == 1) {
            if (selectedMessages[0].senderId == myUser?.uuid) {
                options.add(OptionsMenu(editLabel) {
                    onEditMessage(selectedMessages.first())
                })
            }
            options.add(OptionsMenu(respondLabel) {
                onRespondToMessage(selectedMessages.first())
            })
        }

        if (selectedMessages.isNotEmpty()) {
            options.add(OptionsMenu(deleteForMe) {
                onDeleteFoMe(selectedMessages)
            })
            options.add(OptionsMenu(clear) { // Esta opção geralmente limpa o chat inteiro, não apenas as selecionadas
                onClear()
            })
        }

        // Esta lógica de deletar para todos deve ser mais restrita
        // Apenas o remetente pode deletar para todos e se a mensagem não for muito antiga
        if (myUser != null && selectedMessages.all { it.senderId == myUser.uuid }) {
            options.add(OptionsMenu(deleteLabel) {
                onDeleteMessages(selectedMessages)
            })
        }

        options
    }
}