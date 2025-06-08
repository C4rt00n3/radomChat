package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.utils.FormatClass
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserRealm : RealmObject {
    @kotlin.jvm.Transient
    private  val format = FormatClass()

    @PrimaryKey
    var uuid: String = ""

    var name: String = ""
    var age: Int = 18
    var gender: String = "M"

    var chatParticipants: RealmList<ChatParticipantRealm> = realmListOf()

    var blocks: RealmList<BlockRealm> = realmListOf()

    var profileImages: RealmList<ImageProfileRealm> = realmListOf()


    var messageImages: RealmList<ImageMessageRealm> = realmListOf()

    var owner: Boolean = false

    fun toClass() = format.fromUserRealm(this)
}
