package com.example.lab08_meneses
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch
import com.example.lab08_meneses.ui.theme.Lab08MenesesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08MenesesTheme(darkTheme = isSystemInDarkTheme()) { // Detecta el tema del sistema
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).build()

                val taskDao = db.taskDao()
                val viewModel = TaskViewModel(taskDao)

                TaskScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var newTaskDescription by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var editingTask: Task? by remember { mutableStateOf(null) }
    var editedDescription by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aplicativo de Tareas") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    viewModel.searchTasks(query)
                },
                label = { Text("Buscar tareas") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de filtro de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = { viewModel.filterTasksByStatus(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Todas")
                }
                Button(
                    onClick = { viewModel.filterTasksByStatus(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) // Naranja
                ) {
                    Text("Pendientes")
                }
                Button(
                    onClick = { viewModel.filterTasksByStatus(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Verde
                ) {
                    Text("Completadas")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = newTaskDescription,
                onValueChange = { newTaskDescription = it },
                label = { Text("Nueva tarea") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (newTaskDescription.isNotEmpty()) {
                        viewModel.addTask(newTaskDescription)
                        newTaskDescription = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Agregar tarea")
            }

            Spacer(modifier = Modifier.height(16.dp))

            tasks.forEach { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (editingTask == task) {
                        TextField(
                            value = editedDescription,
                            onValueChange = { editedDescription = it },
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                viewModel.updateTaskDescription(task, editedDescription)
                                editingTask = null
                                editedDescription = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Guardar")
                        }
                        Button(
                            onClick = { editingTask = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Cancelar")
                        }
                    } else {
                        Text(text = task.description, modifier = Modifier.weight(1f))
                        Button(
                            onClick = { viewModel.toggleTaskCompletion(task) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (task.isCompleted) Color(0xFF4CAF50) else Color(0xFFFF9800))
                        ) {
                            Text(if (task.isCompleted) "Completada" else "Pendiente")
                        }
                        Button(
                            onClick = {
                                editingTask = task
                                editedDescription = task.description
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Editar")
                        }
                    }
                }
            }

            Button(
                onClick = { coroutineScope.launch { viewModel.deleteAllTasks() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Eliminar todas las tareas")
            }
        }
    }
}
