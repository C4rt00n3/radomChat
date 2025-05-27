package com.example.meettalk.presentation.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meettalk.presentation.components.ProfileDetailsSection
import com.example.meettalk.presentation.components.ProfileImageGrid
import com.example.meettalk.presentation.components.SearchPreferencesSection
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme

@Composable
fun EditarPerfilScreen(chatViewModel: ChatViewModel) {
    val user by chatViewModel.user.collectAsState(null)

    val imagePickerLauncher = rememberLauncherForActivityResult (
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
//                viewModel.uploadImage(it, context)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Editar Perfil",
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileImageGrid(chatViewModel) {
            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileDetailsSection(title = "DETALHES DO PERFIL", chatViewModel)

        Spacer(modifier = Modifier.height(24.dp))

        SearchPreferencesSection()
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilPreview() {
    MeetTalkTheme {
        val context = LocalContext.current
        EditarPerfilScreen(ChatViewModel(context))
    }
}