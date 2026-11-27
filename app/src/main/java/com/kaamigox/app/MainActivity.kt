
package com.kaamigox.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Orange = Color(0xFFFF7900)
private val DarkOrange = Color(0xFFE96500)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KaamigoApp()
        }
    }
}

@Composable
fun KaamigoApp() {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedTool by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "All", "Text", "Image", "Calculator",
        "Security", "Internet", "AI", "Other"
    )

    val tools = listOf(
        "Word Counter", "Character Counter", "Case Converter",
        "Text Reverser", "Password Generator", "PIN Generator",
        "Line Sorter", "Space Cleaner", "Text Compare",
        "Duplicate Remover", "QR Code Generator", "Barcode Generator",
        "Calculator", "Scientific Calculator", "Age Calculator",
        "BMI Calculator", "Percentage Calculator", "Discount Calculator",
        "GST Calculator", "EMI Calculator", "Unit Converter",
        "Length Converter", "Weight Converter", "Temperature Converter",
        "Time Converter", "Currency Calculator", "Date Calculator",
        "Stopwatch", "Timer", "Alarm Helper",
        "Notes", "To Do List", "Daily Planner",
        "Reminder", "Checklist", "Calendar Helper",
        "Image Resizer", "Image Compressor", "Image Cropper",
        "Image Rotator", "Image Converter", "Color Picker",
        "Color Palette", "Gradient Maker", "Screenshot Helper",
        "PDF Merger", "PDF Splitter", "PDF Compressor",
        "PDF to Text", "Text to PDF", "File Renamer",
        "File Size Checker", "Storage Analyzer", "File Organizer",
        "WiFi Information", "IP Information", "Network Checker",
        "Internet Speed Test", "DNS Lookup", "URL Encoder",
        "URL Decoder", "Base64 Encoder", "Base64 Decoder",
        "HTML Formatter", "JSON Formatter", "XML Formatter",
        "Password Strength", "Password Checker", "Hash Generator",
        "Text Encryptor", "Text Decryptor", "Secure Notes",
        "Offline AI Chat", "AI Text Helper", "AI Summarizer",
        "AI Translator", "AI Writing Helper", "AI Study Helper",
        "Language Translator", "Dictionary", "Word Meaning",
        "Typing Speed Test", "Morse Code", "Binary Converter",
        "Roman Converter", "Number to Words", "Random Number",
        "Random Name", "Coin Toss", "Dice Roller",
        "Flashlight", "Sound Meter", "Compass",
        "Screen Info", "Device Information", "App Information",
        "Privacy Checker", "QR Scanner"
    )

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Orange,
            secondary = DarkOrange,
            background = Color(0xFFFFF8F2),
            surface = Color.White
        )
    ) {
        Scaffold(
            containerColor = Color(0xFFFFF8F2),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Orange)
                        .padding(18.dp)
                ) {
                    Text(
                        "Kaamigo X",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        "Your Smart Tools Universe",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "100+ Smart Tools • Offline First",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 14.dp)
            ) {
                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search your tools...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Explore Categories",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    categories.take(4).forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    categories.drop(4).forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    "Popular Smart Tools",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                val filteredTools = tools.filter {
                    it.contains(search, ignoreCase = true)
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTools) { tool ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clickable {
                                    selectedTool = tool
                                },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "✦",
                                    fontSize = 25.sp,
                                    color = Orange
                                )
                                Text(
                                    tool,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }

        selectedTool?.let { tool ->
            AlertDialog(
                onDismissRequest = { selectedTool = null },
                title = { Text(tool) },
                text = {
                    Text("Welcome to $tool in Kaamigo X. This tool will be added with its working functionality.")
                },
                confirmButton = {
                    TextButton(
                        onClick = { selectedTool = null }
                    ) {
                        Text("OK", color = Orange)
                    }
                }
            )
        }
    }
}
