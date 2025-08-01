package com.example.meettalk.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meettalk.R
import com.example.meettalk.data.local.model.RealmClass.BlockRealm
import com.example.meettalk.data.local.model.RealmClass.ChatParticipantRealm
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.RealmClass.ImageMessageRealm
import com.example.meettalk.data.local.model.RealmClass.ImageProfileRealm
import com.example.meettalk.data.local.model.RealmClass.LocationRealm
import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.RealmClass.PreferenceRealm
import com.example.meettalk.data.local.model.RealmClass.PrivacyUserRealm
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.body.CreateMessage
import com.example.meettalk.data.local.model.body.UpdateMessage
import com.example.meettalk.data.local.model.body.enums.MessageType
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.ChatParticipant
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.data.remote.ChatEndPoint
import com.example.meettalk.data.remote.ChatRequests
import com.example.meettalk.data.remote.UserRequests
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import com.example.meettalk.utils.TaskManager
import com.example.meettalk.utils.TokenManager
import com.example.meettalk.utils.getSubFromJwt
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant

class MessageViewModel(
) : ViewModel() {
    private val taskManager = TaskManager()

    private val _chat = MutableStateFlow<Chat?>(null)
    val chat: StateFlow<Chat?> = _chat.asStateFlow()

    private val _myUser = MutableStateFlow<User?>(null)
    val myUser: StateFlow<User?> = _myUser.asStateFlow()

    private val _taskManager = TaskManager()

    private val _token = MutableStateFlow<String>("")
    val token: StateFlow<String> = _token.asStateFlow()

    private val format = FormatRealm()
    private val formatR = FormatClass()

    private lateinit var realm: Realm
    private lateinit var url: String
    private lateinit var retrofit: Retrofit
    private lateinit var apiServiceChat: ChatEndPoint
    private lateinit var chatRequests: ChatRequests
    private lateinit var usersRequests: UserRequests
    private lateinit var context: Context

    fun build(context: Context, realm: Realm? = null) {
        val url = context.getText(R.string.baseUrl).toString()
        this.realm = realm ?: run {
            val config = RealmConfiguration.Builder(
                schema = setOf(
                    UserRealm::class,
                    BlockRealm::class,
                    ChatRealm::class,
                    LocationRealm::class,
                    ImageProfileRealm::class,
                    ChatParticipantRealm::class,
                    ImageMessageRealm::class,
                    PreferenceRealm::class,
                    MessageRealm::class,
                    PrivacyUserRealm::class
                )
            ).schemaVersion(1).deleteRealmIfMigrationNeeded().build()
            Realm.open(config)
        }

        this.url = url
        this.context = context
        this.retrofit =
            Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create())
                .build()
        this.apiServiceChat = this.retrofit.create(ChatEndPoint::class.java)

        this.realm.let {
            this.chatRequests = ChatRequests(url, it)
            this.usersRequests = UserRequests(url, it)
        }

        viewModelScope.launch(Dispatchers.Default) {
            val token = TokenManager(context).getToken()
            token?.let { it ->
                _token.value = it

                val sub = getSubFromJwt(it.replace("Bearer ", ""))
                sub?.let { userId ->
                    _myUser.value = getUser(userId)
                }
            }
        }
    }

    private fun getUser(uuid: String): User? {
        return realm.query<UserRealm>("uuid == $0", uuid).find().firstOrNull()?.let { userRealm ->
            formatR.fromUserRealm(userRealm)
        }
    }

    private fun addMessage(message: Message) {
        val messages = _chat.value?.messages ?: emptyList()
        _chat.value = _chat.value?.copy(messages = messages + message)
    }

    /**
     * Adiciona uma nova mensagem à conversa e inicia o processo de inserção no banco de dados.
     *
     * Esta função adiciona a [message] localmente à lista de mensagens da conversa,
     * e em seguida, lança uma *coroutine* no `viewModelScope` para realizar a
     * persistência da mensagem no banco de dados remoto ou local.
     *
     * A operação exige Android 13 ([Build.VERSION_CODES.TIRAMISU]) ou superior.
     *
     * @param token Token de autenticação necessário para a inserção da mensagem.
     * @param message Objeto [Message] representando a mensagem a ser criada.
     * @param onFinished Callback opcional que será chamado após a conclusão da inserção.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createMessage(
        token: String,
        message: Message,
        currentUser: User,
        file: MultipartBody.Part?,
        onFinished: (Chat?) -> Unit
    ) {
        viewModelScope.launch {
            insertInDatabase(token, message, currentUser, file = file,onFinished)
        }
    }

    private fun convertMessage(message: Message) = CreateMessage(
        uuid = message.uuid,
        text = message.text,
        type = message.type ?: MessageType.TEXT,
        url = message.url,
        receiverId = message.receiverId,
        senderId = message.senderId,
        replyToId = message.replyToId,
        createdAt = message.createdAt
    )

    /**
     * Deleta localmente (apenas para o usuário atual) uma lista de mensagens no banco de dados Realm.
     *
     * Esta operação não afeta o servidor nem outros usuários. É utilizada, por exemplo,
     * quando o usuário deseja apagar mensagens só da própria visualização ("deletar para mim").
     *
     * @param messagesToDeleteList Lista de mensagens a serem removidas localmente.
     */
    fun deleteForMe(messagesToDeleteList: List<Message>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (messagesToDeleteList.isEmpty()) {
                return@launch
            }
            try {
                val uuidsToDelete = messagesToDeleteList.map { it.uuid }
                realm.write {
                    val messagesRealm = this.query<MessageRealm>("uuid IN $0", uuidsToDelete).find()
                    delete(messagesRealm)
                }
            } catch (e: Exception) {
                println("Erro ao deletar mensagens do Realm: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Remove todas as mensagens de um chat específico do banco de dados local (Realm).
     *
     * Esta operação é executada em uma coroutine usando `Dispatchers.IO` para evitar
     * bloqueio da thread principal. Caso ocorra algum erro durante a exclusão,
     * ele será capturado e impresso no log.
     *
     * @param chatUud O UUID do chat cujas mensagens devem ser removidas.
     */
    suspend fun clearChat(chatUud: String) {
        try {
            realm.write {
                val messagesRealm = this.query<MessageRealm>("chatId = $0", chatUud).find()
                delete(messagesRealm)
            }
        } catch (e: Exception) {
            println("Erro ao deletar mensagens do Realm: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Adiciona uma mensagem ao banco de dados Realm de forma síncrona,
     * operando dentro de uma transação Realm existente.
     *
     * Essa função converte a [message] para o modelo Realm e a adiciona à lista de mensagens
     * do chat atual. Caso o chat ou a mensagem estejam nulos, a operação é ignorada.
     *
     * @param transaction A transação Realm mutável na qual a operação será executada.
     * @param message A mensagem a ser adicionada ao banco de dados local.
     */
    private fun addMessageInDb(transaction: MutableRealm, message: Message) {

        val chatUuid = message.chatId ?: return
        val chatRealm = transaction.query<ChatRealm>("uuid == $0", chatUuid).first().find()
        val messageRealm = format.toMessageRealm(message)

        if (chatRealm != null && messageRealm != null) {
            chatRealm.messages.add(messageRealm)
            transaction.copyToRealm(chatRealm, updatePolicy = UpdatePolicy.ALL)
        }
    }

    /**
     * Insere uma mensagem no banco de dados remoto e, em caso de falha,
     * persiste localmente usando o Realm.
     *
     * Esta função tenta enviar a mensagem para o backend usando o token de autenticação.
     * Se ocorrer erro, a mensagem será salva localmente via [addMessageInDb],
     * e uma notificação de erro será exibida ao usuário.
     *
     * @param token Token de autenticação do usuário.
     * @param message A mensagem a ser enviada e inserida no banco de dados.
     * @param onFinished Callback a ser chamado ao final da operação (sucesso ou falha).
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun insertInDatabase(
        token: String,
        message: Message,
        currentUser: User?,
        file: MultipartBody.Part? = null,
        onFinished: (Chat?) -> Unit
    ) {
        chatRequests.create(token, convertMessage(message), file = file) { mess ->
            try {
                realm.writeBlocking {
                    if (message.chatId == null) {
                        val chat = Chat(
                            uuid = mess.uuid,
                            createdAt = Instant.now().toString(),
                            lastMessageDate = mess.createdAt,
                            messages = listOf(message),
                            participants = listOf(
                                ChatParticipant(
                                    chatId = mess.chatId!!,
                                    userId = mess.senderId,
                                    user = _myUser.value
                                ),
                                ChatParticipant(
                                    chatId = mess.chatId,
                                    userId = mess.receiverId,
                                    user = currentUser
                                )
                            ),
                            fav = false
                        )
                        format.toChat(chat)?.let { chatRealm ->
                            val formattedParticipants = chatRealm.participants.map {
                                it.id = "${it.userId}${it.chatId}"
                                it
                            }

                            chatRealm.participants =
                                realmListOf(*formattedParticipants.toTypedArray())

                            copyToRealm(chatRealm, updatePolicy = UpdatePolicy.ALL)
                        }

                        onFinished(chat)
                    } else {
                        addMessageInDb(
                            transaction = this,
                            message = mess
                        )
                        onFinished(null)
                    }
                }
            } catch (e: Exception) {
                showToast("Error ao enviar mensagem!")
                e.printStackTrace()
            }
        }
    }

    fun getImageProfileRealm(uuid: String, onSuccess: (ImageProfile) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            realm.write {
                val get =
                    this.query<ImageProfileRealm>("uuid == $0", uuid).first().find()
                val formated = get?.toClass()

                formated?.let {
                    onSuccess(it)
                }
            }
        }
    }

    /**
     * Atualiza o conteúdo de uma mensagem que pertence ao usuário atual.
     *
     * Essa função envia uma requisição de atualização da mensagem para o servidor.
     * Caso a atualização ocorra com sucesso, os dados da mensagem também são atualizados
     * no banco de dados local (Realm).
     *
     * @param message A mensagem original que será atualizada.
     * @param update Os dados novos que substituirão os valores da mensagem atual.
     */
    suspend fun updateMessage(message: Message, update: UpdateMessage, onFinally: () -> Unit) {
        val token = _token.value
        val updatedChatResponse = chatRequests.update(
            token, message.uuid, update, onFinally
        )

        updatedChatResponse?.let {
            realm.writeBlocking {
                val messageToUpdate =
                    this.query<MessageRealm>("uuid == $0", message.uuid).first().find()

                if (update.text?.isNotBlank() == true && messageToUpdate != null) {
                    messageToUpdate.text = it.text
                    messageToUpdate.countUpdate = it.countUpdate
                    messageToUpdate.isUpdate = it.isUpdate
                    messageToUpdate.updateAt = it.updateAt

                }
            }
        } ?: run {
            println("Error: Failed to update message via network request.")
        }
    }

    private fun deleteMessagesInBank(messages: List<String>) {
        realm.writeBlocking {
            val messagesRealm = this.query<MessageRealm>("uuid IN $0", messages).find()
            delete(messagesRealm)
        }
    }

    /**
     * Deleta todas as mensagens selecionadas que pertencem ao usuário atual.
     *
     * Esta função executa a exclusão de múltiplas mensagens no servidor via `chatRequests.deleteMany`
     * e, em seguida, remove essas mensagens do banco de dados local (Realm).
     *
     * @param token Token de autenticação do usuário.
     * @param messages Lista de mensagens a serem deletadas (devem ser mensagens do próprio usuário).
     */
    suspend fun deleteMessages(messages: List<Message>, inServer: Boolean = true) {
        val token = _token.value
        val uuids = messages.map { it.uuid }
        if (inServer) chatRequests.deleteMany(token, uuids, false, {
            showToast("Error ao apagar mensagens")
        }) {
            deleteMessagesInBank(it)
        }
        else deleteMessagesInBank(uuids)
    }

    fun markChatAsRead(chatUuid: String, senderId: String) {
        val token = _token.value

        viewModelScope.launch {
            chatRequests.markRead(token = token, chatUuid) {
                realm.writeBlocking {
                    val messagesToUpdate = query<MessageRealm>(
                        "chatId == $0 AND senderId == $1", chatUuid, senderId
                    ).find()
                    messagesToUpdate.forEach { message ->
                        message.isRead = true
                    }
                }
            }
        }
    }

    /**
     * Atualiza o estado interno do chat atual.
     *
     * Esta função define o valor do estado `_chat` com o objeto [Chat] fornecido.
     * É geralmente utilizada para refletir a mudança de contexto do chat atual
     * dentro da aplicação (por exemplo, ao abrir uma conversa específica).
     *
     * @param chat Objeto [Chat] a ser definido como o chat atual.
     */
    fun setChat(chat: Chat) {
        _chat.value = chat
    }

    private fun findChat(uuid: String): Chat? {
       return  realm.query<ChatRealm>("uuid == $0", uuid).first().find()?.let {
           formatR.fromChatRealm(it)
       }
    }

    /**
     * Observa as alterações no banco de dados Realm relacionadas aos objetos [ChatRealm]
     * e atualiza o chat atual com base no UUID fornecido.
     *
     * Essa função inicia uma *coroutine* no escopo da `viewModelScope` e coleta os fluxos
     * de dados do Realm usando `asFlow()`. Sempre que houver alteração nos dados,
     * ela tenta encontrar e atualizar o chat correspondente ao [uuid] fornecido.
     *
     * Requer API nível [Build.VERSION_CODES.TIRAMISU] (Android 13) ou superior.
     *
     * @param uuid UUID do chat a ser observado e atualizado em tempo real.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun observeChat(uuid: String, onChange: (Chat) -> Unit) {
        realm.query<ChatRealm>().asFlow().collect { result ->
            val chats = result.list
            if (chats.isNotEmpty()) {
                findChat(uuid)?.let { chat ->
                    onChange(chat)

                    val messages = chat.messages.filter { !it.isSend }

                    messages.forEach {
                        _taskManager.addTask { sendMessageNotSend(it) }
                    }
                }
            }
        }
    }

    /**
     * Recupera um objeto [Chat] correspondente ao UUID fornecido.
     *
     * Esta função realiza uma consulta no banco de dados Realm para buscar
     * um objeto [ChatRealm] com o UUID especificado. Caso encontrado,
     * o objeto será convertido para o modelo de domínio [Chat] usando
     * o conversor `formatR.fromChatRealm`.
     *
     * @param chatUuid UUID do chat a ser recuperado.
     * @return Um objeto [Chat] se encontrado; caso contrário, retorna `null`.
     */
    fun getChat(chatUuid: String): Chat? {
        return realm.query<ChatRealm>("uuid == $0", chatUuid).find().firstOrNull()
            ?.let { chatRealm ->
                formatR.fromChatRealm(chatRealm)
            }
    }

    /**
     * Recupera o primeiro objeto [ChatRealm] que inclui um usuário específico como participante,
     * e o converte para o tipo [Chat] usando o formatador.
     *
     * Esta função consulta o banco de dados Realm para encontrar o primeiro chat onde
     * ao menos um participante possui o [userId] fornecido. Ela utiliza os recursos
     * de consulta de "linking objects" do Realm para buscar eficientemente dentro da
     * [RealmList] de participantes embutida.
     *
     * **Importante:** Como esta função usa `.first()`, ela irá:
     * - Retornar o primeiro chat encontrado que corresponde ao critério.
     * - Lançar uma exceção se nenhum chat for encontrado para o `userId` especificado.
     * Considere adicionar tratamento de erro (ex: `firstOrNull()`) se a ausência de chats for uma possibilidade esperada.
     *
     * @param realm A instância ativa do [Realm] para realizar a consulta.
     * @param userId O identificador único do usuário a ser buscado entre os participantes do chat.
     * @return Um objeto [Chat] que representa o primeiro chat encontrado com o usuário como participante.
     * @throws NoSuchElementException Se nenhum [ChatRealm] for encontrado para o `userId` fornecido.
     */
    suspend fun getChatsByParticipantUserId(userId: String): Chat? {
        val chat =
            realm.query<ChatRealm>("participants.userId == $0", userId).find().firstOrNull()
        return chat?.let {
            formatR.fromChatRealm(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun sendMessageNotSend(message: Message) {
        try {
            val messageReq = chatRequests.create(token.value, convertMessage(message)) {}

            if (messageReq == null) {
                showToast("Erro ao enviar mensagem!")
                return
            }

            realm.writeBlocking {
                val writeTransaction =
                    query<MessageRealm>("uuid == $0", message.uuid).find().first()
                writeTransaction.isSend = true
            }
        } catch (e: Exception) {
            showToast("Erro ao enviar mensagem!")
            e.printStackTrace()
        }
    }

    private fun showToast(message: String) {
        context.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }

    fun markFav(uuid: String) {
        val token = _token.value

        viewModelScope.launch {
            chatRequests.markFav(uuid, token)?.let { chatResponse ->
                realm.write {
                    val chatRealm = query<ChatRealm>("uuid == $0", uuid).find().firstOrNull()

                    if (chatRealm != null) {
                        findLatest(chatRealm)?.fav = chatResponse.fav
                    } else {
                        Log.w("MarkFavDebug", "ChatRealm not found for UUID: $uuid")
                    }
                }
            }
        }
    }

    fun findUser(user: User): User? {
        return  try {
            val token = _token.value
            null
        } catch (e: Exception) {
            println(e.message)
            null
        }
    }
}