package com.example.meettalk.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.presentation.viewmodel.ChatViewModel

@Composable
fun ProfileDetailsSection(title: String, chatViewModel: ChatViewModel) {
    val user by chatViewModel.user.collectAsState(null)

    Column {
        Text(
            text = title,
            color = Color(0xFFB085F5),
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        DetailRow(
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Apelido",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            label = "Apelido",
            value = user?.name ?: "Carregando..."
        )

        DetailRow(
            icon = {
                Icon(
                    painter = painterResource(if(user?.gender == Gender.F) R.drawable.baseline_face_3_24 else R.drawable.baseline_face_6_24),
                    contentDescription = "Sexo",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            label = "Sexo",
            value = "Masculino"
        )

        DetailRow(
            icon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Idade",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            label = "Minha idade",
            value = user?.age.toString() ?: "..."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilPreview() {
    val context = LocalContext.current
    MaterialTheme {
        ProfileDetailsSection("Editar Perfil", ChatViewModel(context))
    }
}
