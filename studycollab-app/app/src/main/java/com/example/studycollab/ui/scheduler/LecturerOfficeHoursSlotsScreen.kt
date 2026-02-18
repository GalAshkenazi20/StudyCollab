package com.example.studycollab.ui.scheduler

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EventAvailable
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
fun LecturerOfficeHoursSlotsScreen(
    navController: NavController,
    lecturerId: String,
    lecturerName: String,
    viewModel: OfficeHoursViewModel = viewModel()
) {
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(lecturerId) {
        viewModel.fetchAvailableSlots(lecturerId)
    }

    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            snackbarHostState.showSnackbar("Slot booked successfully!")
            showSuccessSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lecturerName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Available office hours for $lecturerName",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.slots.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No available office hours at the moment.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(viewModel.slots) { slot ->
                        BookingCard(
                            slot = slot,
                            onBook = {
                                viewModel.bookSlot(slot.id)
                                showSuccessSnackbar = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(slot: SlotInfo, onBook: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.EventAvailable,
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
                    text = "Available",
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Button(onClick = onBook) {
                Text("Book")
            }
        }
    }
}