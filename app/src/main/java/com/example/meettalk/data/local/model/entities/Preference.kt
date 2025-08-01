package com.example.meettalk.data.local.model.entities

import com.example.meettalk.data.local.model.RealmClass.PreferenceRealm
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm

data class Preference(val uuid: String, val gender: Gender, val maxAge: Int) {
    fun toRealm() = FormatRealm().toPreference(this)
}