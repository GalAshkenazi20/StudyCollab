package com.example.studycollab.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: StudyGroupViewModel = viewModel() // חיבור ל-ViewModel הקיים
) {
    // טעינת הנתונים (הקבוצות) ברגע שהמסך נפתח
    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    val groups = viewModel.myGroups

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Chats") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        if (groups.isEmpty()) {
            // מצב ריק: אם אין קבוצות
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active chats found.\nJoin a study group first!",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // רשימת הצ'אטים
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(groups) { group ->
                    ChatItem(
                        groupName = group.name,
                        courseName = group.course, // נניח שיש שדה כזה, או שתמחק אם אין
                        onClick = {
                            // הניווט הקריטי: מעביר אותנו למסך הצ'אט עם ה-ID של הקבוצה
                            navController.navigate("chat/${group._id}")
                        }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    groupName: String,
    courseName: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // הופך את כל השורה ללחיצה
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // אייקון צד שמאל
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // טקסטים (שם הקבוצה והקורס)
        Column {
            Text(
                text = groupName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!courseName.isNullOrEmpty()) {
                Text(
                    text = courseName,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "Tap to open chat",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}