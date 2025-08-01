package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.utils.FormatClass
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

open class ChatRealm : RealmObject {
    @PrimaryKey
    var uuid: String = ""

    var createdAt: String = ""
    var lastMessageDate: String? = ""

    var messages: RealmList<MessageRealm> = realmListOf()
    var fav: Boolean = false
    var participants: RealmList<ChatParticipantRealm> = realmListOf()

    @Transient
    val format = FormatClass()

    fun toRealm() = format.fromChatRealm(this)
}
