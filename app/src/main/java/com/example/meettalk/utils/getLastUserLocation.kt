package com.example.meettalk.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * Verifica se as permissões de localização (fina ou grossa) foram concedidas.
 * @param context O contexto do aplicativo.
 * @return true se alguma das permissões de localização for concedida, false caso contrário.
 */
fun areLocationPermissionsGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Recupera a última localização conhecida do usuário de forma assíncrona.
 * É importante garantir que as permissões de localização foram concedidas antes de chamar esta função.
 *
 * @param context O contexto do aplicativo, necessário para acessar os serviços de localização.
 * @param onGetLastLocationSuccess Callback invocado quando a localização é recuperada com sucesso.
 * Fornece um Pair representando latitude e longitude.
 * @param onGetLastLocationFailed Callback invocado quando ocorre um erro ao recuperar a localização.
 * Fornece a Exception que ocorreu.
 */
@SuppressLint("MissingPermission")
fun getLastUserLocation(
    context: Context,
    onGetLastLocationSuccess: (Pair<Double, Double>) -> Unit,
    onGetLastLocationFailed: (Exception) -> Unit
) {
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    if (areLocationPermissionsGranted(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    onGetLastLocationSuccess(Pair(it.latitude, it.longitude))
                } ?: run {
                    // Se location for nula (ex: nunca houve uma localização registrada ou GPS desligado),
                    // você pode querer tratar isso como uma falha ou um caso específico.
                    onGetLastLocationFailed(Exception("Última localização desconhecida ou nula."))
                }
            }
            .addOnFailureListener { exception: Exception ->
                // Se ocorrer um erro, invoca o callback de falha
                onGetLastLocationFailed(exception)
            }
    } else {
        onGetLastLocationFailed(SecurityException("Permissões de localização não concedidas."))
    }
}