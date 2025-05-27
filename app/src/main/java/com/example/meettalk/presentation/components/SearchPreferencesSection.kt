package com.example.meettalk.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.meettalk.R

@Composable
fun SearchPreferencesSection() {
    Column {
        Text(
            text = "PROCURO",
            color = Color(0xFFB085F5),
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        DetailRow(icon = {
            Icon(painter = painterResource(R.drawable.baseline_face_3_24,), contentDescription = "")
        }, label = "Sexo", value = "Feminino")
        DetailRow(icon = {
            Icon(Icons.Default.DateRange, contentDescription = "Idade de")
        }, label = "Idade de", value = "18", trailingCheck = true)
    }
}
