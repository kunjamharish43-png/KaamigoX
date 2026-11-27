
package com.kaamigox.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun TextCaseConverterTool() {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }

    val clipboard = LocalClipboardManager.current
    val locale = Locale.getDefault()

    fun titleCase(text: String): String {
        return text.lowercase(locale).split(" ").joinToString(" ") { word ->
            if (word.isEmpty()) word
            else word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(locale)
                else char.toString()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Text Case Converter",
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Text("अपना टेक्स्ट यहाँ लिखें")

        OutlinedTextField(
            value = inputText,
            onValueChange = {
                inputText = it
                outputText = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            placeholder = { Text("Enter your text...") },
            label = { Text("Input Text") }
        )

        Text("Choose Conversion", fontSize = 20.sp)

        Button(
            onClick = { outputText = inputText.uppercase(locale) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("UPPERCASE")
        }

        Button(
            onClick = { outputText = inputText.lowercase(locale) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("lowercase")
        }

        Button(
            onClick = { outputText = titleCase(inputText) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Title Case")
        }

        Button(
            onClick = {
                outputText = inputText.lowercase(locale)
                    .replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(locale)
                        else char.toString()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sentence case")
        }

        if (outputText.isNotEmpty()) {
            Text("Converted Text", fontSize = 20.sp)

            OutlinedTextField(
                value = outputText,
                onValueChange = { outputText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Output") }
            )

            OutlinedButton(
                onClick = {
                    clipboard.setText(AnnotatedString(outputText))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Copy Text")
            }
        }

        OutlinedButton(
            onClick = {
                inputText = ""
                outputText = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear All")
        }
    }
}
