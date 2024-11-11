package com.example.lab08_meneses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class TaskViewModel(private val dao: TaskDao) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _filteredTasks = MutableStateFlow<List<Task>>(emptyList())
    val filteredTasks: StateFlow<List<Task>> = _filteredTasks

    init {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks()
            _filteredTasks.value = _tasks.value
        }
    }

    fun addTask(description: String) {
        val newTask = Task(description = description)
        viewModelScope.launch {
            dao.insertTask(newTask)
            _tasks.value = dao.getAllTasks()
            _filteredTasks.value = _tasks.value
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            dao.updateTask(updatedTask)
            _tasks.value = dao.getAllTasks()
            _filteredTasks.value = _tasks.value
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch {
            dao.deleteAllTasks()
            _tasks.value = emptyList()
            _filteredTasks.value = emptyList()
        }
    }

    fun searchTasks(query: String) {
        _filteredTasks.value = _tasks.value.filter { task ->
            task.description.contains(query, ignoreCase = true)
        }
    }

    fun updateTaskDescription(task: Task, newDescription: String) {
        viewModelScope.launch {
            val updatedTask = task.copy(description = newDescription)
            dao.updateTask(updatedTask)
            _tasks.value = dao.getAllTasks()
            _filteredTasks.value = _tasks.value
        }
    }

    // Nueva función para ordenar las tareas por estado
    fun filterTasksByStatus(showCompleted: Boolean?) {
        _filteredTasks.value = when (showCompleted) {
            true -> _tasks.value.filter { it.isCompleted }
            false -> _tasks.value.filter { !it.isCompleted }
            else -> _tasks.value // Muestra todas si `showCompleted` es nulo
        }
    }
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
            _tasks.value = dao.getAllTasks()
            _filteredTasks.value = _tasks.value
        }
    }

}


