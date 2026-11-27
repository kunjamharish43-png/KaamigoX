
package com.kaamigox.app

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun QRCodeGeneratorTool() {
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

        Text(
            "Text या Website का QR Code बनाएँ",
            fontSize = 15.sp
        )

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
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
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

                    Text(
                        "QR Code सफलतापूर्वक बन गया।",
                        color = MaterialTheme.colorScheme.primary
                    )
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
            "यह QR Code आपके फोन पर Offline बनाया जाता है।",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
