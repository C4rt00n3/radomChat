package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.utils.FormatClass
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class ImageProfileRealm : RealmObject {
    @kotlin.jvm.Transient
    private  val format = FormatClass()

    @PrimaryKey
    var uuid: String = ""

    var src: ByteArray? = null

    var userUuid: String? = null

    var user: UserRealm? = null

    var isPrimary: Boolean = false
}
