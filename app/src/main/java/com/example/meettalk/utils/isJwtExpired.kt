package com.example.meettalk.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Base64
import org.json.JSONObject
import java.util.Date // Para trabalhar com datas

/**
 * Verifica se um token JWT expirou.
 * @param token O token JWT a ser verificado.
 * @return true se o token expirou, false caso contrário (incluindo tokens inválidos ou sem 'exp').
 */
@RequiresApi(Build.VERSION_CODES.O)
fun isJwtExpired(token: String): Boolean {
    try {
        val parts = token.split(".")
        if (parts.size != 3) {
            println("Token JWT inválido: número incorreto de partes.")
            return true // Considera expirado se for inválido
        }

        val payload = String(Base64.getUrlDecoder().decode(parts[1]))
        val jsonObject = JSONObject(payload)

        if (jsonObject.has("exp")) {
            val expirationTimestampSeconds = jsonObject.getLong("exp")
            val expirationDate = Date(expirationTimestampSeconds * 1000L) // Converte segundos para milissegundos

            val currentTime = Date() // Data e hora atual

            // Retorna true se a data de expiração for anterior à data/hora atual
            return expirationDate.before(currentTime)
        } else {
            println("Token JWT não possui a claim 'exp'.")
            return true
        }
    } catch (e: Exception) {
        println("Erro ao decodificar ou parsear o JWT para verificar expiração: ${e.message}")
        return true
    }
}
