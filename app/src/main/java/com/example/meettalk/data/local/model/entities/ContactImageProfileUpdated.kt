package com.example.meettalk.data.local.model.entities

import okio.Buffer

data class ImageProfileBuffer(val uuid: String, val userUuid: String, val src: String, val createAt: String, val updateAt: String, val slot: Int) {}

data class ContactImageProfileUpdated (val action: String, val data: ImageProfileBuffer)