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
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch
import com.example.lab08_meneses.ui.theme.Lab08MenesesTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit

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
// Colores
val LightBackground = Color(0xFFF7F7F7) // Gris muy claro para fondo en tema claro
val DarkBackground = Color(0xFF121212) // Gris oscuro para fondo en tema oscuro
val LightAppBarColor = Color(0xFFE0E0E0) // Gris claro para barra en tema claro
val DarkAppBarColor = Color(0xFF1F1F1F) // Gris más oscuro para barra en tema oscuro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var newTaskDescription by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var editingTask: Task? by remember { mutableStateOf(null) }
    var editedDescription by remember { mutableStateOf("") }
    var taskToDelete: Task? by remember { mutableStateOf(null) }

    //detección de modo oscuro
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) DarkBackground else LightBackground
    val appBarColor = if (isDarkTheme) DarkAppBarColor else LightAppBarColor

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aplicativo de Tareas") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = appBarColor
                )
            )
        },
        containerColor = backgroundColor 
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Pendientes")
                }
                Button(
                    onClick = { viewModel.filterTasksByStatus(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editingTask == task) {
                        TextField(
                            value = editedDescription,
                            onValueChange = { editedDescription = it },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                viewModel.updateTaskDescription(task, editedDescription)
                                editingTask = null
                                editedDescription = ""
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Guardar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { editingTask = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Cancelar",
                                tint = Color.Gray
                            )
                        }
                    } else {
                        Text(text = task.description, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { viewModel.toggleTaskCompletion(task) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.Edit else Icons.Default.Edit,
                                contentDescription = if (task.isCompleted) "Completada" else "Pendiente",
                                tint = if (task.isCompleted) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                        IconButton(
                            onClick = {
                                editingTask = task
                                editedDescription = task.description
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(
                            onClick = { taskToDelete = task },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}
