package com.example.meettalk.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meettalk.R
import com.example.meettalk.ui.theme.MeetTalkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputBar(
    onTextChange: (String) -> Unit,
    value: String,
    isEditing: Boolean,
    onSend: () -> Unit
) {
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp

    val containerColor = Color.Black.copy(0.6f)

    val colors = TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White.copy(0.9f),
        disabledTextColor = Color.Gray.copy(0.5f),
        errorTextColor = Color.Red,
        focusedContainerColor = containerColor,
        unfocusedContainerColor = containerColor,
        disabledContainerColor = containerColor,
        errorContainerColor = Color.Red.copy(0.1f),
        cursorColor = Color.White,
        errorCursorColor = Color.Red,
        selectionColors = LocalTextSelectionColors.current,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Red,
        focusedLeadingIconColor = Color.White,
        unfocusedLeadingIconColor = Color.White.copy(0.8f),
        disabledLeadingIconColor = Color.Gray.copy(0.5f),
        errorLeadingIconColor = Color.Red,
        focusedTrailingIconColor = Color.White,
        unfocusedTrailingIconColor = Color.White.copy(0.8f),
        disabledTrailingIconColor = Color.Gray.copy(0.5f),
        errorTrailingIconColor = Color.Red,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White.copy(0.6f),
        disabledLabelColor = Color.Gray,
        errorLabelColor = Color.Red,
        focusedPlaceholderColor = Color.White.copy(0.6f),
        unfocusedPlaceholderColor = Color.White.copy(0.6f),
        disabledPlaceholderColor = Color.Gray.copy(0.5f),
        errorPlaceholderColor = Color.Red.copy(0.5f),
        focusedSupportingTextColor = Color.White,
        unfocusedSupportingTextColor = Color.White.copy(0.6f),
        disabledSupportingTextColor = Color.Gray.copy(0.5f),
        errorSupportingTextColor = Color.Red,
        focusedPrefixColor = Color.White,
        unfocusedPrefixColor = Color.White,
        disabledPrefixColor = Color.White.copy(0.4f),
        errorPrefixColor = Color.White,
        focusedSuffixColor = Color.White,
        unfocusedSuffixColor = Color.White,
        disabledSuffixColor = Color.White.copy(0.4f),
        errorSuffixColor = Color.White,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            leadingIcon = {
                IconButton(
                    {},
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor =Color.White,
                        contentColor = containerColor
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.camicon),
                        contentDescription = "Icone de camera. Tire uma foto ou escolha em sua galeria",
                        modifier = Modifier.size(15.dp)
                    )
                }
            },
            modifier = Modifier
                .width(screenWidth * 0.9f)
                .padding(bottom = 16.dp),
            value = value,
            onValueChange = { onTextChange(it) },
            shape = CircleShape,
            placeholder = {
                Text(
                    text = "Enter your message...",
                    color = Color.White.copy(0.6f),
                )
            },
            colors = colors,
            maxLines = 4,
            trailingIcon = {
                ButtonIconSendOrMic(
                    value.isNotBlank(),
                    isEditing,
                ) { onSend() }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun InputBarPreview() {
    MeetTalkTheme {
        InputBar({}, "", true) { }
    }
}