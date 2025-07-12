package com.example.meettalk.data.local

/**
 * Define as rotas de navegação da aplicação para melhor organização e segurança de tipo.
 */
object AppRoutes {
    const val LOGIN = "login"
    const val CHAT_LIST = "chat"
    const val CHAT_MESSAGES_PATTERN = "chat/{uuid}"
    const val EDIT_MY_PERFIL = "edit/user"
    const val USER_PERFIL = "user/{uuid}"
    const val EXPLORER_USERS = "explorer"
    const val OPTIONS_APP = "options"
    const val LOADING = "loading"
}