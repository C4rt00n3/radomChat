package com.example.meettalk.presentation.components.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meettalk.R
import kotlinx.coroutines.launch

/**
 * Barra de topo para a tela de perfil.
 *
 * @param navController O [NavController] para navegação.
 * @param snackBar O [SnackbarHostState] para exibir mensagens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAppBar(navController: NavController, snackBar: SnackbarHostState) {
    val coroutineScope = rememberCoroutineScope()
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        title = {
            Text(
                text = stringResource(R.string.detalhes_do_perfil),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        },
        navigationIcon = {
            IconButton({ navController.popBackStack() }) {
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                    contentDescription = stringResource(R.string.content_description_voltar)
                )
            }
        },
        actions = {
            val mensagem_menu_em_breve = stringResource(R.string.mensagem_menu_em_breve)
            IconButton(onClick = {
                coroutineScope.launch { snackBar.showSnackbar(
                    mensagem_menu_em_breve
                ) }
            }) {
                Icon(
                    imageVector = Icons.Filled.Menu, contentDescription = stringResource(R.string.content_description_menu)
                )
            }
        },
    )
}
