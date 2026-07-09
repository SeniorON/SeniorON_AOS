package com.example.senior_on

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.senior_on.ui.app.SeniorOnApp
import com.example.senior_on.ui.theme.SENIOR_ONTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SENIOR_ONTheme {
                SeniorOnApp()
            }
        }
    }
}
