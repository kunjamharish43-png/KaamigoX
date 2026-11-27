
package com.kaamigox.app

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import android.graphics.ImageDecoder

@Composable
fun ImageToTextTool() {
    val context = LocalContext.current

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var extractedText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        extractedText = ""
        status = ""
        bitmap = null

        if (uri != null) {
            try {
                bitmap = if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(
                        context.contentResolver, uri
                    )
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver, uri
                    )
                }
            } catch (e: Exception) {
                status = "फोटो खोलने में समस्या: ${e.message}"
            }
        }
    }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(extractedText.toByteArray(Charsets.UTF_8))
                }
                status = "TXT फाइल सफलतापूर्वक सेव हो गई।"
            } catch (e: Exception) {
                status = "फाइल सेव नहीं हुई: ${e.message}"
            }
        }
    }

    fun recognizeText() {
        val selectedBitmap = bitmap ?: return

        isProcessing = true
        status = "टेक्स्ट पहचाना जा रहा है..."

        val image = InputImage.fromBitmap(selectedBitmap, 0)

        val latinRecognizer = TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

        val devanagariRecognizer = TextRecognition.getClient(
            DevanagariTextRecognizerOptions.Builder().build()
        )

        latinRecognizer.process(image)
            .addOnSuccessListener { latinResult ->
                devanagariRecognizer.process(image)
                    .addOnSuccessListener { hindiResult ->

                        val englishText = latinResult.text
                        val hindiText = hindiResult.text

                        extractedText = listOf(
                            hindiText,
                            englishText
                        ).filter { it.isNotBlank() }
                            .distinct()
                            .joinToString("\n\n")

                        status = if (extractedText.isBlank()) {
                            "कोई टेक्स्ट नहीं मिला।"
                        } else {
                            "टेक्स्ट सफलतापूर्वक पहचाना गया।"
                        }

                        isProcessing = false
                        latinRecognizer.close()
                        devanagariRecognizer.close()
                    }
                    .addOnFailureListener { error ->
                        extractedText = latinResult.text
                        status = "हिंदी पहचान में समस्या: ${error.message}"
                        isProcessing = false
                        latinRecognizer.close()
                        devanagariRecognizer.close()
                    }
            }
            .addOnFailureListener { error ->
                status = "टेक्स्ट पहचान में समस्या: ${error.message}"
                isProcessing = false
                latinRecognizer.close()
                devanagariRecognizer.close()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Image to Text (OCR)",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("फोटो से हिंदी और English टेक्स्ट निकालें।")

        Button(
            onClick = { picker.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📷 फोटो चुनें")
        }

        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
            )
        }

        if (bitmap != null) {
            Button(
                onClick = { recognizeText() },
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("टेक्स्ट निकालें")
            }
        }

        if (isProcessing) {
            CircularProgressIndicator()
        }

        if (status.isNotBlank()) {
            Text(status)
        }

        if (extractedText.isNotBlank()) {
            Text(
                "निकाला गया टेक्स्ट",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = extractedText,
                onValueChange = { extractedText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                label = {
                    Text("आप टेक्स्ट एडिट भी कर सकते हैं")
                }
            )

            Button(
                onClick = {
                    val clipboard = context.getSystemService(
                        android.content.Context.CLIPBOARD_SERVICE
                    ) as android.content.ClipboardManager

                    clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText(
                            "OCR Text",
                            extractedText
                        )
                    )

                    status = "टेक्स्ट कॉपी हो गया।"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("टेक्स्ट कॉपी करें")
            }

            Button(
                onClick = {
                    saveLauncher.launch("OCR_Text.txt")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("TXT फाइल में सेव करें")
            }
        }
    }
}
