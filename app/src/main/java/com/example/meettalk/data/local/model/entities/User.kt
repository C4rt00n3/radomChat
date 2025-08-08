package com.example.meettalk.data.local.model.entities

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.body.enums.Gender
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.Period
import io.realm.kotlin.ext.realmListOf
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class User(
    val uuid: String,
    val name: String,
    val birthDate: String,
    val preference: Preference,
    val gender: String? = Gender.M.name,
    val chatParticipants: List<ChatParticipant> = emptyList(),
    val profileImages: List<ImageProfile> = emptyList(),
    val privacyUser: PrivacyUser? = null,
    val location: Location? = null,
    val updateAt: String? = null,
    var createAt: String? = null,
) : Iterable<Any> {
    val age: Int
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            return try {
                val dobInstant = Instant.parse(birthDate)

                val dobLocalDate = dobInstant.atZone(ZoneId.systemDefault()).toLocalDate()

                val currentDate = LocalDate.now()

                Period.between(dobLocalDate, currentDate).years
            } catch (e: Exception) {
                println("Erro ao calcular idade para '${name}' (birthDate: ${birthDate}): ${e.message}")
                0
            }
        }

    /**
     * Calcula a data e hora de nascimento aproximada, no dia 1 do mês,
     * com base na idade fornecida e na data atual.
     * O horário é padronizado para 02:00:00.000 e o fuso horário para UTC (Z).
     *
     * @param idade A idade em anos da pessoa.
     * @return Uma [String] formatada no padrão ISO 8601 (ex: "AAAA-MM-01T02:00:00.000Z"),
     * ou null se a idade for inválida (negativa).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun calcularDataNascimentoFormatada(idade: Int): String? {
        if (idade < 0) {
            println("A idade não pode ser negativa.")
            return null
        }

        val dateNow = LocalDate.now()
        val anoNascimento = dateNow.year - idade

        val dataNascimento = LocalDate.of(anoNascimento, dateNow.month, 1)

        val horaEspecifica = LocalDateTime.of(dataNascimento, java.time.LocalTime.of(2, 0, 0))

        val dataHoraComOffset = OffsetDateTime.of(horaEspecifica, ZoneOffset.UTC)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        return dataHoraComOffset.format(formatter)
    }

    private fun toUserRealm(user: User?): UserRealm? {
        if (user == null) return null
        return UserRealm().apply {
            uuid = user.uuid
            name = user.name
            gender = user.gender ?: Gender.M.name
            updateAt = user.updateAt
            createAt = user.createAt
            birthDate = user.birthDate // Mantenha a string original para o Realm
            preference = user.preference?.toRealm()
            location = user.location?.toRealm()
            privacyUser = user.privacyUser?.toRealm()
            profileImages = realmListOf(
                *user.profileImages.orEmpty()
                    .mapNotNull { it.toRealm() }
                    .toTypedArray()
            )
        }
    }

    fun toRealm() = toUserRealm(this)
    override fun iterator(): Iterator<Any> {
        TODO("Not yet implemented")
    }
}