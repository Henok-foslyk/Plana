package com.example.plana.ui.screen.tasks

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.plana.ui.screen.CalendarViewModel

import com.example.plana.data.TaskItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/*@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TasksScreen(viewModel: CalendarViewModel = viewModel()
) {
    var parsedDate = viewModel.selectedDate


    // Sample to-do items (could be fetched from a database or API)
    var todoList by remember {
        mutableStateOf(
            listOf(
                "Finish homework",
                "Read a book",
                "Work on project",
                "Go to the gym"
            ).map { TaskItem(it, false) }
        )
    }

    var newTaskDescription by remember { mutableStateOf("") }

    // Main Column Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF5F5F5)), // Light background
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Text
        if (parsedDate != null) {
            Text(
                text = "Tasks for ${parsedDate.month} ${parsedDate.dayOfMonth}, ${parsedDate.year}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF388E3C), // Dark Green
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        // Task List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            todoList.forEachIndexed { index, task ->
                TaskRow(
                    task = task,
                    onCheckedChange = { isChecked ->
                        todoList = todoList.mapIndexed { i, t ->
                            if (i == index) t.copy(isChecked = isChecked) else t
                        }
                    }
                )
            }
        }

        // Add New Task Section
        OutlinedTextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text("Add New Task") },
            placeholder = { Text("Enter task description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF66BB6A), // Lighter Green
                unfocusedBorderColor = Color(0xFFBDBDBD),
                cursorColor = Color(0xFF388E3C)
            )
        )

        // Add Button
        Button(
            onClick = {
                if (newTaskDescription.isNotBlank()) {
                    todoList = todoList + TaskItem(newTaskDescription, false)
                    newTaskDescription = "" // Clear input after adding
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF388E3C), // Dark Green
                contentColor = Color.White
            )
        ) {
            Text(text = "Add Task")
        }
    }
}

*/

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TasksScreen(
    viewModel: CalendarViewModel = viewModel()
) {
    Log.d("Tasks", "${viewModel.selectedDate}")
    Log.d("Tasks", "${viewModel.eventsMap}")
    val parsedDate: LocalDate = viewModel.selectedDate ?: return

    val eventsForDay = viewModel.eventsMap[parsedDate].orEmpty()

    val timeFmt = remember { DateTimeFormatter.ofPattern("h:mm a") }

    val tasks = remember(eventsForDay) {
        eventsForDay
            .sortedBy { it.startTime }
            .map { event ->
                TaskItem(
                    taskName = event.eventName.orEmpty(),
                    description = "${event.startTime.format(timeFmt)} – " +
                            event.endTime.format(timeFmt),
                    isChecked = false
                )
            }
            .toMutableStateList()          // Compose‑aware mutable list
    }

    val done       = tasks.count { it.isChecked }
    val remaining  = tasks.size - done

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tasks for ${parsedDate.month} ${parsedDate.dayOfMonth}, ${parsedDate.year}",
            style = MaterialTheme.typography.headlineMedium.copy(
                color       = Color(0xFF388E3C),
                fontWeight  = FontWeight.Bold,
                fontSize    = 24.sp
            ),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            itemsIndexed(tasks) { index, task ->
                TaskRow(
                    task = task,
                    onCheckedChange = { checked ->
                        tasks[index] = task.copy(isChecked = checked)
                    }
                )
            }
        }

        Text(
            text = "✅ $done done • $remaining remaining",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}


@Composable
fun TaskRow(
    task: TaskItem,
    onCheckedChange: (Boolean) -> Unit
) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)), // Light Green Card
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = task.isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF66BB6A), // Lighter Green
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
            Column(){
                Text(
                    text = task.taskName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(start = 8.dp),
                    color = if (task.isChecked) Color(0xFF757575) else Color(0xFF212121)
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(start = 8.dp),
                    color = if (task.isChecked) Color(0xFF757575) else Color(0xFF212121)
                )
            }
    }

}





