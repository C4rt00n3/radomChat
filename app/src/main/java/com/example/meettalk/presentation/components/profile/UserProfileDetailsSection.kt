package com.example.meettalk.presentation.components.profile

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.presentation.components.DetailRow
import com.example.meettalk.presentation.viewmodel.UserViewModel

/**
 * Exibe a seção de detalhes do perfil do usuário, buscando os dados do UserViewModel.
 *
 * @param userProfileViewModel O ViewModel que fornece os dados do perfil do usuário.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserProfileDetailsSection(
    userProfileViewModel: UserViewModel = viewModel()
) {
    val currentUser by userProfileViewModel.myUser.collectAsState(null)

    Column(Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.detalhes_do_perfil),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        DetailRow(
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.apelido_do_usuario),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            value = currentUser?.name ?: stringResource(R.string.carregando_dados)
        )
        Spacer(modifier = Modifier.height(8.dp))

        DetailRow(
            icon = {
                val genderIconResId = if (currentUser?.gender == Gender.F) R.drawable.baseline_face_3_24 else R.drawable.baseline_face_6_24
                Icon(
                    painter = painterResource(genderIconResId),
                    contentDescription = stringResource(R.string.sexo_do_usuario),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            value = when (currentUser?.gender) {
                Gender.M -> stringResource(R.string.masculino)
                Gender.F -> stringResource(R.string.feminino)
                else -> stringResource(R.string.nao_informado)
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        DetailRow(
            icon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.idade_do_usuario),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            value = currentUser?.age?.toString() ?: stringResource(R.string.indisponivel)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun UserProfileDetailsSectionPreview() {
    val userProfileViewModel: UserViewModel = viewModel()
    MaterialTheme {
        UserProfileDetailsSection()
    }
}