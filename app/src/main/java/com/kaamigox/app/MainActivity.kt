
package com.kaamigox.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Orange = Color(0xFFFF7A00)
private val LightOrange = Color(0xFFFFF3E8)
private val DarkText = Color(0xFF252525)

data class ToolItem(
    val name: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Orange,
                    secondary = Orange,
                    background = Color(0xFFFFFBF7)
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
    var searchText by remember { mutableStateOf("") }

    val categories = listOf(
        "Text Tools" to listOf(
            ToolItem("Word Counter", Icons.Filled.TextFields),
            ToolItem("Text Case Converter", Icons.Filled.SwapHoriz),
            ToolItem("Text Encoder / Decoder", Icons.Filled.Code),
            ToolItem("Text to PDF", Icons.Filled.Description),
            ToolItem("PDF to Text", Icons.Filled.Description)
        ),
        "PDF Tools" to listOf(
            ToolItem("Image to PDF Converter", Icons.Filled.PictureAsPdf),
            ToolItem("PDF to Image Converter", Icons.Filled.Image),
            ToolItem("PDF Merger", Icons.Filled.Description),
            ToolItem("PDF Splitter", Icons.Filled.CallSplit),
            ToolItem("PDF Lock / Unlock", Icons.Filled.Lock)
        ),
        "Image Tools" to listOf(
            ToolItem("Image Compressor", Icons.Filled.Compress),
            ToolItem("Image Resizer", Icons.Filled.AspectRatio),
            ToolItem("Image to Text (OCR)", Icons.Filled.Image),
            ToolItem("Image Translator", Icons.Filled.Translate)
        ),
        "Other Tools" to listOf(
            ToolItem("Calculator", Icons.Filled.Calculate),
            ToolItem("Password Generator", Icons.Filled.Lock),
            ToolItem("QR Code Generator", Icons.Filled.QrCode),
            ToolItem("Speech to Text", Icons.Filled.Mic),
            ToolItem("Text to Speech", Icons.Filled.VolumeUp)
        )
    )

    val filteredGroups = categories.mapNotNull { group ->
        val filtered = group.second.filter {
            it.name.contains(searchText, ignoreCase = true)
        }
        if (filtered.isNotEmpty()) group.first to filtered else null
    }

    Scaffold(
        containerColor = Color(0xFFFFFBF7),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedTool.isEmpty()) "Kaamigo X"
                        else selectedTool,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (selectedTool.isNotEmpty()) {
                        IconButton(onClick = {
                            selectedTool = ""
                        }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->

        if (selectedTool.isNotEmpty()) {
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
                    "PDF to Text" -> PdfToTextTool()
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Orange
                            ),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Apps,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "Kaamigo X",
                                        color = Color.White,
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    "Your All-in-One Utility App",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )

                                Spacer(Modifier.height(18.dp))

                                Text(
                                    "19 Useful Tools",
                                    color = Color.White,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Search tools...")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchText = "" }
                                    ) {
                                        Icon(
                                            Icons.Filled.Clear,
                                            contentDescription = "Clear"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                filteredGroups.forEach { group ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = group.first,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText,
                            modifier = Modifier.padding(
                                top = 12.dp,
                                bottom = 2.dp
                            )
                        )
                    }

                    items(group.second) { tool ->
                        ToolCard(
                            tool = tool,
                            onClick = {
                                selectedTool = tool.name
                            }
                        )
                    }
                }

                if (filteredGroups.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            "No tools found",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToolCard(
    tool: ToolItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.name,
                tint = Orange,
                modifier = Modifier.size(30.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = tool.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                lineHeight = 17.sp
            )
        }
    }
}
