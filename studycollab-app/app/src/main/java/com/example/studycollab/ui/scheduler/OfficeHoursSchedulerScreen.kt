package com.example.studycollab.ui.scheduler

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeHoursSchedulerScreen(
    navController: NavController,
    viewModel: OfficeHoursViewModel = viewModel()
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchMySlots()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Office Hours Scheduler") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Slot")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Manage your weekly availability slots.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.slots.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No office hours set. Tap + to add one.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(viewModel.slots) { slot ->
                        LecturerSlotCard(
                            slot = slot,
                            onDelete = { viewModel.deleteSlot(slot.id) }
                        )
                    }
                }
            }
        }

        // Create Slot Dialog
        if (showCreateDialog) {
            CreateSlotDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { day, start, end ->
                    viewModel.createSlot(day, start, end)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun LecturerSlotCard(slot: SlotInfo, onDelete: () -> Unit) {
    val bookedName = slot.bookedBy?.profile?.fullName
    val statusText = if (slot.isBooked && bookedName != null) {
        "Booked by $bookedName"
    } else if (slot.isBooked) {
        "Booked"
    } else {
        "Available"
    }
    val statusColor = if (slot.isBooked) Color(0xFFE53935) else Color(0xFF4CAF50)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${slot.day}: ${slot.start} - ${slot.end}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSlotDialog(
    onDismiss: () -> Unit,
    onConfirm: (day: String, startTime: String, endTime: String) -> Unit
) {
    val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    var selectedDay by remember { mutableStateOf(days[0]) }
    var dayExpanded by remember { mutableStateOf(false) }

    val hours = (8..19).map { String.format("%02d:00", it) }
    var selectedStart by remember { mutableStateOf("10:00") }
    var startExpanded by remember { mutableStateOf(false) }
    var selectedEnd by remember { mutableStateOf("11:00") }
    var endExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Office Hour Slot") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Day Picker
                Text("Day", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = dayExpanded,
                    onExpandedChange = { dayExpanded = !dayExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDay,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false }
                    ) {
                        days.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day) },
                                onClick = {
                                    selectedDay = day
                                    dayExpanded = false
                                }
                            )
                        }
                    }
                }

                // Start Time
                Text("Start Time", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = startExpanded,
                    onExpandedChange = { startExpanded = !startExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedStart,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = startExpanded,
                        onDismissRequest = { startExpanded = false }
                    ) {
                        hours.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(time) },
                                onClick = {
                                    selectedStart = time
                                    // Auto-set end to 1 hour later
                                    val startHour = time.substringBefore(":").toInt()
                                    if (startHour < 20) {
                                        selectedEnd = String.format("%02d:00", startHour + 1)
                                    }
                                    startExpanded = false
                                }
                            )
                        }
                    }
                }

                // End Time
                Text("End Time", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = endExpanded,
                    onExpandedChange = { endExpanded = !endExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedEnd,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = endExpanded,
                        onDismissRequest = { endExpanded = false }
                    ) {
                        hours.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(time) },
                                onClick = {
                                    selectedEnd = time
                                    endExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDay, selectedStart, selectedEnd) },
                enabled = selectedStart < selectedEnd
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}