
package com.kaamigox.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFFFF7A00)
                )
            ) {
                KaamigoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaamigoApp() {
    var selectedTool by remember { mutableStateOf("") }

    val tools = listOf(
        "Calculator",
        "Word Counter",
        "Password Generator",
        "Text Case Converter",
        "Text Encoder / Decoder"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedTool.isEmpty()) {
                            "Kaamigo X"
                        } else {
                            selectedTool
                        }
                    )
                },
                navigationIcon = {
                    if (selectedTool.isNotEmpty()) {
                        TextButton(
                            onClick = { selectedTool = "" }
                        ) {
                            Text("← Back")
                        }
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTool) {
                "Calculator" -> CalculatorTool()

                "Word Counter" -> WordCounterTool()

                "Password Generator" -> PasswordGeneratorTool()

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(tools) { tool ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        selectedTool = tool
                                    }
                            ) {
                                Text(
                                    text = tool,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
