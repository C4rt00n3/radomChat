package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.utils.FormatClass
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

// CORREÇÃO AQUI: Adicionar 'open'
open class MessageRealm: RealmObject {
    @kotlin.jvm.Transient
    private val format = FormatClass()
    @PrimaryKey
    var uuid: String = ""

    var text: String = ""
    var type: String= "TEXT"
    var url: String? = null

    var chatId: String? = ""
    var createdAt: String = ""

    var senderId: String = ""
    var receiverId: String = ""

    var chat: ChatRealm? = null

    var isSend: Boolean = true
    var isRead: Boolean = false

    var replyToId: String? = null
    var replyTo: MessageRealm? = null

    var ImageMessage: ImageMessageRealm? = null

    var replies: RealmList<MessageRealm> = realmListOf()

    var isUpdate: Boolean = false
    var updateAt: String? = null
    var countUpdate: Int = 0
}