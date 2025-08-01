package com.example.meettalk.data.local.model.body

import com.example.meettalk.data.local.model.body.enums.Gender

data class UpdateUser(val name: String? = null, val birthDate: String? = null, val gender: Gender? = null, val location: CreateLocation? = null)