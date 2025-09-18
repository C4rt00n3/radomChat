package com.example.meettalk.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.entities.Chat
import io.realm.kotlin.Realm

@Composable
fun rememberDynamicOptionsListChats(select: List<Chat>, realm: Realm): List<OptionsMenu> {
    val deleteString = stringResource(R.string.excluir)
    val clearMessages = stringResource(R.string.limpar_conversa)
    return remember(select) {
        listOf(
            OptionsMenu(
                text = deleteString,
                onClick = {
                    realm.writeBlocking {
                        select.forEach { chat ->
                            query(ChatRealm::class, "uuidd == $0", chat.uuid)
                                .find()
                                .firstOrNull()
                                ?.let {
                                    delete(it)
                                }
                        }
                    }
                }
            ),
            OptionsMenu(
                text = clearMessages,
                onClick = {
                    realm.writeBlocking {
                        select.forEach { chat ->
                            query(MessageRealm::class, "chatId == $0", chat.uuid)
                                .find()
                                .forEach { message ->
                                    delete(message)
                                }
                        }
                    }
                }
            )
        )
    }
}