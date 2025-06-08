package com.example.meettalk

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.meettalk.data.local.model.RealmClass.BlockRealm
import com.example.meettalk.data.local.model.RealmClass.ChatParticipantRealm
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.RealmClass.ImageMessageRealm
import com.example.meettalk.data.local.model.RealmClass.ImageProfileRealm
import com.example.meettalk.data.local.model.RealmClass.LocationRealm
import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.presentation.viewmodel.LoginViewModel
import com.example.meettalk.presentation.viewmodel.MessageViewModel
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val config = RealmConfiguration.Builder(
            schema = setOf(
                UserRealm::class,
                BlockRealm::class,
                ChatRealm::class,
                LocationRealm::class,
                ImageProfileRealm::class,
                ChatParticipantRealm::class,
                ImageMessageRealm::class,
                MessageRealm::class
            )
        )
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .build()

        val realm by lazy {
            Realm.open(config)
        }
        val loginViewModel = LoginViewModel(this, realm)
        val chatViewModel = ChatViewModel(this, realm)
        val messageViewModel = MessageViewModel(this, realm)
        val userViewModel = UserViewModel(this, realm)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            MeetTalkTheme {
                AppNavHost(loginViewModel, chatViewModel, messageViewModel, userViewModel)
            }
        }
    }
}