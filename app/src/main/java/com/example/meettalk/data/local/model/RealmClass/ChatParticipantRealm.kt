package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.utils.FormatClass
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

open class ChatParticipantRealm : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    @kotlin.jvm.Transient
    private  val format = FormatClass()
    var chatId: String = ""
    var userId: String = ""

    var chat: ChatRealm? = null
    var user: UserRealm? = null

    fun toClass() = format.fromChatParticipantRealm(this)
}