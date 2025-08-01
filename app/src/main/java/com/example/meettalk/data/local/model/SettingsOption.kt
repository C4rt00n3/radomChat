package com.example.meettalk.data.local.model

import androidx.compose.runtime.Composable

/**
 * Representa uma opção de configuração com conteúdo principal, texto e um checkbox opcional.
 *
 * @param leadingContent Composable para o ícone ou imagem à esquerda da opção.
 * @param headlineText O texto principal da opção.
 * @param isChecked Estado atual do checkbox.
 * @param showCheckbox Define se o checkbox deve ser exibido.
 * @param onCheckedChange Callback para quando o estado do checkbox muda.
 * @param supportingContent Composable para conteúdo adicional abaixo do texto principal (ex: lista de usuários bloqueados).
 * @param type O tipo da opção para tratamento específico (ex: RADIO_GROUP para grupos de rádio).
 * @param onClick Callback para quando a opção é clicada (útil para opções sem checkbox).
 */
data class SettingsOption(
    val leadingContent: @Composable () -> Unit,
    val headlineText: String,
    val isChecked: Boolean = false,
    val showCheckbox: Boolean = true,
    val onCheckedChange: (Boolean) -> Unit = {},
    val supportingContent: List<SettingsOption> = emptyList(),
    val type: SettingsOptionType? = SettingsOptionType.CHECKBOX, // Novo parâmetro para o tipo
    val onClick: () -> Unit = {}, // Novo parâmetro para clique
    val customContent: @Composable (() -> Unit)? = null // Novo parâmetro para conteúdo customizado
)