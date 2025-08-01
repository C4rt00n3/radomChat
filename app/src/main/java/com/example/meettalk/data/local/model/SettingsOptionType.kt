package com.example.meettalk.data.local.model

enum class SettingsOptionType {
    CHECKBOX,
    RADIO_GROUP, // Este tipo agora indica que o item principal pode expandir/colapsar
    NAVIGATE,
    CUSTOM_CARD, // Novo tipo para o card customizado
    NONE // Para sub-itens que não têm comportamento próprio de checkbox/rádio
}