package com.example.meettalk.presentation.components.chat

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.meettalk.presentation.components.SingleChoiceSegmentedButton


/**
 * Botões segmentados para filtrar a lista de chats.
 *
 * @param onCategorySelected Callback que é invocado quando uma categoria é selecionada.
 */
@Composable
fun ChatFilterSegmentedButtons(onCategorySelected: (Int, String) -> Unit) {
    SingleChoiceSegmentedButton(modifier = Modifier.padding(vertical = 16.dp)) { index, label ->
        onCategorySelected(index, label)
    }
}