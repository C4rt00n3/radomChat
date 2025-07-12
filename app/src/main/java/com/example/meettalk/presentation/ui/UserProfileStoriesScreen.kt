package com.example.meettalk.presentation.ui

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
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.presentation.components.chat.ChatTopBar
import com.example.meettalk.presentation.components.images.AsynchronousImageWithErrorPrevention
import com.example.meettalk.presentation.viewmodel.UserViewModel
import io.realm.kotlin.Realm

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserProfileStoriesScreen(
    uuid: String,
    currentUser: User? = null,
    realm: Realm? = null,
    navController: NavHostController = rememberNavController(),
    userViewModel: UserViewModel = viewModel(),
) {
    var user: User? by remember {
        mutableStateOf(
            currentUser ?: User(
                uuid = "123e4567-e89b-12d3-a456-426614174000",
                name = "Ana Souza",
                age = 28,
                gender = Gender.F,
                chatParticipants = listOf(),
                profileImages = listOf(
                    ImageProfile(
                        uuid = "1fe6efc5-d3eb-45c7-aab3-eb791e847a6e",
                        src = null,
                        userUuid = "123e4567-e89b-12d3-a456-426614174000"
                    )
                )
            )
        )
    }
    val token by userViewModel.token.collectAsState("")
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val dynamicOptions = emptyList<OptionsMenu>()

    LaunchedEffect(uuid) {
        userViewModel.build(context, realm)
        userViewModel.findUser(uuid)?.let {
            user = it
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.padding(top = 16.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
                title = {},
                navigationIcon = {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton({ navController.popBackStack() }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                                    contentDescription = "Voltar"
                                )
                            }
                            AsynchronousImageWithErrorPrevention(
                                imageProfile = user?.profileImages?.first(),
                                token = token,
                                contentDescription = stringResource(R.string.imagem_do_usu_rio),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.size(8.dp))
                        }
                    }
                },
                actions = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            expanded = !expanded
                        }) {
                            Icon(
                               painter = painterResource(R.drawable.menu_burger),
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(expanded = expanded,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            onDismissRequest = { expanded = !expanded }) {
                            dynamicOptions.forEach {
                                DropdownMenuItem(text = { Text(it.text) }, onClick = {
                                    expanded = false
                                    it.onClick()
                                })
                            }
                        }
                    }
                },
            )
            Spacer(Modifier.padding(top = 16.dp))
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val imageUrls = user?.profileImages ?: emptyList()

            ImageCarouselFullScreen(images = imageUrls, token = token)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 0f,
                            endY = 500f
                        )
                    )
                    .padding(
                        bottom = 32.dp,
                        start = 16.dp,
                        end = 16.dp,
                        top = 64.dp
                    ),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = user?.name ?: "...",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gênero: ${user?.gender?.name ?: "..."}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "Idade: ${user?.age ?: "..."} anos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarouselFullScreen(images: List<ImageProfile>, token: String) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(modifier = Modifier.fillMaxSize()) {
        val currentImage = images.getOrNull(pagerState.currentPage)
        if (currentImage != null) {
            AsynchronousImageWithErrorPrevention(
                imageProfile = currentImage,
                token = token,
                contentDescription = "Fundo borrado da imagem de perfil",
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 32.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val imageUrl = images.getOrNull(page)
            AsynchronousImageWithErrorPrevention(
                imageProfile = imageUrl,
                token = token,
                contentDescription = "Imagem de perfil do usuário",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Preview
@Composable
fun UserProfileStoriesScreenPreview() {
    MaterialTheme {
        UserProfileStoriesScreen("", userViewModel = viewModel())
    }
}