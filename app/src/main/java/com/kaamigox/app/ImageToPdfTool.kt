
package com.kaamigox.app

import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream

@Composable
fun ImageToPdfTool() {
    val context = LocalContext.current

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imageNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var pdfBytes by remember { mutableStateOf<ByteArray?>(null) }
    var message by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris

        imageNames = uris.mapIndexed { index, uri ->
            try {
                context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(
                            OpenableColumns.DISPLAY_NAME
                        )
                        if (nameIndex >= 0) {
                            cursor.getString(nameIndex)
                        } else {
                            "Image ${index + 1}"
                        }
                    } else {
                        "Image ${index + 1}"
                    }
                } ?: "Image ${index + 1}"
            } catch (e: Exception) {
                "Image ${index + 1}"
            }
        }

        pdfBytes = null
        message = if (uris.isNotEmpty()) {
            "${uris.size} फोटो चुनी गईं।"
        } else {
            "कोई फोटो नहीं चुनी गई।"
        }
    }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null && pdfBytes != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(pdfBytes!!)
                }
                message = "PDF सफलतापूर्वक सेव हो गई!"
            } catch (e: Exception) {
                message = "PDF सेव नहीं हो पाई। दोबारा कोशिश करें।"
            }
        } else if (uri == null) {
            message = "PDF सेव करना रद्द किया गया।"
        }
    }

    fun generatePdf() {
        if (selectedImages.isEmpty()) {
            message = "पहले कम से कम एक फोटो चुनें।"
            return
        }

        isGenerating = true

        try {
            val document = PdfDocument()
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            var pageNumber = 0

            for (uri in selectedImages) {
                val bitmap = context.contentResolver
                    .openInputStream(uri)
                    ?.use { BitmapFactory.decodeStream(it) }

                if (bitmap != null) {
                    pageNumber++

                    val pageInfo = PdfDocument.PageInfo.Builder(
                        595, 842, pageNumber
                    ).create()

                    val page = document.startPage(pageInfo)

                    val margin = 24f
                    val scale = minOf(
                        (595f - margin * 2) / bitmap.width,
                        (842f - margin * 2) / bitmap.height
                    )

                    val width = bitmap.width * scale
                    val height = bitmap.height * scale

                    val left = (595f - width) / 2f
                    val top = (842f - height) / 2f

                    val destination = RectF(
                        left,
                        top,
                        left + width,
                        top + height
                    )

                    page.canvas.drawBitmap(
                        bitmap,
                        null,
                        destination,
                        paint
                    )

                    document.finishPage(page)
                    bitmap.recycle()
                }
            }

            if (pageNumber == 0) {
                document.close()
                message = "फोटो पढ़ी नहीं जा सकीं। दूसरी फोटो चुनें।"
            } else {
                val output = ByteArrayOutputStream()
                document.writeTo(output)
                pdfBytes = output.toByteArray()
                document.close()
                message = "$pageNumber पेज की PDF तैयार है!"
            }
        } catch (e: Exception) {
            pdfBytes = null
            message = "PDF बनाने में समस्या हुई। दोबारा कोशिश करें।"
        } finally {
            isGenerating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Image to PDF Converter",
            fontSize = 25.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Text("अपनी फोटो को PDF में बदलें।")

        Button(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Images")
        }

        if (selectedImages.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Selected Images (${selectedImages.size})",
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    imageNames.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${index + 1}.",
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = name,
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { generatePdf() },
            enabled = selectedImages.isNotEmpty() && !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(if (isGenerating) "Creating PDF..." else "Create PDF")
        }

        if (pdfBytes != null) {
            Button(
                onClick = {
                    savePdfLauncher.launch("KaamigoX_Document.pdf")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save PDF to Phone")
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
                selectedImages = emptyList()
                imageNames = emptyList()
                pdfBytes = null
                message = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear All")
        }

        Text(
            "आपकी फोटो आपके फोन पर ही PDF में बदली जाती हैं।",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
