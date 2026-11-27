
package com.kaamigox.app

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

private fun getPdfName(context: Context, uri: Uri): String {
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

@Composable
fun PdfMergerTool() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFiles by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }

    var status by remember {
        mutableStateOf("कम से कम 2 PDF फाइलें चुनें")
    }

    var isMerging by remember {
        mutableStateOf(false)
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedFiles = uris
            status = "${uris.size} PDF फाइलें चुनी गईं"
        }
    }

    val savePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isMerging = true
                status = "PDF जोड़ी जा रही हैं..."

                try {
                    val pdfBytes = withContext(Dispatchers.IO) {
                        PDFBoxResourceLoader.init(context)

                        val output = ByteArrayOutputStream()
                        val mergedDocument = PDDocument()

                        try {
                            selectedFiles.forEach { fileUri ->
                                context.contentResolver
                                    .openInputStream(fileUri)
                                    ?.use { input ->
                                        PDDocument.load(input).use { source ->
                                            for (i in 0 until source.numberOfPages) {
                                                mergedDocument.importPage(
                                                    source.getPage(i)
                                                )
                                            }
                                        }
                                    } ?: throw Exception(
                                        "PDF फाइल नहीं खुल सकी"
                                    )
                            }

                            mergedDocument.save(output)
                            output.toByteArray()
                        } finally {
                            mergedDocument.close()
                        }
                    }

                    withContext(Dispatchers.IO) {
                        context.contentResolver
                            .openOutputStream(uri)
                            ?.use { it.write(pdfBytes) }
                            ?: throw Exception("फाइल सेव नहीं हो सकी")
                    }

                    status = "PDF सफलतापूर्वक सेव हो गई!"
                } catch (e: Exception) {
                    status = "त्रुटि: ${e.message ?: "PDF नहीं जुड़ सकीं"}"
                } finally {
                    isMerging = false
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
            text = "📄 PDF Merger",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                picker.launch(arrayOf("application/pdf"))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isMerging
        ) {
            Text("PDF फाइलें चुनें")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("चुनी गई फाइलें: ${selectedFiles.size}")

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false)
        ) {
            itemsIndexed(selectedFiles) { index, uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}. ${getPdfName(context, uri)}",
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = {
                                selectedFiles = selectedFiles.filterIndexed {
                                    i, _ -> i != index
                                }
                                status = "फाइल हटाई गई"
                            },
                            enabled = !isMerging
                        ) {
                            Text("हटाएँ")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (selectedFiles.size < 2) {
                    status = "कृपया कम से कम 2 PDF चुनें"
                } else {
                    savePicker.launch("Merged_PDF.pdf")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedFiles.size >= 2 && !isMerging
        ) {
            Text(if (isMerging) "कृपया प्रतीक्षा करें..." else "PDF जोड़ें और सेव करें")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(status)
    }
}
