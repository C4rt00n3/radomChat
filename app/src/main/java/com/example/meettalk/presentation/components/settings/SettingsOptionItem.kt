package com.example.meettalk.presentation.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.meettalk.R
import com.example.meettalk.data.local.model.SettingsOption
import com.example.meettalk.data.local.model.SettingsOptionType

/**
 * Item de opção configurável para a lista de configurações.
 * Lida com diferentes tipos de opções (Checkbox, Radio Group, Custom Content, etc.).
 *
 * @param option A [SettingsOption] a ser exibida.
 */
@Composable
fun SettingsOptionItem(option: SettingsOption) {
    var isExpanded by remember { mutableStateOf(false) }

    if (option.type == SettingsOptionType.CUSTOM_CARD && option.customContent != null) {
        option.customContent.let { it() }
    } else {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = option.leadingContent,
            headlineContent = {
                Text(option.headlineText)
            },
            trailingContent = {
                when (option.type) {
                    SettingsOptionType.CHECKBOX -> {
                        if (option.showCheckbox) {
                            Checkbox(
                                checked = option.isChecked,
                                onCheckedChange = option.onCheckedChange,
                            )
                        }
                    }
                    SettingsOptionType.RADIO_GROUP -> {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) stringResource(R.string.content_description_collapse_options) else stringResource(
                                R.string.content_description_expand_options)
                        )
                    }
                    SettingsOptionType.NONE -> { // Assumindo que NONE significa um RadioButton simples
                        RadioButton(
                            selected = option.isChecked, onClick = option.onClick
                        )
                    }
                    else -> Unit // Para outros tipos não especificados ou sem trailing content
                }
            },
            modifier = Modifier.clickable {
                if (option.type == SettingsOptionType.RADIO_GROUP) {
                    isExpanded = !isExpanded
                } else {
                    option.onClick()
                }
            }
        )
    }

    if (isExpanded && option.supportingContent.isNotEmpty()) {
        Column(modifier = Modifier.padding(start = 24.dp)) {
            option.supportingContent.forEach { supportingContentOption ->
                SettingsOptionItem(supportingContentOption) // Recursão para exibir sub-opções
            }
        }
    }
}