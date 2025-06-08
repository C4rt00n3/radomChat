package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.utils.FormatClass
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class ChatParticipantRealm : RealmObject {
    @PrimaryKey
    var id: String = ""
    @kotlin.jvm.Transient
    private  val format = FormatClass()
    var chatId: String = ""
    var userId: String = ""

    var chat: ChatRealm? = null
    var user: UserRealm? = null

    fun toClass() = format.fromChatParticipantRealm(this)
}