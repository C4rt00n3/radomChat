package com.example.meettalk.presentation.ui

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.LoginRequest
import com.example.meettalk.presentation.components.LoginComponents.AlternativeLoginOptions
import com.example.meettalk.presentation.components.LoginComponents.CreateAccountLink
import com.example.meettalk.presentation.components.LoginComponents.EmailInputField
import com.example.meettalk.presentation.components.LoginComponents.LoginHeader
import com.example.meettalk.presentation.components.LoginComponents.PasswordInputField
import com.example.meettalk.presentation.components.LoginComponents.PrimaryLoginButton
import com.example.meettalk.presentation.viewmodel.LoginViewModel
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.utils.SignInWithGoogleFunctions
import com.example.meettalk.utils.TokenManager
import io.realm.kotlin.Realm
import kotlinx.coroutines.launch

/**
 * Tela de Login principal da aplicação.
 * Permite que o usuário insira credenciais ou utilize opções de login social (Google, Facebook).
 *
 * @param realm Instância do Realm para operações de banco de dados (opcional, com valor padrão null).
 * @param viewModel O [LoginViewModel] responsável por gerenciar a lógica de login.
 * @param navController O [NavController] para navegação entre telas.
 */
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun LoginScreen(
    realm: Realm? = null,
    viewModel: LoginViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    LaunchedEffect(Unit) {
        viewModel.build(context, realm)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginHeader(screenHeight)

        Column(
            modifier = Modifier
                .safeContentPadding()
                .width(width = (screenWidth * 0.80f)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmailInputField(
                email = emailInput,
                onEmailChange = { emailInput = it },
                isError = isEmailError,
                errorMessage = stringResource(R.string.erro_email_invalido)
            )

            PasswordInputField(
                password = passwordInput,
                onPasswordChange = { passwordInput = it },
                isError = isPasswordError,
                errorMessage = stringResource(R.string.erro_senha_vazia)
            )

            PrimaryLoginButton(
                onClick = {
                    isEmailError = !viewModel.isValidEmail(emailInput)
                    isPasswordError = passwordInput.isBlank()

                    if (!isEmailError && !isPasswordError) {
                        viewModel.login(LoginRequest(emailInput, passwordInput)) { loginResponse ->
                            TokenManager(context).apply {
                                saveToken(loginResponse.accessToken)
                                saveUser(loginResponse.user)
                            }
                            Log.d("LoginScreen", "Login bem-sucedido. Navegando para chat.")
                            navController.navigate("chat")
                        }
                    }
                }
            )

            AlternativeLoginOptions(
                screenWidth = screenWidth,
                onGoogleSignIn = {
                    val signInWithGoogle = SignInWithGoogleFunctions(context)
                    signInWithGoogle.signInUser { googleToken ->
                        viewModel.viewModelScope.launch {
                            Log.d("LoginScreen", "Token Google recebido: $googleToken")
                            viewModel.singInGoogle(googleToken)?.let { loginResponse ->
                                TokenManager(context).apply {
                                    saveToken(loginResponse.accessToken)
                                    saveRefreshToken(loginResponse.refreshToken)
                                    saveUser(loginResponse.user)
                                }
                                Log.d("LoginScreen", "Login Google bem-sucedido. Redirecionando para edição de perfil.")
                                navController.navigate("edit/user/true")
                            }
                        }
                    }
                },
                onFacebookSignIn = { /* Lógica de login com Facebook */ }
            )

            CreateAccountLink(onClick = { /* Lógica para navegar para tela de criação de conta */ })
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MeetTalkTheme {
        LoginScreen()
    }
}