package com.example.meettalk.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.data.local.AppRoutes
import com.example.meettalk.ui.theme.BlueOther

@Composable
fun NavigationBarApp(
    navController: NavController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = AppRoutes.CHAT_LIST
    var selectedDestination by rememberSaveable { mutableStateOf(startDestination) }

    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            AppRoutes.CHAT_LIST -> selectedDestination = AppRoutes.CHAT_LIST
            AppRoutes.EXPLORER_USERS -> selectedDestination = AppRoutes.EXPLORER_USERS
            AppRoutes.EDIT_MY_PERFIL -> selectedDestination = AppRoutes.EDIT_MY_PERFIL
            AppRoutes.OPTIONS_APP -> selectedDestination = AppRoutes.OPTIONS_APP
        }
    }

    val colors = NavigationBarItemDefaults.colors(
        selectedIconColor = BlueOther,
        selectedTextColor = BlueOther,
        indicatorColor = Color.White.copy(0.3f)
    )

    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets,
        modifier = Modifier.clip(
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        NavigationBarItem(
            selected = selectedDestination == AppRoutes.CHAT_LIST,
            onClick = {
                if(currentRoute == AppRoutes.CHAT_LIST)
                    return@NavigationBarItem
                navController.navigate(route = AppRoutes.CHAT_LIST) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                selectedDestination = AppRoutes.CHAT_LIST
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Mail,
                    contentDescription = "Caixa de entrada",
                    modifier = Modifier.size(30.dp),
                )
            },
            label = { Text("Conversas") },
            colors = colors
        )

        NavigationBarItem(
            selected = selectedDestination == AppRoutes.EXPLORER_USERS,
            onClick = {
                if(currentRoute == AppRoutes.EXPLORER_USERS)
                    return@NavigationBarItem
                navController.navigate(route = AppRoutes.EXPLORER_USERS) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                selectedDestination = AppRoutes.EXPLORER_USERS
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Explore,
                    contentDescription = "Buscar usuarios aleatorios",
                    modifier = Modifier.size(30.dp),
                )
            },
            label = { Text("Explorar") },
            colors = colors
        )

        NavigationBarItem(
            selected = selectedDestination == AppRoutes.EDIT_MY_PERFIL,
            onClick = {
                if(currentRoute == AppRoutes.EDIT_MY_PERFIL)
                    return@NavigationBarItem
                navController.navigate(route = AppRoutes.EDIT_MY_PERFIL) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                selectedDestination = AppRoutes.EDIT_MY_PERFIL
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Meu perfil",
                    modifier = Modifier.size(30.dp),
                )
            },
            label = { Text("Meu perfil") },
            colors = colors
        )

        NavigationBarItem(
            selected = selectedDestination == AppRoutes.OPTIONS_APP,
            onClick = {
                if(currentRoute == AppRoutes.OPTIONS_APP)
                    return@NavigationBarItem
                navController.navigate(route = AppRoutes.OPTIONS_APP) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                selectedDestination = AppRoutes.OPTIONS_APP
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Configurações",
                    modifier = Modifier.size(30.dp),
                )
            },
            label = { Text("Configurações") },
            colors = colors
        )
    }
}