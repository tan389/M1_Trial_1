package com.example.projectm1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.projectm1.ui.theme.ProjectM1Theme
//
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.activity.compose.BackHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { M1App() }
    }
}

enum class Screen { MENU, B1, B2, B3 }

@Composable
fun M1App() {
    var screen by remember { mutableStateOf(Screen.MENU) }
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            when (screen) {
                Screen.MENU -> MenuScreen(
                    onB1 = { screen = Screen.B1 },
                    onB2 = { screen = Screen.B2 },
                    onB3 = { screen = Screen.B3 }
                )
                Screen.B1 -> Button1Screen(onBack = { screen = Screen.MENU })
                Screen.B2 -> Button2Screen(onBack = { screen = Screen.MENU })
                Screen.B3 -> Button3Screen(onBack = { screen = Screen.MENU })
            }
        }
    }
}

@Composable
fun MenuScreen(onB1: () -> Unit, onB2: () -> Unit, onB3: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Button(onClick = onB1, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Login + Server") }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onB2, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Live Updates") }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onB3, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Timer") }
    }
}