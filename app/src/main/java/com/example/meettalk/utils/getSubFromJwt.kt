package com.example.meettalk.utils

import com.auth0.android.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException

/**
 * Decodifica um token JWT e retorna o valor do campo "sub" (subject).
 *
 * @param jwtToken O token JWT como uma String.
 * @return O valor do campo "sub" como String, ou null se o token for inválido
 * ou se o campo "sub" não estiver presente.
 */
fun getSubFromJwt(jwtToken: String): String? {
    return try {
        val jwt = com.auth0.jwt.JWT.decode(jwtToken.replace("Bearer ", ""))
        jwt.getClaim("sub").asString()
    } catch (e: JWTDecodeException) {
        println("Erro ao decodificar o JWT: ${e.message}")
        null
    } catch (e: Exception) {
        println("Ocorreu um erro inesperado: ${e.message}")
        null
    }
}