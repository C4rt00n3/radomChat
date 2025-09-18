package com.example.meettalk.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.data.local.AppRoutes
import com.example.meettalk.presentation.viewmodel.UserViewModel
import com.example.meettalk.utils.TokenManager
import com.example.meettalk.utils.isJwtExpired
import io.realm.kotlin.Realm

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Loading(
    realm: Realm,
    navController: NavHostController = rememberNavController(),
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        userViewModel.build(context, realm)
        val route = userViewModel.handleTokenValidation()
        navController.navigate(route) {
            popUpTo(AppRoutes.LOADING) { inclusive = true }
        }
    }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(Modifier.size(30.dp))
    }
}