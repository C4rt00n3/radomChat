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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.NavigationBarApp
import com.example.meettalk.presentation.components.SearchPreferencesSection
import com.example.meettalk.presentation.components.message.user
import com.example.meettalk.presentation.components.profile.ProfileDetailsSection
import com.example.meettalk.presentation.components.profile.ProfileImageGrid
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.ui.theme.Purple40
import io.realm.kotlin.Realm
import kotlinx.coroutines.launch


/**
 * Diálogo em tela cheia para edição completa dos dados do usuário.
 *
 * Este composable apresenta uma interface de edição com campos para Nome, Idade e Gênero,
 * utilizando componentes Material Design 3.
 *
 * @param currentUser O objeto User atual a ser editado, garantido como não nulo.
 * @param onDismissRequest Callback para quando o diálogo deve ser fechado (ex: botão de voltar).
 * @param onSaveClick Callback para quando o botão de salvar é clicado, passando o usuário atualizado.
 */
@Composable
fun EditUserFullScreenDialog(
    currentUser: User, onDismissRequest: () -> Unit, onSaveClick: (User) -> Unit
) {
    var editableName by remember { mutableStateOf(currentUser.name) }
    var editableAge by remember { mutableIntStateOf(currentUser.age) }
    val radioOptions = Gender.entries.toTypedArray()
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(currentUser.gender) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { // Ocupa a largura total do Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp), // Espaçamento inferior para o título
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Editar Perfil", fontWeight = FontWeight.SemiBold, fontSize = 20.sp
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fechar edição",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                OutlinedTextField(
                    value = editableName,
                    onValueChange = { editableName = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true // Garante que o nome fique em uma linha
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = editableAge.toString(),
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                            editableAge = newValue.toIntOrNull() ?: 0
                        } else if (newValue.isEmpty()) {
                            editableAge = 0
                        }
                    },
                    label = { Text("Idade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Gênero", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                Column(Modifier.selectableGroup()) {
                    radioOptions.forEach { genderOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (genderOption == selectedOption),
                                    onClick = { onOptionSelected(genderOption) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (genderOption == selectedOption), onClick = null
                            )
                            Text(
                                text = genderOption.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismissRequest, colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.size(8.dp))
                    Button(
                        onClick = {
                            if (editableName.isNotBlank() && editableAge > 0) {
                                onSaveClick(
                                    currentUser.copy(
                                        name = editableName,
                                        age = editableAge,
                                        gender = selectedOption
                                    )
                                )
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Preencha todos os campos corretamente.")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple40, contentColor = Color.White
                        )
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}

/**
 * Tela para edição e visualização do perfil do usuário.
 *
 * Esta tela permite ao usuário visualizar e editar suas imagens de perfil,
 * detalhes pessoais e preferências de busca.
 *
 * @param userViewModel O ViewModel responsável por gerenciar e fornecer os dados do perfil do usuário.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    realm: Realm? = null,
    userViewModel: UserViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val token by userViewModel.token.collectAsState("")
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    val `snack-barHostState` = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var index by remember { mutableIntStateOf(0) }
    val uiState by userViewModel.uiState.collectAsState()
    val user by userViewModel.myUser.collectAsState(null)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                val uuid = user?.profileImages?.getOrNull(index)?.uuid
                userViewModel.uploadProfileImage(it, uuid)
            }
        }
    )

    LaunchedEffect(Unit) {
        userViewModel.build(context, realm)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .safeContentPadding()
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(`snack-barHostState`) },
                topBar = {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        title = {
                            Text(
                                text = stringResource(R.string.detalhes_do_perfil),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        },
                        navigationIcon = {
                            IconButton({ navController.popBackStack() }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                                    contentDescription = "Voltar"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                scope.launch { `snack-barHostState`.showSnackbar("Funcionalidade de menu em breve!") }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu, contentDescription = "Menu"
                                )
                            }
                        },
                    )
                },
                bottomBar = { NavigationBarApp(navController) },
                floatingActionButton = {
                    FloatingActionButton(onClick = { showEditDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            "Editar perfil",
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
                        .background(MaterialTheme.colorScheme.secondary)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    when (uiState) {
                        is UserViewModel.UiState.Loading -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            ) { CircularProgressIndicator() }
                        }

                        is UserViewModel.UiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Erro ao carregar perfil: ${(uiState as UserViewModel.UiState.Error).message}",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        is UserViewModel.UiState.Success -> {
                            user?.let { actualUser ->
                                ProfileImageGrid(userViewModel = userViewModel, token) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        index = it
                                        imagePickerLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    } else {
                                        scope.launch {
                                            `snack-barHostState`.showSnackbar("Seleção de mídia avançada não disponível nesta versão do Android.")
                                        }
                                    }
                                }

                                ProfileDetailsSection(
                                    title = stringResource(R.string.detalhes_do_perfil),
                                    userViewModel = userViewModel
                                )

                                SearchPreferencesSection()

                                Spacer(modifier = Modifier.height(16.dp))

                                AnimatedVisibility(
                                    visible = showEditDialog,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    EditUserFullScreenDialog(
                                        currentUser = actualUser,
                                        onDismissRequest = { showEditDialog = false }
                                    ) { updatedUser ->
                                        userViewModel.updateUser(updatedUser) {
                                            showEditDialog = false
                                            scope.launch {
                                                `snack-barHostState`.showSnackbar("Perfil atualizado com sucesso!")
                                            }
                                        }
                                    }
                                }
                            }
                                ?: run {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f)
                                    ) {
                                        Text(
                                            "Dados do usuário não disponíveis.",
                                            color = Color.Gray
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