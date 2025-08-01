package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.data.local.model.entities.ImageMessage
import com.example.meettalk.utils.FormatClass
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class ImageMessageRealm : RealmObject {
    @Transient
    val format = FormatClass()

    @PrimaryKey
    var uuid: String = ""

    var src: ByteArray? = null

    var user: UserRealm? = null

    var message: MessageRealm? = null

    fun toClass() = format.fromImageMessage(this)
}