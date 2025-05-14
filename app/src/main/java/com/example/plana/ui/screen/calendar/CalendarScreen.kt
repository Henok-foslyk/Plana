package com.example.plana.ui.screen.calendar


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.plana.navigation.TasksScreenRoute
import io.github.boguszpawlowski.composecalendar.SelectableWeekCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.rememberSelectableWeekCalendarState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.plana.data.EventItem
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontStyle
import com.example.plana.ui.screen.CalendarViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(navController: NavController,
                   viewModel: CalendarViewModel = viewModel(),
                   events: List<EventItem>?
){
    val calendarState = rememberSelectableWeekCalendarState()
    var notes by remember { mutableStateOf("") }
    viewModel.setEvents(events)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Weekly Calendar",
            color = Color(0xFF388E3C),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        SelectableWeekCalendar(
            calendarState = calendarState,
            dayContent = { day ->
                Column(
                    modifier = Modifier
                        .size(400.dp)
                        .padding(4.dp)
                        .background(Color(0xFFD8F3DC), CircleShape), // Rounded dark cards
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DefaultDayContent(dayState = day, selectionState = calendarState.selectionState)
                    val eventsForDay = viewModel.eventsMap[day.date].orEmpty()
                    eventsForDay
                        .sortedBy { it.startTime.toLocalTime() }
                        .take(3)                           // show at most 3, keeps cell tidy
                        .forEach { event ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(){
                                    Text(
                                        text = event.eventName ?: "" ,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = event.startTime
                                            .toLocalTime()
                                            .format(DateTimeFormatter.ofPattern("HH:mm")),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                    if (eventsForDay.size > 3) {
                        Text(
                            text = "+${eventsForDay.size - 3} more",
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        )

        SelectionControls(selectionState = calendarState.selectionState)

        Text(
            text = "Selected Dates: ${calendarState.selectionState.selection.joinToString(", ")}",
            style = MaterialTheme.typography.titleMedium,
        )

        if (calendarState.selectionState.selection.size == 1) {
            viewModel.selectedDate = calendarState.selectionState.selection[0]
            Button(
                onClick = {
                    navController.navigate(TasksScreenRoute)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "View Tasks for this Date")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Weekly Notes") },
            placeholder = { Text("Write your notes here...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .scrollable(rememberScrollState(), orientation = Orientation.Vertical),
            minLines = 5,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50), // Focused border color
                unfocusedBorderColor = Color(0xFFE0E0E0), // Unfocused border color
                cursorColor = Color(0xFF4CAF50)
            )
        )

    }
}

@Composable
private fun SelectionControls(
    selectionState: DynamicSelectionState,
) {
    Text(
        text = "Calendar Selection Mode",
        style = MaterialTheme.typography.titleMedium,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        SelectionMode.entries.drop(1).forEach { selectionMode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,

                modifier = Modifier.padding(end = 5.dp)
            ) {
                RadioButton(
                    selected = selectionState.selectionMode == selectionMode,
                    onClick = { selectionState.selectionMode = selectionMode }
                )
                Text(text = selectionMode.name, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DefaultDayContent(dayState: DayState<DynamicSelectionState>, selectionState: DynamicSelectionState) {
    val isSelected = selectionState.isDateSelected(dayState.date)
    val backgroundColor = if (isSelected) Color(0xFF4CAF50) else Color.Transparent
    val textColor = if (isSelected) Color.White else Color(0xFF212121)

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color = backgroundColor, shape = CircleShape)
            .clickable { dayState.selectionState.onDateSelected(dayState.date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayState.date.dayOfMonth.toString(),
            color = if (dayState.isCurrentDay) Color.Blue else textColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}