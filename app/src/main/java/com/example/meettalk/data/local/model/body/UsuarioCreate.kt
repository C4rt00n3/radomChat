package com.example.meettalk.data.local.model.body

import com.example.meettalk.data.local.model.body.enums.Gender

class UsuarioCreate (
    val nome: String,
    val imagemUrl: String? = null,
    val idade: Int,
    val sexo: Gender = Gender.M,
    val autenticacao: LoginRequest,
    val localizacao: CreateLocalizacao? = null
)
