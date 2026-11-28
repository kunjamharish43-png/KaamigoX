
package com.kaamigox.app

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
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
import java.io.ByteArrayOutputStream

@Composable
fun TextToPdfTool() {
    val context = LocalContext.current

    var text by remember { mutableStateOf("") }
    var status by remember {
        mutableStateOf("अपना टेक्स्ट लिखें और PDF बनाएँ।")
    }
    var pendingPdf by remember { mutableStateOf<ByteArray?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null && pendingPdf != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(pendingPdf!!)
                }
                status = "PDF सफलतापूर्वक सेव हो गई।"
            } catch (e: Exception) {
                status = "PDF सेव नहीं हुई: ${e.localizedMessage}"
            }
        } else {
            status = "PDF सेव करना रद्द किया गया।"
        }

        pendingPdf = null
    }

    fun createPdf() {
        if (text.isBlank()) {
            status = "पहले कुछ टेक्स्ट लिखें।"
            return
        }

        try {
            val document = PdfDocument()
            val output = ByteArrayOutputStream()

            val pageWidth = 595
            val pageHeight = 842
            val margin = 48f
            val maxWidth = pageWidth - (margin * 2)

            val paint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 16f
                typeface = Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.NORMAL
                )
                isAntiAlias = true
            }

            var pageNumber = 1
            var y = 65f
            var page: PdfDocument.Page? = null
            var canvas: android.graphics.Canvas? = null

            fun startNewPage() {
                page?.let { document.finishPage(it) }

                val info = PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pageNumber
                ).create()

                page = document.startPage(info)
                canvas = page!!.canvas
                pageNumber++
                y = 65f
            }

            startNewPage()

            val lines = text.split("\n")

            for (originalLine in lines) {
                if (originalLine.isBlank()) {
                    y += 24f
                    if (y > 790f) {
                        startNewPage()
                    }
                    continue
                }

                var remaining = originalLine

                while (remaining.isNotEmpty()) {
                    if (y > 790f) {
                        startNewPage()
                    }

                    val count = paint.breakText(
                        remaining,
                        true,
                        maxWidth,
                        null
                    ).coerceAtLeast(1)

                    val line = remaining.substring(0, count)

                    canvas?.drawText(
                        line,
                        margin,
                        y,
                        paint
                    )

                    remaining = remaining.substring(count)
                    y += 24f
                }
            }

            page?.let { document.finishPage(it) }

            document.writeTo(output)
            document.close()

            pendingPdf = output.toByteArray()

            status = "PDF तैयार है। अब सेव करें।"

            saveLauncher.launch("Kaamigo-Text.pdf")

        } catch (e: Exception) {
            status = "PDF बनाने में समस्या: ${e.localizedMessage}"
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
            "Text to PDF",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("अपना टेक्स्ट लिखें और उसे PDF में बदलें।")

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp),
            label = { Text("अपना टेक्स्ट लिखें") },
            placeholder = {
                Text("यहाँ हिंदी या English में लिखें...")
            }
        )

        Text("कुल अक्षर: ${text.length}")

        Button(
            onClick = { createPdf() },
            enabled = text.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 PDF बनाएँ और सेव करें")
        }

        OutlinedButton(
            onClick = {
                text = ""
                status = "टेक्स्ट साफ कर दिया गया।"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("टेक्स्ट साफ करें")
        }

        Text(status)
    }
}
