package com.example.meettalk.data.local.model

import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User

/**
 * Representa o estado completo da interface do usuário (UI) da tela de chat.
 * Este objeto encapsula todas as variáveis de estado que influenciam como a UI
 * da tela de chat é exibida e interage.
 *
 * @property messageText O texto atual digitado no campo de entrada de mensagem.
 * @property editingMessage Um booleano que indica se o usuário está no modo de edição de mensagem.
 * @property respondingToMessage A mensagem à qual o usuário está respondendo. Nulo se não houver resposta ativa.
 * @property selectedMessages Uma lista de mensagens que foram selecionadas pelo usuário.
 * @property currentUser O objeto do usuário autenticado atualmente. Nulo se o usuário não estiver logado.
 * @property token O token de autenticação do usuário, usado para requisições de API.
 */
data class ChatUiState(
    val messageText: String = "",
    val editingMessage: Message? = null,
    val respondingToMessage: Message? = null,
    val selectedMessages: List<Message> = emptyList(),
    val currentUser: User? = null,
    val token: String = ""
)
