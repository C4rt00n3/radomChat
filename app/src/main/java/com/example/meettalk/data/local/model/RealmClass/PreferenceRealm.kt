package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class PreferenceRealm: RealmObject {
    @PrimaryKey
    var uuid: String = ""

    var gender: String = ""
    var maxAge: Int = 60

    fun toPreference() = FormatClass().fromPreference(this)
}