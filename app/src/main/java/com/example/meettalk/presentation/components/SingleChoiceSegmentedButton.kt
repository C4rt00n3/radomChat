package com.example.meettalk.presentation.components

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SingleChoiceSegmentedButton(
    modifier: Modifier = Modifier,
    options: List<String> = listOf("Todas", "Não lidas", "Favoritas"),
    initialSelectedIndex: Int = 0,
    onSelectionChange: (selectedIndex: Int, selectedOption: String) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(initialSelectedIndex) }

    LaunchedEffect(options, initialSelectedIndex) {
        if (initialSelectedIndex in options.indices) {
            onSelectionChange(initialSelectedIndex, options[initialSelectedIndex])
        }
    }

    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = {
                    selectedIndex = index
                    onSelectionChange(index, options[index])
                },
                selected = index == selectedIndex,
                label = { Text(label) }
            )
        }
    }
}
