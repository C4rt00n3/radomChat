package com.example.meettalk.utils.users

import android.util.Log
import io.realm.kotlin.Realm
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.entities.User
import io.realm.kotlin.ext.query

/**
 * Atualiza um usuário existente no Realm Database.
 *
 * Esta função busca um [UserRealm] pelo seu UUID e aplica as atualizações dos dados
 * fornecidos em um objeto [User]. As atualizações são realizadas dentro de uma
 * transação de escrita bloqueante do Realm.
 *
 * @param realm A instância do Realm para realizar a operação.
 * @param uuid O UUID do usuário a ser atualizado.
 * @param userData O objeto [User] contendo os dados a serem atualizados.
 * Apenas campos não nulos de [userData] serão aplicados.
 * @return O objeto [User] atualizado (convertido de [UserRealm]) se encontrado e atualizado com sucesso,
 * ou `null` se o usuário não for encontrado no Realm ou ocorrer um erro.
 */
fun updateUserInRealm(realm: Realm, uuid: String, userData: User): User? {
    try {
        return realm.writeBlocking {
            val userRealm: UserRealm? = query<UserRealm>("uuid == $0", uuid).find().firstOrNull()

            if (userRealm == null) {
                Log.e("UserUpdateUtil", "Usuário não encontrado no Realm para UUID: $uuid")
                return@writeBlocking null // Retorna null se não encontrar o usuário
            }

            userRealm?.apply {
                userData.name?.let { name = it }
                userData.gender?.name?.let { gender = it }
                userData.birthDate?.let { birthDate = it }
                userData.updateAt?.let { updateAt = it }

                userData.preference?.let { preference = it.toRealm() }
                userData.location?.let { location = it.toRealm() }

            }

            // <--- A CORREÇÃO ESTÁ AQUI --->
            // Converta o objeto gerenciado (userRealm) para sua versão não gerenciada (User)
            // ANTES de sair da transação de escrita.
            return@writeBlocking userRealm.toClass()
        }
    } catch (error: Exception) {
        Log.e("UserUpdateUtil", "Erro ao atualizar usuário no Realm: ${error.message}", error)
        return null
    }
}