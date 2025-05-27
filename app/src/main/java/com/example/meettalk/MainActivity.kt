package com.example.meettalk

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.data.repository.SocketManager
import com.example.meettalk.presentation.ui.Chat
import com.example.meettalk.presentation.ui.Login
import com.example.meettalk.presentation.ui.MessageScreen
import com.example.meettalk.presentation.viewmodel.BlockViewModel
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.presentation.viewmodel.LoginViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.utils.TokenManager

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val loginViewModel = LoginViewModel(this)
        val chatViewModel = ChatViewModel(this)
        val blockViewModel = BlockViewModel(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            MeetTalkTheme {
                AppNavHost(loginViewModel, chatViewModel, blockViewModel)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val context = LocalContext.current
    MeetTalkTheme {
        AppNavHost(LoginViewModel(context), ChatViewModel(context), BlockViewModel(context))
    }
}