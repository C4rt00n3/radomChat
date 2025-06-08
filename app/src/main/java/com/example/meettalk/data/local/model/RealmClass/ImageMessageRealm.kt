package com.example.meettalk.data.local.model.RealmClass

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class ImageMessageRealm : RealmObject {
    @PrimaryKey
    var uuid: String = java.util.UUID.randomUUID().toString()

    var src: ByteArray? = null

    var userUuid: String? = null

    var user: UserRealm? = null
}
