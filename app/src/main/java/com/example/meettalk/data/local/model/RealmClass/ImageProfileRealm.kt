package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.utils.FormatClass
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class ImageProfileRealm : RealmObject {
    @kotlin.jvm.Transient
    private  val format = FormatClass()

    @PrimaryKey
    var uuid: String = ""

    var createAt:String? = null
    var updateAt:  String? = null

    var src: ByteArray? = null

    var userUuid: String? = null

    var user: UserRealm? = null

    var slot: Int = 1

    fun toClass() = ImageProfile(
        uuid = this.uuid,
        src = this.src,
        slot = this.slot,
        userUuid = this.userUuid,
        createAt = this.createAt,
        updateAt = this.updateAt
    )
}
