package com.example.studycollab.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studycollab.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    viewModel: StudyGroupViewModel,
    navController: NavController
) {
    // TRIGGER: טעינת נתונים ראשונית במידה וחסרים
    LaunchedEffect(groupId) {
        viewModel.loadInitialData()
    }

    // מציאת הקבוצה הנוכחית מתוך הרשימה ב-ViewModel
    val group = viewModel.myGroups.find { it._id == groupId }
    val currentUserId = UserSession.UserSession.userId

    // לוגיקה לבדיקה האם המשתמש הנוכחי הוא Admin (לצורך כפתור המחיקה)
    val isAdmin = group?.members?.find { member ->
        val id = if (member.userId.isJsonPrimitive) {
            member.userId.asString
        } else {
            member.userId.asJsonObject.get("_id").asString
        }
        id == currentUserId
    }?.role == "admin"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Loading Group...") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (group == null) {
            // מצב טעינה (אם הקבוצה לא נמצאה עדיין)
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // תוכן המסך
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // כפתור למשתתפים
                DashboardButton("👥 View Participants", Icons.Default.Person) {
                    navController.navigate("participants/$groupId")
                }

                // כפתור למשימות
                DashboardButton("✅ Group Tasks", Icons.Default.CheckCircle) {
                    navController.navigate("group_tasks/$groupId")
                }

                // --- התיקון החשוב: Group Chat ---
                DashboardButton("💬 Group Chat", Icons.AutoMirrored.Filled.Send) {
                    // טריק הניווט הכפול:
                    // 1. אנחנו מכניסים את רשימת הצ'אטים להיסטוריה
                    // (וודא שהשם "chats" זהה ל-Screen.Chats.route שלך ב-AppNavigation)
                    navController.navigate("chats")

                    // 2. ומיד נכנסים לצ'אט הספציפי
                    navController.navigate("chat/${group._id}")
                }

                // כפתור לצ'אט עם מרצה (Placeholder)
                DashboardButton("👨‍🏫 Chat with Lecturer", Icons.Default.Face) {
                    // Logic for lecturer chat implementation
                }

                // כפתור הגשה (רק אם מטרת הקבוצה היא הגשה)
                if (group.purpose == "assignment_submission") {
                    Button(
                        onClick = { /* Submit logic */ },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📤 Submit Assignment")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // כפתור מחיקת קבוצה (רק למנהל)
                if (isAdmin) {
                    var showDeleteDialog by remember { mutableStateOf(false) }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Group") },
                            text = { Text("Are you sure? This action cannot be undone.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteGroup(groupId) { success ->
                                        if (success) navController.popBackStack()
                                    }
                                    showDeleteDialog = false
                                }) {
                                    Text("Delete", color = Color.Red)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Group")
                    }
                }
            }
        }
    }
}

// רכיב כפתור מעוצב לשימוש חוזר במסך
@Composable
fun DashboardButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}