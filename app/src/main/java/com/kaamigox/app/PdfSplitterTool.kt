
package com.kaamigox.app

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private fun splitPdfName(context: Context, uri: Uri): String {
    var name = "PDF File"
    context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            name = cursor.getString(0) ?: name
        }
    }
    return name
}

private fun createPdfPages(
    context: Context,
    uri: Uri,
    startPage: Int,
    endPage: Int
): ByteArray {
    PDFBoxResourceLoader.init(context)

    val output = ByteArrayOutputStream()

    context.contentResolver.openInputStream(uri)?.use { input ->
        PDDocument.load(input).use { source ->
            val result = PDDocument()

            try {
                for (page in startPage..endPage) {
                    result.importPage(source.getPage(page))
                }
                result.save(output)
            } finally {
                result.close()
            }
        }
    } ?: throw Exception("PDF फाइल नहीं खुल सकी")

    return output.toByteArray()
}

private fun createPdfZip(
    context: Context,
    uri: Uri,
    pageCount: Int
): ByteArray {
    PDFBoxResourceLoader.init(context)

    val output = ByteArrayOutputStream()

    ZipOutputStream(output).use { zip ->
        context.contentResolver.openInputStream(uri)?.use { input ->
            PDDocument.load(input).use { source ->

                for (page in 0 until pageCount) {
                    val pageDocument = PDDocument()

                    try {
                        pageDocument.importPage(source.getPage(page))

                        val pageOutput = ByteArrayOutputStream()
                        pageDocument.save(pageOutput)

                        zip.putNextEntry(
                            ZipEntry(
                                "page_${(page + 1).toString().padStart(3, '0')}.pdf"
                            )
                        )
                        zip.write(pageOutput.toByteArray())
                        zip.closeEntry()
                    } finally {
                        pageDocument.close()
                    }
                }
            }
        } ?: throw Exception("PDF फाइल नहीं खुल सकी")
    }

    return output.toByteArray()
}

@Composable
fun PdfSplitterTool() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPdf by remember { mutableStateOf<Uri?>(null) }
    var pdfName by remember { mutableStateOf("") }
    var pageCount by remember { mutableIntStateOf(0) }
    var startPage by remember { mutableStateOf("1") }
    var endPage by remember { mutableStateOf("1") }
    var status by remember { mutableStateOf("पहले PDF फाइल चुनें") }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                try {
                    val count = withContext(Dispatchers.IO) {
                        PDFBoxResourceLoader.init(context)
                        context.contentResolver.openInputStream(uri)?.use {
                            PDDocument.load(it).use { document ->
                                document.numberOfPages
                            }
                        } ?: throw Exception("PDF नहीं खुल सकी")
                    }

                    selectedPdf = uri
                    pdfName = splitPdfName(context, uri)
                    pageCount = count
                    startPage = "1"
                    endPage = count.toString()
                    status = "PDF तैयार है। कुल पेज: $count"
                } catch (e: Exception) {
                    selectedPdf = null
                    status = "त्रुटि: ${e.message}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    val pdfSavePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { saveUri ->
        val sourceUri = selectedPdf
        val first = startPage.toIntOrNull()
        val last = endPage.toIntOrNull()

        if (saveUri != null && sourceUri != null &&
            first != null && last != null
        ) {
            scope.launch {
                isProcessing = true
                status = "चुने हुए पेज तैयार हो रहे हैं..."

                try {
                    val bytes = withContext(Dispatchers.IO) {
                        createPdfPages(
                            context,
                            sourceUri,
                            first - 1,
                            last - 1
                        )
                    }

                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(saveUri)
                            ?.use { it.write(bytes) }
                            ?: throw Exception("फाइल सेव नहीं हुई")
                    }

                    status = "अलग PDF सफलतापूर्वक सेव हो गई!"
                } catch (e: Exception) {
                    status = "त्रुटि: ${e.message}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    val zipSavePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { saveUri ->
        val sourceUri = selectedPdf

        if (saveUri != null && sourceUri != null) {
            scope.launch {
                isProcessing = true
                status = "सभी पेज अलग किए जा रहे हैं..."

                try {
                    val bytes = withContext(Dispatchers.IO) {
                        createPdfZip(context, sourceUri, pageCount)
                    }

                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(saveUri)
                            ?.use { it.write(bytes) }
                            ?: throw Exception("ZIP सेव नहीं हुई")
                    }

                    status = "सभी पेज ZIP में सफलतापूर्वक सेव हो गए!"
                } catch (e: Exception) {
                    status = "त्रुटि: ${e.message}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "📄 PDF Splitter",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                picker.launch(arrayOf("application/pdf"))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            Text("PDF फाइल चुनें")
        }

        if (selectedPdf != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("📄 $pdfName")
                    Text("कुल पेज: $pageCount")

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            selectedPdf = null
                            pdfName = ""
                            pageCount = 0
                            startPage = "1"
                            endPage = "1"
                            status = "PDF हटा दी गई। दूसरी PDF चुनें।"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessing
                    ) {
                        Text("🗑 चुनी हुई PDF हटाएँ")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "चुने हुए पेज अलग PDF में सेव करें",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = startPage,
                onValueChange = { startPage = it },
                label = { Text("शुरुआती पेज नंबर") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = endPage,
                onValueChange = { endPage = it },
                label = { Text("आखिरी पेज नंबर") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val first = startPage.toIntOrNull()
                    val last = endPage.toIntOrNull()

                    if (first == null || last == null ||
                        first < 1 || last > pageCount || first > last
                    ) {
                        status = "कृपया सही पेज नंबर डालें"
                    } else {
                        pdfSavePicker.launch("Selected_Pages.pdf")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                Text("चुने हुए पेज सेव करें")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    zipSavePicker.launch("Separate_PDF_Pages.zip")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                Text("सभी पेज अलग PDF में निकालें (ZIP)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isProcessing) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(status)
    }
}
