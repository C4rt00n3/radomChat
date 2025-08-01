package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.utils.FormatClass
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class PrivacyUserRealm : RealmObject {
    @kotlin.jvm.Transient
    private val format = FormatClass()

    @PrimaryKey
    var uuid: String = ""

    var noMarkRead: Boolean = false

    var imageBreak: Int = 0

    var talkBreak: Int = 0

    fun toRealm() = format.fromPrivacyUserRealm(this)
}