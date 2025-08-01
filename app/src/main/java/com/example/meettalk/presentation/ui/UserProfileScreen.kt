package com.example.meettalk.presentation.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.R
import com.example.meettalk.presentation.components.NavigationBarApp
import com.example.meettalk.presentation.components.SearchPreferencesSection
import com.example.meettalk.presentation.components.UiState
import com.example.meettalk.presentation.components.WelcomeInfoDialog
import com.example.meettalk.presentation.components.profile.ProfileAppBar
import com.example.meettalk.presentation.components.profile.ProfileEditDialog
import com.example.meettalk.presentation.components.profile.ProfileImageGrid
import com.example.meettalk.presentation.components.profile.UserProfileDetailsSection
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import io.realm.kotlin.Realm
import kotlinx.coroutines.launch


/**
 * Tela para edição e visualização do perfil do usuário.
 *
 * Esta tela permite ao usuário visualizar e editar suas imagens de perfil,
 * detalhes pessoais e preferências de busca.
 *
 * @param viewModel O [UserViewModel] responsável por gerenciar e fornecer os dados do perfil do usuário.
 * @param realm Instância do Realm para operações de banco de dados (opcional, com valor padrão null).
 * @param showWelcome Indica se a mensagem de boas-vindas deve ser exibida ao iniciar.
 * @param navController O [NavController] para navegação entre telas.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserProfileScreen(
    viewModel: UserViewModel = viewModel(),
    realm: Realm? = null,
    showWelcome: Boolean = false,
    navController: NavController = rememberNavController()
) {
    var isWelcomeDialogVisible by remember { mutableStateOf(showWelcome) }
    val userToken by viewModel.token.collectAsState("")
    val context = LocalContext.current
    var isEditDialogVisible by remember { mutableStateOf(false) }
    val screenSnackBarState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var currentImageIndex by remember { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()
    val userProfileData by viewModel.myUser.collectAsState(null)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { imageUri ->
            imageUri?.let {
                val imageId = userProfileData?.profileImages?.getOrNull(currentImageIndex)?.uuid
                viewModel.uploadProfileImage(it, imageId, currentImageIndex)
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.build(context, realm)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(screenSnackBarState) },
        topBar = { ProfileAppBar(navController, screenSnackBarState) },
        bottomBar = { NavigationBarApp(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { isEditDialogVisible = true }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.content_description_editar_perfil),
                    tint = Color.White,
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val messageSelectText = stringResource(R.string.mensagem_selecao_midia_indisponivel)
            val messageProfileText = stringResource(R.string.mensagem_perfil_atualizado_sucesso)

            when (uiState) {
                is UiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) { CircularProgressIndicator() }
                }

                is UiState.Error -> {
                    val errorMessage = (uiState as UiState.Error).message
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.erro_carregar_perfil, errorMessage),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                is UiState.Success -> {
                    userProfileData?.let { currentUser ->
                        ProfileImageGrid(viewModel = viewModel, authToken = userToken) { clickedImageIndex ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                currentImageIndex = clickedImageIndex
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            } else {
                                coroutineScope.launch {
                                    screenSnackBarState.showSnackbar(
                                        messageSelectText
                                    )
                                }
                            }
                        }

                        UserProfileDetailsSection(userProfileViewModel = viewModel)

                        SearchPreferencesSection()

                        Spacer(modifier = Modifier.height(16.dp))

                        AnimatedVisibility(
                            visible = isEditDialogVisible,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            ProfileEditDialog(
                                user = currentUser,
                                onDismiss = { isEditDialogVisible = false }
                            ) { updatedUser ->
                                viewModel.updateUser(updatedUser) {
                                    isEditDialogVisible = false
                                    coroutineScope.launch {
                                        screenSnackBarState.showSnackbar(
                                            messageProfileText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isWelcomeDialogVisible) {
        WelcomeInfoDialog { isWelcomeDialogVisible = false }
    }
}

/**
 * Preview do Composable `UserProfileScreen`.
 * Permite visualizar o componente no Android Studio sem a necessidade de rodar no dispositivo.
 */
@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    MeetTalkTheme {
        UserProfileScreen()
    }
}