package com.example.meettalk

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.data.local.AppRoutes
import com.example.meettalk.presentation.components.PaddingGlobal
import com.example.meettalk.presentation.ui.AppSettingsScreen
import com.example.meettalk.presentation.ui.ChatListScreen
import com.example.meettalk.presentation.ui.ChatScreen
import com.example.meettalk.presentation.ui.FullScreenImageViewer
import com.example.meettalk.presentation.ui.Loading
import com.example.meettalk.presentation.ui.LoginScreen
import com.example.meettalk.presentation.ui.UserExplorerScreen
import com.example.meettalk.presentation.ui.UserProfileScreen
import com.example.meettalk.presentation.ui.UserStoriesScreen
import com.example.meettalk.presentation.viewmodel.UserViewModel
import io.realm.kotlin.Realm

/**
 * Composable raiz para a navegação da aplicação.
 * Gerencia a lógica inicial de autenticação, carregamento de token/usuário
 * e configuração do NavHost com as telas da aplicação.
 *
 * @param realm Instância do Realm Database, injetada no composable.
 *
 * @RequiresApi VANILLA_ICE_CREAM (Android 15):
 * Esta anotação indica que o Composable utiliza APIs disponíveis a partir do Android 15.
 *
 * Considerações Importantes:
 * 1. Compatibilidade: O Android 15 ainda está em desenvolvimento ou preview.
 * Se o aplicativo precisar suportar versões anteriores do Android, você precisará
 * reavaliar se as APIs específicas usadas são realmente exclusivas do Android 15
 * ou se há alternativas para versões mais antigas. Se não, o `minSdk` do seu
 * projeto precisará ser definido para Android 15.
 * 2. Estabilidade: APIs de preview podem mudar antes do lançamento final.
 * Esteja ciente de que pode ser necessário ajustar o código no futuro.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavHost(
    realm: Realm
) {
    val context = LocalContext.current
    val navController: NavHostController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()

    LaunchedEffect(Unit) {
        userViewModel.build(context, realm)
    }

    PaddingGlobal {
        NavHost(
            navController = navController, startDestination = AppRoutes.LOADING,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            composable(AppRoutes.EDIT_MY_PERFIL) { backStackEntry ->
                val show = (backStackEntry.arguments?.getString("show") ?: "false").toBoolean()
                UserProfileScreen(
                    navController = navController,
                    viewModel = userViewModel,
                    showWelcome = show,
                    realm = realm
                )
            }
            composable(AppRoutes.LOGIN) {
                LoginScreen(realm, navController = navController)
            }
            composable(AppRoutes.CHAT_LIST) {
                ChatListScreen(
                    navController = navController, realm = realm,
                )
            }
            composable(AppRoutes.CHAT_MESSAGES_PATTERN) { backStackEntry ->
                val userUuid = backStackEntry.arguments?.getString("uuid") ?: ""
                ChatScreen(
                    userUuid = userUuid,
                    navController = navController,
                    userViewModel = userViewModel,
                    realm = realm,
                )
            }
            composable(AppRoutes.IMAGE_VIEW) { backStackEntry ->
                val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
                val chatUuid = backStackEntry.arguments?.getString("chat_uuid") ?: ""

                FullScreenImageViewer(uuid, chatUuid, realm, userViewModel, navController)
            }
            composable(AppRoutes.USER_PERFIL) { backStackEntry ->
                val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
                UserStoriesScreen(userUuid = uuid, realm = realm, navController = navController)
            }
            composable(AppRoutes.LOADING) {
                Loading(realm, navController = navController)
            }
            composable(AppRoutes.EXPLORER_USERS) {
                UserExplorerScreen(
                    realm = realm,
                    navController = navController,
                    userViewModel = userViewModel,
                )
            }
            composable(AppRoutes.OPTIONS_APP) {
                AppSettingsScreen(
                    realm = realm,
                    userViewModel = userViewModel,
                    navController = navController
                )
            }
        }
    }
}