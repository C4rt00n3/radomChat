package com.example.meettalk.presentation.components.images

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.meettalk.R

/**
 * Barra de topo para a tela de visualização de imagem em tela cheia.
 * Contém um ícone de "voltar" para navegação.
 *
 * @param navController O [NavController] para controlar a navegação.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerTopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = stringResource(R.string.content_description_voltar_tela),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable {
                    navController.popBackStack()
                }
            )
        },
        title = {}
    )
}
