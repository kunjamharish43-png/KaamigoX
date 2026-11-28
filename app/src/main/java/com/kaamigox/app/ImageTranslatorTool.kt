
package com.kaamigox.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevaTextRecognizerOptions

@Composable
fun ImageTranslatorTool() {
    val context = LocalContext.current

    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var sourceLanguage by remember { mutableStateOf("en") }
    var extractedText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("फोटो चुनें या कैमरे से फोटो लें।") }
    var loading by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = context.contentResolver
                    .openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }

                if (bitmap != null) {
                    imageBitmap = bitmap
                    extractedText = ""
                    translatedText = ""
                    status = "फोटो तैयार है। अब अनुवाद करें।"
                } else {
                    status = "फोटो खोल नहीं पाए।"
                }
            } catch (e: Exception) {
                status = "फोटो खोलने में समस्या हुई।"
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            imageBitmap = bitmap
            extractedText = ""
            translatedText = ""
            status = "कैमरा फोटो तैयार है। अब अनुवाद करें।"
        }
    }

    fun translateImage() {
        val bitmap = imageBitmap

        if (bitmap == null) {
            status = "पहले फोटो चुनें या कैमरे से फोटो लें।"
            return
        }

        loading = true
        extractedText = ""
        translatedText = ""
        status = "फोटो से टेक्स्ट पहचाना जा रहा है..."

        val recognizer = if (sourceLanguage == "hi") {
            TextRecognition.getClient(
                DevaTextRecognizerOptions.Builder().build()
            )
        } else {
            TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
            )
        }

        val inputImage = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(inputImage)
            .addOnSuccessListener { result ->
                extractedText = result.text

                if (result.text.isBlank()) {
                    loading = false
                    status = "फोटो में कोई टेक्स्ट नहीं मिला।"
                    recognizer.close()
                } else {
                    status = "टेक्स्ट मिला। अनुवाद तैयार हो रहा है..."

                    val source = if (sourceLanguage == "hi") {
                        TranslateLanguage.HINDI
                    } else {
                        TranslateLanguage.ENGLISH
                    }

                    val target = if (sourceLanguage == "hi") {
                        TranslateLanguage.ENGLISH
                    } else {
                        TranslateLanguage.HINDI
                    }

                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(source)
                        .setTargetLanguage(target)
                        .build()

                    val translator = Translation.getClient(options)

                    translator.downloadModelIfNeeded(
                        DownloadConditions.Builder().build()
                    ).addOnSuccessListener {
                        translator.translate(result.text)
                            .addOnSuccessListener { translation ->
                                translatedText = translation
                                status = "अनुवाद सफल हुआ।"
                                loading = false
                                translator.close()
                                recognizer.close()
                            }
                            .addOnFailureListener { error ->
                                status = "अनुवाद नहीं हुआ: ${error.localizedMessage}"
                                loading = false
                                translator.close()
                                recognizer.close()
                            }
                    }.addOnFailureListener { error ->
                        status = "भाषा मॉडल डाउनलोड नहीं हुआ: ${error.localizedMessage}"
                        loading = false
                        translator.close()
                        recognizer.close()
                    }
                }
            }
            .addOnFailureListener { error ->
                status = "टेक्स्ट पहचानने में समस्या: ${error.localizedMessage}"
                loading = false
                recognizer.close()
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
            "Image Translator",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("फोटो में लिखे टेक्स्ट का हिंदी या अंग्रेज़ी में अनुवाद करें।")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Text("🖼 Gallery")
            }

            Button(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.weight(1f)
            ) {
                Text("📷 Camera")
            }
        }

        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!.asImageBitmap(),
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
            )

            OutlinedButton(
                onClick = {
                    imageBitmap = null
                    extractedText = ""
                    translatedText = ""
                    status = "फोटो हटा दी गई।"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("फोटो हटाएँ")
            }
        }

        Text("फोटो में लिखी भाषा चुनें:")

        Row {
            RadioButton(
                selected = sourceLanguage == "en",
                onClick = { sourceLanguage = "en" }
            )
            Text("English")

            Spacer(Modifier.width(20.dp))

            RadioButton(
                selected = sourceLanguage == "hi",
                onClick = { sourceLanguage = "hi" }
            )
            Text("हिंदी")
        }

        Text(
            if (sourceLanguage == "hi") {
                "अनुवाद की भाषा: English"
            } else {
                "अनुवाद की भाषा: हिंदी"
            }
        )

        Button(
            onClick = { translateImage() },
            enabled = imageBitmap != null && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("अनुवाद करें")
        }

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        Text(status)

        if (extractedText.isNotBlank()) {
            HorizontalDivider()

            Text(
                "फोटो से मिला टेक्स्ट",
                style = MaterialTheme.typography.titleMedium
            )

            Text(extractedText)
        }

        if (translatedText.isNotBlank()) {
            HorizontalDivider()

            Text(
                "अनुवाद",
                style = MaterialTheme.typography.titleMedium
            )

            Text(translatedText)

            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(
                        android.content.Context.CLIPBOARD_SERVICE
                    ) as android.content.ClipboardManager

                    clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText(
                            "Translation",
                            translatedText
                        )
                    )

                    status = "अनुवाद कॉपी हो गया।"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("अनुवाद कॉपी करें")
            }
        }
    }
}
