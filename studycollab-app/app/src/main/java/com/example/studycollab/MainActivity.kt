package com.example.studycollab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // If this is red, hover and Alt+Enter to import
import com.example.studycollab.ui.AppNavigation
import com.example.studycollab.ui.theme.StudyCollabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyCollabTheme {
                AppNavigation()
            }
        }
    }
}