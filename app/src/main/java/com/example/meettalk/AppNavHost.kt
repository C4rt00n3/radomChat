package com.example.meettalk

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.data.local.AppRoutes
import com.example.meettalk.presentation.ui.ChatScreen
import com.example.meettalk.presentation.ui.EditorFirescreen
import com.example.meettalk.presentation.ui.Login
import com.example.meettalk.presentation.ui.MessageScreen
import com.example.meettalk.presentation.ui.UserProfileStoriesScreen
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.presentation.viewmodel.LoginViewModel
import com.example.meettalk.presentation.viewmodel.MessageViewModel
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.utils.TokenManager
import com.example.meettalk.utils.isInternetAvailable
import com.example.meettalk.utils.isJwtExpired

/**
 * Composable raiz para a navegação da aplicação.
 * Gerencia a lógica inicial de autenticação, carregamento de token/usuário
 * e configuração do NavHost com as telas da aplicação.
 *
 * @param loginViewModel ViewModel para a tela de Login.
 * @param chatViewModel ViewModel para as telas de Chat e Mensagens.
 *
 * @RequiresApi VANILLA_ICE_CREAM (Android 15): Esta anotação indica que o Composable
 * utiliza APIs disponíveis a partir do Android 15. Avalie se é estritamente necessário
 * ou se pode ser adaptado para versões anteriores para maior compatibilidade.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavHost(
    loginViewModel: LoginViewModel,
    chatViewModel: ChatViewModel,
    messageViewModel: MessageViewModel,
    userViewModel: UserViewModel
) {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current
    val baseUrl = stringResource(R.string.baseUrl)
    val token by userViewModel.token.collectAsState("")

    LaunchedEffect(Unit) {
        userViewModel.pickToken()
        userViewModel.observeToken {
            chatViewModel.setToken(it)
        }
    }

    LaunchedEffect(token) {
        if (token.isBlank()) {
            return@LaunchedEffect
        }

        val rawToken = token.replace("Bearer ", "", ignoreCase = true)

        if (isJwtExpired(rawToken)) {
            TokenManager(context).clearToken()
            Toast.makeText(context, "Sessão expirada!", Toast.LENGTH_LONG).show()
            return@LaunchedEffect
        }

        if (isInternetAvailable(context)) {
            chatViewModel.connectSocket("$baseUrl?token=$rawToken", context)
            navController.navigate(AppRoutes.CHAT_LIST)
            Toast.makeText(context, "Sucesso!", Toast.LENGTH_LONG).show()
        } else {
            println("Internet não disponível. Não foi possível conectar o socket.")
            Toast.makeText(context, "Sem conexão com a internet.", Toast.LENGTH_LONG).show()
        }

        if (chatViewModel.verifyToken(token)) {
            chatViewModel.constrictedImages(token)
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable(AppRoutes.EDIT_MY_PERFIL) {
            EditorFirescreen(userViewModel = userViewModel, token)
        }
        composable(AppRoutes.HOME) {
            Login(
                loginViewModel = loginViewModel
            ) { route -> navController.navigate(route) }
        }
        composable(AppRoutes.CHAT_LIST) {
            ChatScreen(
                chatViewModel = chatViewModel,
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable(AppRoutes.CHAT_MESSAGES_PATTERN) { backStackEntry ->
            val uuid = backStackEntry.arguments?.getString("uuid") ?: ""

            MessageScreen(
                uuid,
                userViewModel = userViewModel,
                viewModel = messageViewModel,
                navigate = navController
            )
        }
        composable(AppRoutes.USER_PERFIL) { backStackEntry ->
            val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
            UserProfileStoriesScreen(uuid = uuid, userViewModel = userViewModel)
        }

    }
}