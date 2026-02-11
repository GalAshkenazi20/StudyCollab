package com.example.studycollab.ui.submissions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studycollab.data.model.SubmissionState

@Composable
fun SubmissionItemCard(submission: SubmissionState, onGradeClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (submission.status == "Submitted") Icons.Default.AssignmentTurnedIn else Icons.Default.Pending,
                contentDescription = null,
                tint = if (submission.status == "Submitted") Color(0xFF4CAF50) else Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = submission.groupName, style = MaterialTheme.typography.titleMedium)
                Text(text = "Status: ${submission.status}", style = MaterialTheme.typography.bodySmall)
                submission.grade?.let {
                    Text(text = "Grade: $it", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            if (submission.status == "Submitted") {
                IconButton(onClick = onGradeClick) {
                    Icon(Icons.Default.Grade, contentDescription = "Grade", tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}