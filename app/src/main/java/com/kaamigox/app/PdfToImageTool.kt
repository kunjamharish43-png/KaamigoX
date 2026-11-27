
package com.kaamigox.app

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
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
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun PdfToImageTool() {
    val context = LocalContext.current

    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var pdfName by remember { mutableStateOf("") }
    var pageCount by remember { mutableIntStateOf(0) }
    var pageNumber by remember { mutableStateOf("1") }
    var message by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    var singleImageBytes by remember {
        mutableStateOf<ByteArray?>(null)
    }

    var allImagesZip by remember {
        mutableStateOf<ByteArray?>(null)
    }

    fun getPdfName(uri: Uri): String {
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )
                    if (index >= 0) cursor.getString(index)
                    else "Document.pdf"
                } else {
                    "Document.pdf"
                }
            } ?: "Document.pdf"
        } catch (e: Exception) {
            "Document.pdf"
        }
    }

    fun getPageCount(uri: Uri): Int {
        val descriptor = context.contentResolver
            .openFileDescriptor(uri, "r") ?: return 0

        descriptor.use {
            PdfRenderer(it).use { renderer ->
                return renderer.pageCount
            }
        }
    }

    fun renderPage(uri: Uri, index: Int): ByteArray? {
        val descriptor = context.contentResolver
            .openFileDescriptor(uri, "r") ?: return null

        descriptor.use {
            PdfRenderer(it).use { renderer ->
                if (index !in 0 until renderer.pageCount) {
                    return null
                }

                renderer.openPage(index).use { page ->
                    val scale = minOf(
                        2f,
                        2000f / maxOf(page.width, page.height)
                    )

                    val width = (page.width * scale).toInt()
                    val height = (page.height * scale).toInt()

                    val bitmap = Bitmap.createBitmap(
                        width,
                        height,
                        Bitmap.Config.ARGB_8888
                    )

                    bitmap.eraseColor(android.graphics.Color.WHITE)

                    page.render(
                        bitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )

                    val output = ByteArrayOutputStream()
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        95,
                        output
                    )
                    bitmap.recycle()

                    return output.toByteArray()
                }
            }
        }
    }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val count = getPageCount(uri)

                if (count > 0) {
                    pdfUri = uri
                    pdfName = getPdfName(uri)
                    pageCount = count
                    pageNumber = "1"
                    singleImageBytes = null
                    allImagesZip = null
                    message = "PDF सफलतापूर्वक चुनी गई।"
                } else {
                    message = "PDF में कोई पेज नहीं मिला।"
                }
            } catch (e: Exception) {
                message = "PDF खोलने में समस्या हुई।"
            }
        }
    }

    val singleSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri ->
        if (uri != null && singleImageBytes != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(singleImageBytes!!)
                }
                message = "JPG इमेज सेव हो गई!"
            } catch (e: Exception) {
                message = "JPG सेव नहीं हो पाई।"
            }
        }
    }

    val zipSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null && allImagesZip != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(allImagesZip!!)
                }
                message = "सभी पेज की JPG ZIP में सेव हो गई!"
            } catch (e: Exception) {
                message = "ZIP फाइल सेव नहीं हो पाई।"
            }
        }
    }

    fun createSingleImage() {
        val uri = pdfUri ?: return
        val page = pageNumber.toIntOrNull()

        if (page == null || page !in 1..pageCount) {
            message = "कृपया सही पेज नंबर डालें।"
            return
        }

        isProcessing = true

        try {
            singleImageBytes = renderPage(uri, page - 1)
            message = if (singleImageBytes != null) {
                "पेज $page की JPG तैयार है।"
            } else {
                "इमेज नहीं बन पाई।"
            }
        } catch (e: Exception) {
            singleImageBytes = null
            message = "JPG बनाने में समस्या हुई।"
        } finally {
            isProcessing = false
        }
    }

    fun createAllImages() {
        val uri = pdfUri ?: return

        isProcessing = true

        try {
            val output = ByteArrayOutputStream()

            ZipOutputStream(output).use { zip ->
                for (index in 0 until pageCount) {
                    val imageBytes = renderPage(uri, index)

                    if (imageBytes == null) {
                        throw Exception("Page conversion failed")
                    }

                    val fileName = "page_${(index + 1).toString().padStart(3, '0')}.jpg"

                    zip.putNextEntry(ZipEntry(fileName))
                    zip.write(imageBytes)
                    zip.closeEntry()
                }
            }

            allImagesZip = output.toByteArray()
            message = "$pageCount पेज की JPG तैयार हैं!"
        } catch (e: Exception) {
            allImagesZip = null
            message = "सभी पेज बदलने में समस्या हुई।"
        } finally {
            isProcessing = false
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
            "PDF to Image Converter",
            fontSize = 25.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Text("PDF के पेज को JPG इमेज में बदलें।")

        Button(
            onClick = {
                pdfPicker.launch(arrayOf("application/pdf"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select PDF File")
        }

        if (pdfUri != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Selected PDF", fontSize = 18.sp)

                    Text(
                        pdfName,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text("Total Pages: $pageCount")
                }
            }

            HorizontalDivider()

            Text("Option 1: Single Page to JPG", fontSize = 19.sp)

            OutlinedTextField(
                value = pageNumber,
                onValueChange = {
                    pageNumber = it.filter { char -> char.isDigit() }
                    singleImageBytes = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Page Number (1 to $pageCount)") }
            )

            Button(
                onClick = { createSingleImage() },
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Convert Selected Page")
            }

            if (singleImageBytes != null) {
                Button(
                    onClick = {
                        singleSaveLauncher.launch("KaamigoX_Page_$pageNumber.jpg")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Single JPG")
                }
            }

            HorizontalDivider()

            Text("Option 2: All Pages to JPG", fontSize = 19.sp)

            Button(
                onClick = { createAllImages() },
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Convert All Pages")
            }

            if (allImagesZip != null) {
                Button(
                    onClick = {
                        zipSaveLauncher.launch("KaamigoX_All_Pages.zip")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save All JPG Images (ZIP)")
                }
            }
        }

        if (isProcessing) {
            CircularProgressIndicator()
            Text("कृपया प्रतीक्षा करें...")
        }

        if (message.isNotEmpty()) {
            Text(
                message,
                color = MaterialTheme.colorScheme.primary
            )
        }

        OutlinedButton(
            onClick = {
                pdfUri = null
                pdfName = ""
                pageCount = 0
                pageNumber = "1"
                singleImageBytes = null
                allImagesZip = null
                message = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear All")
        }

        Text(
            "PDF आपके फोन पर ही JPG में बदली जाती है।",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
