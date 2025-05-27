package com.example.meettalk

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.presentation.ui.Chat
import com.example.meettalk.presentation.ui.Login
import com.example.meettalk.presentation.ui.MessageScreen
import com.example.meettalk.presentation.viewmodel.BlockViewModel
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.presentation.viewmodel.LoginViewModel
import com.example.meettalk.utils.TokenManager

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    loginViewModel: LoginViewModel,
    chatViewModel: ChatViewModel,
    blockViewModel: BlockViewModel
) {
    val navController = rememberNavController()
    val tokenManager = TokenManager(LocalContext.current)
    var token by remember { mutableStateOf("") }
    val context = LocalContext.current;

    val baseUrl = stringResource(R.string.baseUrl)

    LaunchedEffect(Unit) {
        tokenManager.getToken()?.let {
            token = it
        }

        tokenManager.getUser()?.let {
            chatViewModel.setUser(it)
        }
    }

    LaunchedEffect(token) {
        val status = chatViewModel.verifyToken(token)
        if (status) {
            navController.navigate("chat") {
                popUpTo("home") { inclusive = true }
            }

            chatViewModel.connectSocket(
                "$baseUrl?token=${token.substring(7)}",
                context
            )
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable("home") { Login(loginViewModel, { navController.navigate(it) }) }
        composable("chat") { Chat(chatViewModel, navController) }
        composable("chat/{uuid}") { backStackEntry ->
            val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
            MessageScreen(uuid, chatViewModel, blockViewModel)
        }
    }
}