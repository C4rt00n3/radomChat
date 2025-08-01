package com.example.meettalk.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.SettingsOption
import com.example.meettalk.data.local.model.SettingsOptionType
import com.example.meettalk.presentation.components.NavigationBarApp
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.components.rememberAppSettingsOptions
import com.example.meettalk.presentation.components.settings.SettingsOptionItem
import com.example.meettalk.presentation.components.settings.SettingsTopBar
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import io.realm.kotlin.Realm

/**
 * Tela principal de configurações do aplicativo.
 * Exibe opções configuráveis e pode exibir listas de usuários bloqueados ou outras informações.
 *
 * @param realm Instância do Realm (atualmente não utilizada, pode ser removida se não houver uso futuro).
 * @param userViewModel ViewModel para gerenciar os dados do usuário e as operações relacionadas.
 * @param navController Controlador de navegação para a navegação entre telas.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppSettingsScreen(
    realm: Realm? = null,
    userViewModel: UserViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val currentUser by userViewModel.myUser.collectAsState(null)
    val userToken by userViewModel.token.collectAsState("")
    val context = LocalContext.current
    val appSettingsOptions = rememberAppSettingsOptions(navController)

    LaunchedEffect(Unit) {
        userViewModel.build(context, realm)
        userViewModel.fetchBlockedUsers(1, 20)
    }

    Scaffold(
        bottomBar = { NavigationBarApp(navController) },
        topBar = {
            SettingsTopBar(
                currentUser = currentUser,
                userToken = userToken,
                navController = navController,
                userViewModel = userViewModel
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(appSettingsOptions) { option ->
                SettingsOptionItem(option = option)
            }
        }
    }
}

/**
 * Preview da tela de configurações.
 * Exibe a tela de configurações em modo de pré-visualização para temas claro e escuro.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Preview AppSettingsScreen - Light Theme")
@Composable
fun AppSettingsScreenPreview() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MeetTalkTheme(darkTheme = false) {
            AppSettingsScreen()
        }
    }
}