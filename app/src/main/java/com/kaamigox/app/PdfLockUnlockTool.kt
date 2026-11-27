
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.security.SecureRandom

private fun getLockPdfName(context: Context, uri: Uri): String {
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

private fun processPdfSecurity(
    context: Context,
    uri: Uri,
    password: String,
    lockPdf: Boolean
): ByteArray {
    PDFBoxResourceLoader.init(context)

    val output = ByteArrayOutputStream()

    context.contentResolver.openInputStream(uri)?.use { input ->

        PDDocument.load(input, if (lockPdf) "" else password).use { document ->

            if (lockPdf) {
                val randomOwnerPassword = ByteArray(32)
                SecureRandom().nextBytes(randomOwnerPassword)

                val ownerPassword = randomOwnerPassword.joinToString("") {
                    "%02x".format(it)
                }

                val permission = AccessPermission()

                val policy = StandardProtectionPolicy(
                    ownerPassword,
                    password,
                    permission
                )

                policy.setEncryptionKeyLength(128)
                document.protect(policy)

            } else {
                document.setAllSecurityToBeRemoved(true)
            }

            document.save(output)
        }

    } ?: throw Exception("PDF फाइल नहीं खुल सकी")

    return output.toByteArray()
}

@Composable
fun PdfLockUnlockTool() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPdf by remember { mutableStateOf<Uri?>(null) }
    var pdfName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var lockPdf by remember { mutableStateOf(true) }
    var status by remember {
        mutableStateOf("पहले PDF फाइल चुनें")
    }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedPdf = uri
            pdfName = getLockPdfName(context, uri)
            status = "PDF चुन ली गई है"
            password = ""
        }
    }

    val savePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { saveUri ->

        val sourceUri = selectedPdf
        val enteredPassword = password
        val shouldLock = lockPdf

        if (saveUri != null && sourceUri != null) {

            scope.launch {
                isProcessing = true

                status = if (shouldLock) {
                    "PDF पर पासवर्ड लगाया जा रहा है..."
                } else {
                    "PDF का पासवर्ड हटाया जा रहा है..."
                }

                try {
                    val bytes = withContext(Dispatchers.IO) {
                        processPdfSecurity(
                            context,
                            sourceUri,
                            enteredPassword,
                            shouldLock
                        )
                    }

                    withContext(Dispatchers.IO) {
                        context.contentResolver
                            .openOutputStream(saveUri)
                            ?.use { it.write(bytes) }
                            ?: throw Exception("PDF सेव नहीं हुई")
                    }

                    status = if (shouldLock) {
                        "PDF पासवर्ड से सुरक्षित सेव हो गई!"
                    } else {
                        "PDF बिना पासवर्ड के सेव हो गई!"
                    }

                } catch (e: Exception) {
                    status = "त्रुटि: ${e.message ?: "PDF प्रोसेस नहीं हुई"}"
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
            "🔐 PDF Lock / Unlock",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            selectedPdf = null
                            pdfName = ""
                            password = ""
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
                "काम चुनें",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        lockPdf = true
                        password = ""
                        status = "PDF लॉक करने के लिए पासवर्ड डालें"
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (lockPdf) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
                ) {
                    Text("🔒 Lock")
                }

                Button(
                    onClick = {
                        lockPdf = false
                        password = ""
                        status = "PDF अनलॉक करने के लिए सही पासवर्ड डालें"
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!lockPdf) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
                ) {
                    Text("🔓 Unlock")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        if (lockPdf) {
                            "नया PDF पासवर्ड"
                        } else {
                            "PDF का मौजूदा पासवर्ड"
                        }
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (password.isBlank()) {
                        status = "कृपया पासवर्ड डालें"
                    } else if (lockPdf && password.length < 4) {
                        status = "कम से कम 4 अक्षरों का पासवर्ड डालें"
                    } else {
                        val outputName = if (lockPdf) {
                            "Locked_PDF.pdf"
                        } else {
                            "Unlocked_PDF.pdf"
                        }

                        savePicker.launch(outputName)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                Text(
                    if (lockPdf) {
                        "🔒 PDF Lock करके सेव करें"
                    } else {
                        "🔓 PDF Unlock करके सेव करें"
                    }
                )
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
