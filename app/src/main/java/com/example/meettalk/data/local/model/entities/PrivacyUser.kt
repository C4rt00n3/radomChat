package com.example.meettalk.data.local.model.entities

import com.example.meettalk.utils.FormatRealm

data class PrivacyUser(
    val uuid: String,
    val noMarkRead: Boolean = false,
    val imageBreak: Int = 0,
    val talkBreak: Int = 0
) {
    fun toRealm() = FormatRealm().toPrivacyUser(this)
}