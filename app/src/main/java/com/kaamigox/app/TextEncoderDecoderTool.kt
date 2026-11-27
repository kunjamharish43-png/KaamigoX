
package com.kaamigox.app

import android.util.Base64
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
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun TextEncoderDecoderTool() {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val clipboard = LocalClipboardManager.current

    fun convert(action: String) {
        try {
            outputText = when (action) {
                "Base64 Encode" -> {
                    Base64.encodeToString(
                        inputText.toByteArray(StandardCharsets.UTF_8),
                        Base64.NO_WRAP
                    )
                }

                "Base64 Decode" -> {
                    String(
                        Base64.decode(inputText, Base64.DEFAULT),
                        StandardCharsets.UTF_8
                    )
                }

                "URL Encode" -> {
                    URLEncoder.encode(
                        inputText,
                        StandardCharsets.UTF_8.name()
                    )
                }

                "URL Decode" -> {
                    URLDecoder.decode(
                        inputText,
                        StandardCharsets.UTF_8.name()
                    )
                }

                else -> ""
            }

            message = "Conversion सफल हुआ!"
        } catch (e: Exception) {
            outputText = ""
            message = "गलत Input! कृपया टेक्स्ट जाँचें।"
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
            "Text Encoder / Decoder",
            fontSize = 25.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            "Base64 और URL Text को Encode या Decode करें।",
            fontSize = 15.sp
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = {
                inputText = it
                message = ""
                outputText = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            label = { Text("Input Text") },
            placeholder = { Text("यहाँ अपना टेक्स्ट लिखें...") }
        )

        Text("Base64 Tools", fontSize = 19.sp)

        Button(
            onClick = { convert("Base64 Encode") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Base64 Encode")
        }

        OutlinedButton(
            onClick = { convert("Base64 Decode") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Base64 Decode")
        }

        HorizontalDivider()

        Text("URL Tools", fontSize = 19.sp)

        Button(
            onClick = { convert("URL Encode") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("URL Encode")
        }

        OutlinedButton(
            onClick = { convert("URL Decode") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("URL Decode")
        }

        if (outputText.isNotEmpty()) {
            Text("Output Result", fontSize = 20.sp)

            OutlinedTextField(
                value = outputText,
                onValueChange = { outputText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Converted Text") }
            )

            Button(
                onClick = {
                    clipboard.setText(AnnotatedString(outputText))
                    message = "Output Copy हो गया!"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Copy Output")
            }
        }

        if (message.isNotEmpty()) {
            Text(
                message,
                color = MaterialTheme.colorScheme.primary
            )
        }

        OutlinedButton(
            onClick = {
                inputText = ""
                outputText = ""
                message = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear All")
        }

        Text(
            "ध्यान दें: Encoding पासवर्ड या गोपनीय डेटा को सुरक्षित नहीं करती।",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
