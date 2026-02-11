package com.example.studycollab.ui.courses

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsManagementScreen(navController: NavController, courseName: String) {
    var showAddDialog by remember { mutableStateOf(false) }
    // Mock data - in a real app, this comes from your ViewModel
    val materials = remember { mutableStateListOf("Week 1 - Introduction.pdf", "Lecture 2 Notes.docx") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Materials: $courseName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Material")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(materials) { material ->
                MaterialItemCard(
                    name = material,
                    onDelete = { materials.remove(material) } // logic to call API for deletion
                )
            }
        }

        if (showAddDialog) {
            // Reusing the dialog logic we created for assignments
            CreateAssignmentDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, uri ->
                    if (uri != null) {
                        materials.add("$title.pdf") // Mocking the addition
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun MaterialItemCard(name: String, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.FilePresent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}