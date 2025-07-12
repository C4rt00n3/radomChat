package com.example.meettalk.utils

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedDeque

class TaskManager {

    private val taskStack = ConcurrentLinkedDeque<suspend () -> Unit>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isRunning = false

    fun addTask(task: suspend () -> Unit) {
        taskStack.push(task)
        runTasks()
    }

    private fun runTasks() {
        if (isRunning) return

        isRunning = true
        scope.launch {
            while (taskStack.isNotEmpty()) {
                val task = taskStack.pop()
                try {
                    task() // executa tarefa
                } catch (e: Exception) {
                    println("Erro na tarefa: ${e.message}")
                }
            }
            isRunning = false
        }
    }
}
