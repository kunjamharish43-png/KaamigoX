
package com.kaamigox.app

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun QRCodeGeneratorTool() {
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var message by remember { mutableStateOf("") }

    fun generateQR() {
        if (inputText.isBlank()) {
            message = "कृपया पहले Text या Website URL लिखें।"
            qrBitmap = null
            return
        }

        try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 2
            )

            val matrix = QRCodeWriter().encode(
                inputText,
                BarcodeFormat.QR_CODE,
                700,
                700,
                hints
            )

            val bitmap = Bitmap.createBitmap(
                matrix.width,
                matrix.height,
                Bitmap.Config.ARGB_8888
            )

            for (x in 0 until matrix.width) {
                for (y in 0 until matrix.height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (matrix[x, y]) {
                            android.graphics.Color.BLACK
                        } else {
                            android.graphics.Color.WHITE
                        }
                    )
                }
            }

            qrBitmap = bitmap
            message = "आपका QR Code तैयार है!"
        } catch (e: Exception) {
            qrBitmap = null
            message = "QR Code नहीं बन पाया। दोबारा कोशिश करें।"
        }
    }

    fun saveQR(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            message = "Gallery Save के लिए Android 10 या नया संस्करण चाहिए।"
            return
        }

        try {
            val values = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "KaamigoX_QR_${System.currentTimeMillis()}.png"
                )
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/KaamigoX"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

            if (uri == null) {
                message = "QR Code Save नहीं हो पाया।"
                return
            }

            val output = context.contentResolver.openOutputStream(uri)

            if (output == null) {
                context.contentResolver.delete(uri, null, null)
                message = "फाइल सेव नहीं हो पाई।"
                return
            }

            output.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)

            message = "QR Code Gallery में Save हो गया!"
        } catch (e: Exception) {
            message = "Save करने में समस्या हुई। दोबारा कोशिश करें।"
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
            "QR Code Generator",
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Text("Text या Website का QR Code बनाएँ")

        OutlinedTextField(
            value = inputText,
            onValueChange = {
                inputText = it
                message = ""
                qrBitmap = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            label = { Text("Text या Website URL") },
            placeholder = {
                Text("यहाँ अपना Text या Link लिखें...")
            }
        )

        Button(
            onClick = { generateQR() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("Generate QR Code", fontSize = 18.sp)
        }

        qrBitmap?.let { bitmap ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Your QR Code", fontSize = 20.sp)

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated QR Code",
                        modifier = Modifier.size(260.dp)
                    )

                    Button(
                        onClick = { saveQR(bitmap) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save QR Code to Gallery")
                    }
                }
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
                inputText = ""
                qrBitmap = null
                message = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear All")
        }

        Text(
            "QR Code आपके फोन पर Offline बनाया जाता है।",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
