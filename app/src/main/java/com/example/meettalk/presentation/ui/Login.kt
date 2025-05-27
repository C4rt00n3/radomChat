package com.example.meettalk.presentation.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.LoginRequest
import com.example.meettalk.presentation.viewmodel.LoginViewModel
import com.example.meettalk.ui.theme.ButtonColorGray
import com.example.meettalk.ui.theme.DarkBlueLine
import com.example.meettalk.ui.theme.MeetTalkTheme
import com.example.meettalk.ui.theme.TextColorGray
import com.example.meettalk.ui.theme.TextColorGrayLight
import com.example.meettalk.utils.TokenManager

@Composable
fun Login(loginViewModel: LoginViewModel, navigation: (route: String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(R.drawable.subtract),
                contentDescription = "subtract",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.35f)
            )
            Text(
                text = "Login",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Column(
            modifier = Modifier
                .safeContentPadding()
                .width(width = (screenWidth * 0.80f)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    isError = emailError,
                    label = { Text("Email", modifier = Modifier.padding(vertical = 8.dp)) },
                    placeholder = { Text("Digite seu email") },
                    modifier = Modifier.padding(bottom = if (emailError) 8.dp else 32.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Gray,
                        disabledTextColor = Color.Transparent,
                        errorTextColor = Color.Red,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Gray,
                        errorContainerColor = Color.Red.copy(alpha = 0.2f),
                        cursorColor = DarkBlueLine,
                        errorCursorColor = Color.Red,
                        focusedIndicatorColor = DarkBlueLine,
                        unfocusedIndicatorColor = DarkBlueLine,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Red,
                        errorLabelColor = Color.Red
                    )
                )
                if (emailError) {
                    Text(
                        text = "Email inválido",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 24.dp)
                    )
                }

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    isError = passwordError,
                    label = { Text("Password", modifier = Modifier.padding(vertical = 8.dp)) },
                    placeholder = { Text("Digite sua senha") },
                    modifier = Modifier.padding(bottom = if (passwordError) 8.dp else 0.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Gray,
                        disabledTextColor = Color.LightGray,
                        errorTextColor = Color.Red,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Gray,
                        errorContainerColor = Color.Red.copy(alpha = 0.2f),
                        cursorColor = DarkBlueLine,
                        errorCursorColor = Color.Red,
                        focusedIndicatorColor = DarkBlueLine,
                        unfocusedIndicatorColor = DarkBlueLine,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Red
                    )
                )
                if (passwordError) {
                    Text(
                        text = "Senha não pode ser vazia",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 24.dp)
                    )
                }
            }

            Button(
                onClick = {
                    emailError = !loginViewModel.isValidEmail(email)
                    passwordError = password.isBlank()

                    if (!emailError && !passwordError) {
                        loginViewModel.login(LoginRequest(email, password)) {
                            println(it)
                            TokenManager(context).apply {
                                saveToken(it.accessToken)
                                saveUser(it.user)
                            }
                            Log.d("LoginScreen", "Login realizado com sucesso. Redirecionando para chat.")
                            navigation("chat")
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 43.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Login")
            }

            Text(
                "Or continue with",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = TextColorGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(screenWidth * 0.37f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ButtonColorGray)
                ) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.flat_color_icons_google),
                                contentDescription = "Login com Google",
                                modifier = Modifier.padding(end = 8.dp),
                                tint = Color.Unspecified
                            )
                            Text("Google", color = Color(0xFF475569))
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .width(screenWidth * 0.37f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ButtonColorGray)
                ) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.flat_color_icons_facebook),
                                contentDescription = "Login com Facebook",
                                modifier = Modifier.padding(end = 8.dp),
                                tint = Color.Unspecified
                            )
                            Text("Facebook", color = Color(0xFF475569))
                        }
                    }
                }
            }

            IconButton(onClick = {}, modifier = Modifier.width(screenWidth * 0.80f)) {
                Row {
                    Text("Don’t have account? ", fontSize = 14.sp, color = TextColorGrayLight)
                    Text("Create now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MeetTalkTheme {
        val context = LocalContext.current
        val loginViewModel = LoginViewModel(context)
        Login(loginViewModel, {})
    }
}