package com.example.meettalk.presentation.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meettalk.R
import com.example.meettalk.presentation.components.SearchPreferencesSection
import com.example.meettalk.presentation.components.profile.ProfileDetailsSection
import com.example.meettalk.presentation.components.profile.ProfileImageGrid
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme

/**
 * Tela para edição do perfil do usuário.
 *
 * Esta tela permite ao usuário visualizar e editar suas imagens de perfil,
 * detalhes pessoais e preferências de busca. Ela foi aprimorada para lidar com
 * a responsividade e garantir um espaçamento lateral adequado ("respiro").
 *
 * @param userViewModel O ViewModel responsável por gerenciar e fornecer os dados do perfil do usuário.
 * @param token O token de autenticação do usuário. Atualmente não é utilizado diretamente na UI dos componentes filhos,
 * mas pode ser necessário para operações de upload no ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class) // Anotação necessária para usar TopAppBar
@Composable
fun EditorFirescreen(userViewModel: UserViewModel, token: String) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.detalhes_do_perfil), // Usar string resource para o título
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileImageGrid(userViewModel = userViewModel, token) {
                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            ProfileDetailsSection(
                title = stringResource(R.string.detalhes_do_perfil), // Título da seção via string resource
                userViewModel = userViewModel
            )

            SearchPreferencesSection()

            Spacer(modifier = Modifier.height(16.dp)) // Ajuste conforme necessário
        }
    }
}

/**
 * Preview do Composable `EditarPerfilScreen`.
 * Permite visualizar o componente no Android Studio sem a necessidade de rodar no dispositivo.
 */
@Preview(showBackground = true)
@Composable
fun PerfilPreview() {
    MeetTalkTheme {
        // Instancia um ChatViewModel simulado para o Preview.
        EditorFirescreen(userViewModel = viewModel(), token = "")
    }
}