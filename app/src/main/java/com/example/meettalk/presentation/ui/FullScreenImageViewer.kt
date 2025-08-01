package com.example.meettalk.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.ImageMessage
import com.example.meettalk.presentation.components.NavigationBarApp
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPreventionMessage
import com.example.meettalk.presentation.components.images.ImageViewerTopBar
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import io.realm.kotlin.Realm

/**
 * Tela para visualização em tela cheia de uma imagem de mensagem específica.
 * Permite ao usuário ver a imagem com opções de navegação e uma barra inferior.
 *
 * @param imageUuid O UUID da [ImageMessage] a ser exibida.
 * @param chatUuid O UUID do chat ao qual a imagem pertence, usado para contexto no ViewModel.
 * @param realm Instância do Realm para operações de banco de dados (opcional, com valor padrão null).
 * @param viewModel O [UserViewModel] responsável por gerenciar e fornecer dados do usuário e mensagens.
 * @param navController O [NavController] para navegação entre telas.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FullScreenImageViewer(
    imageUuid: String,
    chatUuid: String,
    realm: Realm? = null,
    viewModel: UserViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current
    var displayedImageMessage: ImageMessage? by remember { mutableStateOf(null) }
    val authToken by viewModel.token.collectAsState("")

    // Carrega a imagem da mensagem usando o UUID fornecido
    LaunchedEffect(imageUuid, realm) {
        if (realm != null) {
            viewModel.build(context, realm)
            viewModel.getImageMessage(imageUuid)?.let {
                displayedImageMessage = it
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .safeDrawingPadding() // Garante que o conteúdo não se sobreponha às barras do sistema
            .fillMaxSize(),
        topBar = {
            ImageViewerTopBar(navController = navController)
        },
        bottomBar = {
            NavigationBarApp(navController)
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AsynchronousImageWithErrorPreventionMessage(
                imageMessage = displayedImageMessage,
                token = authToken,
                contentDescription = stringResource(R.string.content_description_imagem_mensagem_usuario),
                modifier = Modifier.fillMaxSize(),
                chatUuid = chatUuid,
                userViewModel = viewModel
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Preview FullScreenImageViewer")
@Composable
fun FullScreenImageViewerPreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MeetTalkTheme(darkTheme = false) {
            FullScreenImageViewer(imageUuid = "", chatUuid = "")
        }
    }
}