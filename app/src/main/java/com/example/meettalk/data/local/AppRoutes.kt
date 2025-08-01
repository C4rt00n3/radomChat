package com.example.meettalk.data.local

/**
 * Define as rotas de navegação da aplicação para melhor organização e segurança de tipo.
 */
object AppRoutes {
    const val LOGIN = "login"
    const val CHAT_LIST = "chat"
    const val CHAT_MESSAGES_PATTERN = "chat/{uuid}"
    const val EDIT_MY_PERFIL = "edit/user/{show}"
    const val USER_PERFIL = "user/{uuid}"
    const val EXPLORER_USERS = "explorer"
    const val OPTIONS_APP = "options/{show}"
    const val LOADING = "loading"
    const val IMAGE_VIEW = "image/view/{uuid}/{chat_uuid}"
}