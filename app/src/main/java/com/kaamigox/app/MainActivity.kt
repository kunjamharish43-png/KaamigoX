
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
        "Text Encoder / Decoder",
        "QR Code Generator",
        "Image to PDF Converter",
        "PDF to Image Converter",
        "Image Compressor",
        "Image Resizer",
        "PDF Merger",
        "PDF Splitter",
        "PDF Lock / Unlock",
        "Image to Text (OCR)",
        "Speech to Text",
        "Text to Speech",
        "Image Translator",
        "Text to PDF"
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
                "Text Case Converter" -> TextCaseConverterTool()
                "Text Encoder / Decoder" -> TextEncoderDecoderTool()
                "QR Code Generator" -> QRCodeGeneratorTool()
                "Image to PDF Converter" -> ImageToPdfTool()
                "PDF to Image Converter" -> PdfToImageTool()
                "Image Compressor" -> ImageCompressorTool()
                "Image Resizer" -> ImageResizerTool()
                "PDF Merger" -> PdfMergerTool()
                "PDF Splitter" -> PdfSplitterTool()
                "PDF Lock / Unlock" -> PdfLockUnlockTool()
                "Image to Text (OCR)" -> ImageToTextTool()
                "Speech to Text" -> SpeechToTextTool()
                "Text to Speech" -> TextToSpeechTool()
                "Image Translator" -> ImageTranslatorTool()
                "Text to PDF" -> TextToPdfTool()

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tools) { tool ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTool = tool
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3E8)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = tool,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("›")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
