package com.example.meettalk.data.local.model.entities

import com.example.meettalk.data.local.model.RealmClass.PreferenceRealm
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.utils.FormatClass

data class Preference(val uuid: String, val gender: Gender, val maxAge: Int) {
    fun toPreference() = FormatClass().toPreference(this)
}