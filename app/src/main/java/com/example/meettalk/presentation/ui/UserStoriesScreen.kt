package com.example.meettalk.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.OptionsMenu
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.Preference
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.FullScreenImageCarousel
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.components.user.UserProfileOverlay
import com.example.meettalk.presentation.components.user.UserStoriesTopAppBar
import com.example.meettalk.presentation.viewmodel.UserViewModel
import io.realm.kotlin.Realm
import java.util.UUID

/**
 * Tela que exibe as "histórias" (múltiplas imagens de perfil) e detalhes de um usuário específico.
 * Permite a navegação pelas imagens e a visualização de informações básicas do perfil.
 *
 * @param userUuid O UUID do usuário cujos detalhes e imagens devem ser exibidos.
 * @param initialUser O objeto [User] inicial para exibição, útil para prévias ou dados já carregados.
 * Se nulo, o usuário será buscado via ViewModel.
 * @param realm Instância do Realm para operações de banco de dados (opcional, com valor padrão null).
 * @param navController O [NavHostController] para navegação.
 * @param userViewModel O [UserViewModel] responsável por gerenciar e fornecer os dados do usuário.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserStoriesScreen(
    userUuid: String,
    initialUser: User? = null,
    realm: Realm? = null,
    navController: NavHostController = rememberNavController(),
    userViewModel: UserViewModel = viewModel(),
) {
    var displayedUser: User? by remember { mutableStateOf(initialUser) }
    val userAuthToken by userViewModel.token.collectAsState("")
    val screenContext = LocalContext.current
    var isMenuExpanded by remember { mutableStateOf(false) }

    val menuOptions = emptyList<OptionsMenu>()

    LaunchedEffect(userUuid) {
        userViewModel.build(screenContext, realm)
        userViewModel.findUser(userUuid)?.let {
            displayedUser = it
        }
    }

    Scaffold(
        topBar = {
            UserStoriesTopAppBar(
                user = displayedUser,
                userAuthToken = userAuthToken,
                navController = navController,
                isMenuExpanded = isMenuExpanded,
                onMenuExpandChange = { isMenuExpanded = it },
                menuOptions = menuOptions
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val userProfileImages = displayedUser?.profileImages ?: emptyList()

            FullScreenImageCarousel(images = userProfileImages, authToken = userAuthToken)

            UserProfileOverlay(user = displayedUser)
        }
    }
}

@Preview
@Composable
fun UserStoriesScreenPreview() {
    MaterialTheme {
        // Exemplo de usuário para o Preview
        val sampleUser = User(
            uuid = "123e4567-e89b-12d3-a456-426614174000",
            name = "Maria Clara",
            birthDate = "1995-07-01T02:00:00.000Z", // Data de nascimento para calcular a idade
            chatParticipants = listOf(),
            preference = Preference(
                uuid = UUID.randomUUID().toString(),
                gender = Gender.F,
                maxAge = 25
            ),
            profileImages = listOf(
                ImageProfile(uuid = "img1-uuid", userUuid = ""),
                ImageProfile(uuid = "img2-uuid", userUuid = ""),
                ImageProfile(uuid = "img3-uuid", userUuid = "")
            )
        )
        UserStoriesScreen(
            userUuid = sampleUser.uuid,
            initialUser = sampleUser,
            userViewModel = viewModel()
        )
    }
}