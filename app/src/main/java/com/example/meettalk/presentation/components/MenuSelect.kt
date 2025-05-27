package com.example.meettalk.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun MenuSelect(listOptions: List<OptionsMenu>) {
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.wrapContentSize()) {
        IconButton(
            onClick = {
                expanded = !expanded
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.menu_burger),
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.text) },
                    onClick = {
                        expanded = false
                        coroutineScope.launch {
                            try {
                                option.onClick()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                )
            }
        }
    }
}
