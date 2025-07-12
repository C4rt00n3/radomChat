package com.example.meettalk.utils

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import java.io.IOException
import java.util.Locale


/**
 * Realiza a geocodificação reversa para obter informações de endereço (cidade, estado, país) a partir de coordenadas geográficas.
 *
 * @param context O contexto do aplicativo.
 * @param latitude A latitude da localização.
 * @param longitude A longitude da localização.
 * @param onAddressFound Callback invocado quando o endereço é encontrado com sucesso.
 * Fornece uma tupla com Cidade, Estado e País (todos String? para permitir nulos).
 * @param onError Callback invocado se ocorrer um erro (ex: IO Exception, serviço não disponível).
 * Fornece a Exception que ocorreu.
 */
fun getAddressFromLocation(
    context: Context,
    latitude: Double,
    longitude: Double,
    onAddressFound: (city: String?, state: String?, country: String?) -> Unit,
    onError: (Exception) -> Unit
) {
    if (!Geocoder.isPresent()) {
        onError(IllegalStateException("Geocoder não está disponível no dispositivo."))
        return
    }

    val geocoder = Geocoder(context, Locale.getDefault())

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    onAddressFound(address.locality, address.adminArea, address.countryName)
                    Log.d("Geocoder", "Endereço encontrado: Cidade=${address.locality}, Estado=${address.adminArea}, País=${address.countryName}")
                } else {
                    onAddressFound(null, null, null)
                    Log.w("Geocoder", "Nenhum endereço encontrado para Lat: $latitude, Lng: $longitude")
                }
            }
        } else {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                onAddressFound(address.locality, address.adminArea, address.countryName)
                Log.d("Geocoder", "Endereço encontrado: Cidade=${address.locality}, Estado=${address.adminArea}, País=${address.countryName}")
            } else {
                onAddressFound(null, null, null)
                Log.w("Geocoder", "Nenhum endereço encontrado para Lat: $latitude, Lng: $longitude")
            }
        }
    } catch (e: IOException) {
        onError(e)
        Log.e("Geocoder", "Erro de E/S ao obter endereço: ${e.message}", e)
    } catch (e: IllegalArgumentException) {
        onError(e)
        Log.e("Geocoder", "Argumentos de localização inválidos: ${e.message}", e)
    }
}