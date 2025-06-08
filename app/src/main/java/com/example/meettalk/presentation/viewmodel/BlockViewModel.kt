package com.example.meettalk.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meettalk.R
import com.example.meettalk.data.local.model.entities.Block
import com.example.meettalk.data.remote.BlockEndPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BlockViewModel(context: Context): ViewModel() {
    private val url = context.getText(R.string.baseUrl).toString()

    private val retrofit =
        Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build()

    private val apiServiceBlock = retrofit.create(BlockEndPoint::class.java)

    private val _blocks = MutableStateFlow<List<Block>>(listOf())
    val blocks: StateFlow<List<Block>> = _blocks

    private val _blocksBackup = MutableStateFlow<List<Block>>(listOf())
    val blocksBackup: StateFlow<List<Block>> = _blocks

    fun createBlock(userId: String, token: String) {
        viewModelScope.launch {
            try {
                val response = apiServiceBlock.create(token, userId)
                val block = response.body()

                if (response.isSuccessful && block != null) {
                    _blocks.value = blocks.value + block
                } else {
                    println(response.errorBody()?.string())
                    Log.d("Error", "Error na resposta: ${response.code()}")
                }

            } catch (error: Exception) {
                Log.d("Error", error.message.toString())
            }
        }
    }

    fun deleteBlock(userId: String, token: String) {
        viewModelScope.launch {
            try {
                val response = apiServiceBlock.delete(token, userId)
                val block = _blocks.value.find { it.blockedUserId == userId }

                if (response.isSuccessful && block != null) _blocks.value = blocks.value - block
                else Log.d("Error", "Error na resposta: ${response.code()}")
            } catch (error: Exception) {
                println(error)
                Log.d("Error", error.message.toString())
            }
        }
    }

    fun findAllBlock(token: String) {
        viewModelScope.launch {
            try {
                val response: Response<List<Block>> = apiServiceBlock.findMany(token)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _blocks.value = it
                        _blocksBackup.value = it
                    }
                } else {
                    _blocks.value = listOf()
                }
            } catch (e: Exception) {
                _blocks.value = listOf()
                Log.e("Login", "Erro: ${e.message}")
            }
        }
    }
}