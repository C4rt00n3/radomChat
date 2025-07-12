package com.example.meettalk.presentation.components.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.presentation.components.DetailRow
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.presentation.viewmodel.UserViewModel

@Composable
fun ProfileDetailsSection(title: String, userViewModel: UserViewModel) {
    val user by userViewModel.myUser.collectAsState(null)

    Column(Modifier.padding(16.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        DetailRow(
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.apelido),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            value = user?.name ?: "Carregando..."
        )
        Spacer(modifier = Modifier.height(8.dp))
        DetailRow(
            icon = {
                Icon(
                    painter = painterResource(if(user?.gender == Gender.F) R.drawable.baseline_face_3_24 else R.drawable.baseline_face_6_24),
                    contentDescription = "Sexo",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            value = "Masculino"
        )
        Spacer(modifier = Modifier.height(8.dp))
        DetailRow(
            icon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Idade",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            value = (user?.age ?: 18).toString() ?: "..."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilPreview() {
    val context = LocalContext.current
    val chatViewModel: UserViewModel = viewModel()
    MaterialTheme {
        ProfileDetailsSection("Editar Perfil", chatViewModel)
    }
}
