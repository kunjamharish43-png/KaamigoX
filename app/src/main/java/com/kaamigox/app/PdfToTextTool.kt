
package com.kaamigox.app

import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.util.concurrent.Executors

@Composable
fun PdfToTextTool() {
    val context = LocalContext.current

    var selectedPdf by remember { mutableStateOf(false) }
    var extractedText by remember { mutableStateOf("") }
    var status by remember {
        mutableStateOf("PDF चुनें और उसका टेक्स्ट निकालें।")
    }
    var loading by remember { mutableStateOf(false) }
    var pdfName by remember { mutableStateOf("") }

    val executor = remember {
        Executors.newSingleThreadExecutor()
    }

    val mainHandler = remember {
        Handler(Looper.getMainLooper())
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(extractedText.toByteArray(Charsets.UTF_8))
                }
                status = "टेक्स्ट TXT फाइल में सेव हो गया।"
            } catch (e: Exception) {
                status = "फाइल सेव नहीं हुई: ${e.localizedMessage}"
            }
        } else {
            status = "सेव करना रद्द किया गया।"
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedPdf = true
            extractedText = ""
            loading = true
            status = "PDF से टेक्स्ट निकाला जा रहा है..."

            pdfName = uri.lastPathSegment ?: "Selected PDF"

            executor.execute {
                var document: PDDocument? = null

                try {
                    PDFBoxResourceLoader.init(context)

                    val inputStream =
                        context.contentResolver.openInputStream(uri)
                            ?: throw Exception("PDF फाइल नहीं खुली।")

                    document = inputStream.use {
                        PDDocument.load(it)
                    }

                    val pageCount = document.numberOfPages
                    val stripper = PDFTextStripper()
                    val result = stripper.getText(document)

                    mainHandler.post {
                        extractedText = result
                        loading = false

                        status = if (result.isBlank()) {
                            "PDF में टेक्स्ट नहीं मिला। संभव है यह स्कैन की हुई PDF हो।"
                        } else {
                            "$pageCount पेजों से टेक्स्ट निकाला गया।"
                        }
                    }

                } catch (e: Exception) {
                    mainHandler.post {
                        loading = false
                        status = "PDF पढ़ने में समस्या: ${e.localizedMessage}"
                    }
                } finally {
                    try {
                        document?.close()
                    } catch (_: Exception) {
                    }
                }
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
            "PDF to Text",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("PDF के सभी पेजों से टेक्स्ट निकालें।")

        Button(
            onClick = {
                pdfLauncher.launch(arrayOf("application/pdf"))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("📄 PDF चुनें")
        }

        if (selectedPdf) {
            Text(
                "चुनी गई PDF: $pdfName",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedButton(
                onClick = {
                    selectedPdf = false
                    extractedText = ""
                    pdfName = ""
                    status = "दूसरी PDF चुनें।"
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text("PDF हटाएँ / दूसरी PDF चुनें")
            }
        }

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        Text(status)

        if (extractedText.isNotEmpty()) {
            HorizontalDivider()

            Text(
                "निकाला गया टेक्स्ट",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = extractedText,
                onValueChange = { extractedText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp),
                label = { Text("आप टेक्स्ट एडिट भी कर सकते हैं") }
            )

            Text("कुल अक्षर: ${extractedText.length}")

            Button(
                onClick = {
                    val clipboard =
                        context.getSystemService(
                            android.content.Context.CLIPBOARD_SERVICE
                        ) as android.content.ClipboardManager

                    clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText(
                            "PDF Text",
                            extractedText
                        )
                    )

                    status = "टेक्स्ट कॉपी हो गया।"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📋 टेक्स्ट कॉपी करें")
            }

            OutlinedButton(
                onClick = {
                    saveLauncher.launch("Kaamigo-PDF-Text.txt")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💾 TXT फाइल सेव करें")
            }

            OutlinedButton(
                onClick = {
                    extractedText = ""
                    status = "टेक्स्ट साफ कर दिया गया।"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("टेक्स्ट साफ करें")
            }
        }
    }
}
