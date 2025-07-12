package com.example.meettalk.utils

import com.example.meettalk.data.local.model.body.enums.State
import java.util.Locale


/**
 * Tenta converter uma string de nome de estado (ex: "Bahia") para o seu enum State (ex: State.BA).
 * Esta função tenta ser robusta, verificando tanto o nome completo quanto a sigla se fornecida.
 *
 * @param stateName A string do nome do estado (ex: "Bahia", "BA").
 * @return O enum State correspondente, ou null se não for encontrado.
 */
fun mapStringToStateEnum(stateName: String?): State? {
    if (stateName.isNullOrBlank()) return null

    try {
        return State.valueOf(stateName.uppercase(Locale.getDefault()))
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    }

    return when (stateName.uppercase(Locale.getDefault())) {
        "ACRE" -> State.AC
        "ALAGOAS" -> State.AL
        "AMAPÁ" -> State.AP
        "AMAZONAS" -> State.AM
        "BAHIA" -> State.BA
        "CEARÁ" -> State.CE
        "DISTRITO FEDERAL" -> State.DF
        "ESPÍRITO SANTO" -> State.ES
        "GOIÁS" -> State.GO
        "MARANHÃO" -> State.MA
        "MINAS GERAIS" -> State.MG
        "MATO GROSSO DO SUL" -> State.MS
        "MATO GROSSO" -> State.MT
        "PARÁ" -> State.PA
        "PARAÍBA" -> State.PB
        "PERNAMBUCO" -> State.PE
        "PIAUÍ" -> State.PI
        "PARANÁ" -> State.PR
        "RIO DE JANEIRO" -> State.RJ
        "RIO GRANDE DO NORTE" -> State.RN
        "RONDÔNIA" -> State.RO
        "RORAIMA" -> State.RR
        "RIO GRANDE DO SUL" -> State.RS
        "SANTA CATARINA" -> State.SC
        "SERGIPE" -> State.SE
        "SÃO PAULO" -> State.SP
        "TOCANTINS" -> State.TO
        else -> null // Se não encontrar correspondência
    }
}

