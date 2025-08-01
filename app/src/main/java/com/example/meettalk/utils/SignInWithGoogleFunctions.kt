package com.example.meettalk.utils

/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licenciado sob a Licença Apache, Versão 2.0 (a "Licença");
 * você não pode usar este arquivo, exceto em conformidade com a Licença.
 * Você pode obter uma cópia da Licença em
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * A menos que exigido pela lei aplicável ou acordado por escrito, software
 * distribuído sob a Licença é distribuído "COMO ESTÁ", SEM GARANTIAS OU CONDIÇÕES DE QUALQUER TIPO,
 * expressas ou implícitas.
 * Consulte a Licença para obter a linguagem específica que rege as permissões
 * e limitações sob a Licença.
 */
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException // Importação adicionada para NoCredentialException
import com.example.meettalk.data.local.model.entities.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.coroutineScope
import java.security.SecureRandom // Importação adicionada para SecureRandom
import java.util.Base64 // Importação adicionada para Base64
import java.util.UUID

// Constante para o ID do cliente da web. Este valor foi preenchido com o ID real do seu projeto Google Cloud.
const val WEB_CLIENT_ID = "780186774390-6duduboeodq47o8tc5bfi4bhqudobqhf.apps.googleusercontent.com"
    // Android "780186774390-4ah8nnr4r07t1eerj7nbigpb5dodmu6f.apps.googleusercontent.com"

/**
 * Classe responsável por encapsular as funções de login com o Google usando o Credential Manager.
 *
 * @param context O contexto da aplicação ou atividade, necessário para inicializar o CredentialManager.
 */
class SignInWithGoogleFunctions(context: Context) {
    // Inicializa o CredentialManager, a API principal para gerenciar credenciais.
    private val credentialManager = CredentialManager.create(context)

    // Armazena o contexto da atividade para uso em chamadas que o exigem.
    private val activityContext = context

    // TAG para logs, preenchida para facilitar a depuração.
    val TAG = "SignInWithGoogleFunctions"

    private val taskManager =
        TaskManager() // A classe TaskManager não foi fornecida, assumindo sua existência.

    /**
     * Gera uma string nonce segura.
     * Em um ambiente de produção, esta nonce deve ser gerada no servidor e associada à sessão do usuário.
     * Para fins de demonstração, uma nonce aleatória é gerada aqui.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateNonce(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32) // 32 bytes para uma nonce de 256 bits
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /**
     * Cria uma opção para obter um token de ID do Google.
     *
     * @param nonce Uma string nonce (número usado uma única vez) para segurança ao gerar o token.
     * @param filterByAuthorizedAccounts Booleano para filtrar apenas por contas já autorizadas pelo aplicativo.
     * @return Uma instância de GetGoogleIdOption configurada.
     */
    fun createGoogleIdOption(
        nonce: String,
        filterByAuthorizedAccounts: Boolean
    ): GetGoogleIdOption {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .setNonce(nonce)
            .build()
        return googleIdOption
    }

