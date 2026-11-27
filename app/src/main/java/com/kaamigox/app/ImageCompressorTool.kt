
package com.kaamigox.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ImageCompressorTool() {
    val context = LocalContext.current

    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var targetSize by remember { mutableStateOf("200") }
    var sizeUnit by remember { mutableStateOf("KB") }
    var resultBytes by remember { mutableStateOf<ByteArray?>(null) }
    var originalSize by remember { mutableStateOf(0L) }
    var resultSize by remember { mutableStateOf(0L) }
    var message by remember { mutableStateOf("") }
    var isCompressing by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImage = uri
        resultBytes = null
        originalSize = 0L
        resultSize = 0L
        previewBitmap = null

        if (uri != null) {
            try {
                previewBitmap = context.contentResolver
                    .openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                originalSize = context.contentResolver
                    .openInputStream(uri)?.use {
                        it.available().toLong()
                    } ?: 0L
                message = if (previewBitmap != null) {
                    "फोटो चुन ली गई है।"
                } else {
                    "फोटो खुल नहीं सकी।"
                }
            } catch (e: Exception) {
                message = "फोटो खुल नहीं सकी: ${e.message}"
            }
        } else {
            message = ""
        }
    }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri ->
        if (uri != null && resultBytes != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(resultBytes!!)
                }
                message = "फोटो सफलतापूर्वक सेव हो गई!"
            } catch (e: Exception) {
                message = "सेव नहीं हो सकी: ${e.message}"
            }
        }
    }

    fun formatSize(bytes: Long): String {
        return if (bytes >= 1024 * 1024) {
            String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        } else {
            String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        }
    }

    fun compressImage() {
        val uri = selectedImage ?: return
        val enteredSize = targetSize.toDoubleOrNull()

        if (enteredSize == null || enteredSize <= 0) {
            message = "सही साइज़ डालें।"
            return
        }

        val targetBytes = (
            enteredSize *
                if (sizeUnit == "MB") 1024 * 1024 else 1024
            ).toLong()

        isCompressing = true
        resultBytes = null

        try {
            val input = context.contentResolver
                .openInputStream(uri)?.use { it.readBytes() }
                ?: throw Exception("फोटो पढ़ी नहीं जा सकी।")

            originalSize = input.size.toLong()

            val originalBitmap = BitmapFactory.decodeByteArray(
                input, 0, input.size
            ) ?: throw Exception("यह फोटो सपोर्ट नहीं होती।")

            var workingBitmap = originalBitmap
            var bestBytes: ByteArray? = null
            var attempts = 0

            while (attempts < 12) {
                var low = 15
                var high = 95
                var found: ByteArray? = null

                while (low <= high) {
                    val quality = (low + high) / 2
                    val stream = ByteArrayOutputStream()

                    workingBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        quality,
                        stream
                    )

                    val data = stream.toByteArray()

                    if (data.size <= targetBytes) {
                        found = data
                        low = quality + 1
                    } else {
                        high = quality - 1
                    }
                }

                if (found != null) {
                    bestBytes = found
                    break
                }

                if (workingBitmap.width <= 320 ||
                    workingBitmap.height <= 320
                ) {
                    break
                }

                val newWidth =
                    (workingBitmap.width * 0.80).roundToInt()
                val newHeight =
                    (workingBitmap.height * 0.80).roundToInt()

                val smallerBitmap = Bitmap.createScaledBitmap(
                    workingBitmap,
                    newWidth.coerceAtLeast(1),
                    newHeight.coerceAtLeast(1),
                    true
                )

                if (workingBitmap !== originalBitmap) {
                    workingBitmap.recycle()
                }
                workingBitmap = smallerBitmap
                attempts++
            }

            if (bestBytes == null) {
                val stream = ByteArrayOutputStream()
                workingBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    15,
                    stream
                )
                bestBytes = stream.toByteArray()
            }

            resultBytes = bestBytes
            resultSize = bestBytes.size.toLong()

            message = if (resultSize <= targetBytes) {
                "Compression सफल! चुना हुआ साइज़ प्राप्त हुआ।"
            } else {
                "फोटो छोटी हुई, लेकिन चुना हुआ साइज़ प्राप्त नहीं हुआ।"
            }

            if (workingBitmap !== originalBitmap) {
                workingBitmap.recycle()
            }
            originalBitmap.recycle()

        } catch (e: Exception) {
            message = "Error: ${e.message}"
        } finally {
            isCompressing = false
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
            "📷 Image Compressor",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("अपनी फोटो का साइज़ अपनी पसंद के अनुसार कम करें।")

        Button(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("फोटो चुनें")
        }

        if (selectedImage != null) {
            previewBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Selected photo preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Text("Original Size: ${formatSize(originalSize)}")

            OutlinedButton(
                onClick = {
                    selectedImage = null
                    previewBitmap = null
                    resultBytes = null
                    originalSize = 0L
                    resultSize = 0L
                    message = "फोटो हटा दी गई है।"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("❌ चुनी हुई फोटो हटाएँ")
            }
        }

        Text("Target Size चुनें")

        OutlinedTextField(
            value = targetSize,
            onValueChange = {
                if (it.length <= 8 && it.all { c ->
                        c.isDigit() || c == '.'
                    }) {
                    targetSize = it
                }
            },
            label = { Text("Target Size") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = sizeUnit == "KB",
                onClick = { sizeUnit = "KB" },
                label = { Text("KB") }
            )

            FilterChip(
                selected = sizeUnit == "MB",
                onClick = { sizeUnit = "MB" },
                label = { Text("MB") }
            )
        }

        Text("उदाहरण: 100 KB, 200 KB, 500 KB या 1 MB")

        Button(
            onClick = { compressImage() },
            enabled = selectedImage != null && !isCompressing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isCompressing) "Compressing..."
                else "Compress Image"
            )
        }

        if (resultBytes != null) {
            HorizontalDivider()

            Text("Original: ${formatSize(originalSize)}")
            Text("Compressed: ${formatSize(resultSize)}")

            Button(
                onClick = {
                    saveLauncher.launch("KaamigoX_Compressed.jpg")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("फोटो सेव करें")
            }
        }

        if (message.isNotEmpty()) {
            Text(message)
        }

        Text(
            "नोट: फोटो JPEG में सेव होगी। चुना हुआ साइज़ हर फोटो में " +
                "मिलना संभव नहीं है।"
        )
    }
}
