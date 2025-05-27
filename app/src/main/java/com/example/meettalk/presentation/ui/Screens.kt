package com.example.meettalk.presentation.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Chat : Screen("chat")
    object Message : Screen("chat/{uuid}") {
        fun withUuid(uuid: String) = "chat/$uuid"
    }
}