    /**
     * Inicia o fluxo de login do usuário, solicitando uma credencial ao CredentialManager.
     * Esta função é suspensa porque `getCredential` é uma chamada assíncrona.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun signInUser(login: (String) -> Unit) {
        taskManager.addTask {
            coroutineScope {
                val initialNonce = generateNonce() // Gera uma nonce para a primeira tentativa

                val authorizedAccountsOption = createGoogleIdOption(initialNonce, true)
                var request = GetCredentialRequest.Builder()
                    .addCredentialOption(authorizedAccountsOption)
                    .build()

                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = activityContext,
                    )
                    handleSignIn(result, login)
                } catch (e: GetCredentialException) {
                    if (e is NoCredentialException) {
                        Log.w(
                            TAG,
                            "Nenhuma credencial autorizada encontrada. Tentando com todas as contas."
                        )
                        // Se não houver credenciais autorizadas, tenta com todas as contas Google
                        val allAccountsNonce =
                            generateNonce() // Gera uma nova nonce para a segunda tentativa
                        val allAccountsOption = createGoogleIdOption(allAccountsNonce, false)
                        request = GetCredentialRequest.Builder()
                            .addCredentialOption(allAccountsOption)
                            .build()
                        try {
                            val result = credentialManager.getCredential(
                                request = request,
                                context = activityContext,
                            )
                            handleSignIn(result, login)
                        } catch (e2: GetCredentialException) {
                            Log.e(
                                TAG,
                                "Falha ao obter credencial após segunda tentativa: ${e2.errorMessage}",
                                e2
                            )
                        }
                    } else {
                        // Lidar com outras falhas na obtenção da credencial (por exemplo, usuário cancelou).
                        Log.e(TAG, "Falha ao obter credencial: ${e.errorMessage}", e)
                    }
                }
            }
        }
    }

    /**
     * Lida com a resposta de uma tentativa de login bem-sucedida, processando o tipo de credencial retornada.
     *
     * @param result A resposta contendo a credencial obtida.
     */
    fun handleSignIn(result: GetCredentialResponse, login: (String) -> Unit) {
        // Lida com a credencial retornada com sucesso.
        val credential = result.credential
        val responseJson: String

        // Usa uma expressão 'when' para determinar o tipo de credencial e processá-la.
        when (credential) {

            // Credencial de chave de acesso (Passkey)
            is PublicKeyCredential -> {
                // Compartilhe o responseJson, como um GetCredentialResponse, com seu servidor para validar e autenticar.
                responseJson = credential.authenticationResponseJson
                Log.d(TAG, "Credencial de Chave de Acesso recebida. JSON: $responseJson")
            }

            // Credencial de senha
            is PasswordCredential -> {
                // Envie o ID e a senha para seu servidor para validar e autenticar.
                val username = credential.id
                val password = credential.password

                println(username)
                println(password)
                Log.d(TAG, "Credencial de Senha recebida. Usuário: $username")
                // Implemente a lógica de envio para o servidor aqui.
            }

            // Credencial personalizada, que pode ser um GoogleIdTokenCredential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential e extraia o ID para validar e
                        // autenticar em seu servidor.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        // Você pode usar os membros de googleIdTokenCredential diretamente para fins de UX
                        // (interface do usuário), mas não os use para armazenar ou controlar o acesso aos dados do usuário.
                        // Para isso, você precisa primeiro validar o token:
                        // passe googleIdTokenCredential.getIdToken() para o servidor de backend.
                        // veja [instruções de validação](https://developers.google.com/identity/gsi/web/guides/verify-google-id-token)
                        login(googleIdTokenCredential.idToken)

                        // Implemente a lógica de envio do token para o servidor aqui.
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Recebeu uma resposta de token de ID do Google inválida", e)
                    }
                } else {
                    // Capture qualquer tipo de credencial personalizada não reconhecido aqui.
                    Log.e(TAG, "Tipo de credencial personalizada inesperado: ${credential.type}")
                }
            }

            else -> {
                // Capture qualquer tipo de credencial não reconhecido aqui.
                Log.e(TAG, "Tipo de credencial inesperado: ${credential.type}")
            }
        }
    }

    /**
     * Cria uma opção para o fluxo "Entrar com o Google".
     *
     * @param nonce Uma string nonce para segurança.
     * @return Uma instância de GetSignInWithGoogleOption configurada.
     */
    fun createGoogleSignInWithGoogleOption(nonce: String): GetSignInWithGoogleOption {
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = WEB_CLIENT_ID // Define o ID do cliente da web do seu servidor.
        ).setNonce(nonce) // Define a string nonce para segurança.
            .build()
        return signInWithGoogleOption
    }

    /**
     * Lida com a resposta quando a opção "Entrar com o Google" é utilizada.
     *
     * @param result A resposta contendo a credencial obtida.
     */
    fun handleSignInWithGoogleOption(result: GetCredentialResponse) {
        // Lida com a credencial retornada com sucesso.
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential e extraia o ID para validar e
                        // autenticar em seu servidor.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        Log.d(
                            TAG,
                            "Google ID Token (SIWG Option) recebido. ID: ${googleIdTokenCredential.id}"
                        )
                        // Implemente a lógica de envio do token para o servidor aqui.
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Recebeu uma resposta de token de ID do Google inválida", e)
                    }
                } else {
                    // Capture qualquer tipo de credencial não reconhecido aqui.
                    Log.e(TAG, "Tipo de credencial inesperado: ${credential.type}")
                }
            }

            else -> {
                // Capture qualquer tipo de credencial não reconhecido aqui.
                Log.e(TAG, "Tipo de credencial inesperado: ${credential.type}")
            }
        }
    }
}