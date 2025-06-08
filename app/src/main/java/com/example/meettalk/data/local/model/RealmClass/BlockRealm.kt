package com.example.meettalk.data.local.model.RealmClass

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class BlockRealm : RealmObject {
    @PrimaryKey
    var uuid: String = ""

    var userId: String? = ""
    var blockedUserId: String = ""

    var blockedUser: UserRealm? = null
}