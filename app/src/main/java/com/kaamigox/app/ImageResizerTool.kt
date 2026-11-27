
package com.kaamigox.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ImageResizerTool() {
    val context = LocalContext.current

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var widthText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }

    var keepRatio by remember { mutableStateOf(true) }
    var imageFormat by remember { mutableStateOf("JPG") }

    var resultBytes by remember { mutableStateOf<ByteArray?>(null) }
    var message by remember { mutableStateOf("") }
    var isResizing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedUri = uri
        resultBytes = null
        previewBitmap = null

        if (uri != null) {
            try {
                val bitmap = context.contentResolver
                    .openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }

                if (bitmap != null) {
                    previewBitmap = bitmap
                    widthText = bitmap.width.toString()
                    heightText = bitmap.height.toString()
                    message = "फोटो चुन ली गई है।"
                } else {
                    selectedUri = null
                    message = "फोटो खुल नहीं सकी।"
                }
            } catch (e: Exception) {
                selectedUri = null
                message = "Error: ${e.message}"
            }
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
                message = "सेव करने में समस्या: ${e.message}"
            }
        }
    }

    fun resizeImage() {
        val bitmap = previewBitmap ?: return

        val newWidth = widthText.toIntOrNull()
        val newHeight = heightText.toIntOrNull()

        if (newWidth == null || newHeight == null ||
            newWidth !in 1..10000 || newHeight !in 1..10000
        ) {
            message = "Width और Height 1 से 10000 के बीच डालें।"
            return
        }

        isResizing = true
        resultBytes = null

        try {
            val resized = Bitmap.createScaledBitmap(
                bitmap,
                newWidth,
                newHeight,
                true
            )

            val stream = java.io.ByteArrayOutputStream()

            val format = if (imageFormat == "PNG") {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }

            val success = resized.compress(
                format,
                90,
                stream
            )

            if (!success) {
                throw Exception("फोटो Resize नहीं हो सकी।")
            }

            resultBytes = stream.toByteArray()

            message = "Resize सफल! नया साइज़: ${newWidth} × ${newHeight}"

            if (resized !== bitmap) {
                resized.recycle()
            }
        } catch (e: Exception) {
            message = "Error: ${e.message}"
        } finally {
            isResizing = false
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
            "🖼️ Image Resizer",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("फोटो की Width और Height अपनी पसंद से बदलें।")

        Button(
            onClick = { picker.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("फोटो चुनें")
        }

        previewBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Selected photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Text("Original: ${bitmap.width} × ${bitmap.height} Pixels")

            OutlinedButton(
                onClick = {
                    selectedUri = null
                    previewBitmap = null
                    resultBytes = null
                    widthText = ""
                    heightText = ""
                    message = "फोटो हटा दी गई है।"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("❌ फोटो हटाएँ")
            }
        }

        OutlinedTextField(
            value = widthText,
            onValueChange = { value ->
                if (value.length <= 5 && value.all { it.isDigit() }) {
                    widthText = value
                    resultBytes = null

                    val bitmap = previewBitmap
                    val width = value.toIntOrNull()

                    if (keepRatio && bitmap != null &&
                        width != null && width > 0
                    ) {
                        heightText = (
                            width.toDouble() * bitmap.height /
                                bitmap.width
                            ).roundToInt().toString()
                    }
                }
            },
            label = { Text("Width (Pixels)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = heightText,
            onValueChange = { value ->
                if (value.length <= 5 && value.all { it.isDigit() }) {
                    heightText = value
                    resultBytes = null

                    val bitmap = previewBitmap
                    val height = value.toIntOrNull()

                    if (keepRatio && bitmap != null &&
                        height != null && height > 0
                    ) {
                        widthText = (
                            height.toDouble() * bitmap.width /
                                bitmap.height
                            ).roundToInt().toString()
                    }
                }
            },
            label = { Text("Height (Pixels)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Original Ratio बनाए रखें")

            Switch(
                checked = keepRatio,
                onCheckedChange = { keepRatio = it }
            )
        }

        Text("फोटो Format चुनें")

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = imageFormat == "JPG",
                onClick = {
                    imageFormat = "JPG"
                    resultBytes = null
                },
                label = { Text("JPG") }
            )

            FilterChip(
                selected = imageFormat == "PNG",
                onClick = {
                    imageFormat = "PNG"
                    resultBytes = null
                },
                label = { Text("PNG") }
            )
        }

        Button(
            onClick = { resizeImage() },
            enabled = previewBitmap != null && !isResizing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isResizing) "Resizing..." else "Resize Image")
        }

        if (resultBytes != null) {
            Text("नया फोटो साइज़: ${resultBytes!!.size / 1024} KB")

            Button(
                onClick = {
                    val extension = if (imageFormat == "PNG") "png" else "jpg"
                    saveLauncher.launch("KaamigoX_Resized.$extension")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("फोटो सेव करें")
            }
        }

        if (message.isNotEmpty()) {
            Text(message)
        }

        Text("नोट: बहुत बड़ी Width या Height चुनने पर अधिक मेमोरी लग सकती है।")
    }
}
