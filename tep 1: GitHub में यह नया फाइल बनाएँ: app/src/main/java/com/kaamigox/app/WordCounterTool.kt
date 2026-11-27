
package com.kaamigox.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WordCounterTool() {
    var text by remember { mutableStateOf("") }

    val words = text.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .size

    val characters = text.length

    val charactersWithoutSpaces = text.count {
        !it.isWhitespace()
    }

    val sentences = text.split(Regex("[.!?]+"))
        .count { it.isNotBlank() }

    val paragraphs = text.lines()
        .count { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Word Counter",
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            placeholder = {
                Text("यहाँ अपना Text लिखें या Paste करें...")
            },
            label = { Text("Your Text") }
        )

        Text(
            "Text Statistics",
            fontSize = 22.sp
        )

        StatItem("Words", words.toString())
        StatItem("Characters", characters.toString())
        StatItem(
            "Characters without spaces",
            charactersWithoutSpaces.toString()
        )
        StatItem("Sentences", sentences.toString())
        StatItem("Paragraphs", paragraphs.toString())

        Button(
            onClick = { text = "" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear Text")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 16.sp)
            Text(
                value,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
