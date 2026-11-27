
package com.kaamigox.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import android.net.Uri

@Composable
fun SpeechToTextTool() {
    val context = LocalContext.current

    var language by remember { mutableStateOf("hi-IN") }
    var offlineMode by remember { mutableStateOf(true) }
    var recognizedText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    var speechRecognizer by remember {
        mutableStateOf<SpeechRecognizer?>(null)
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            status = "इस फोन में Speech Recognition उपलब्ध नहीं है।"
            return
        }

        val recognizer = if (offlineMode) {
            if (Build.VERSION.SDK_INT >= 31 &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
            ) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                status = "Offline Speech Recognition उपलब्ध नहीं है। भाषा पैक डाउनलोड करें या Online मोड चुनें।"
                return
            }
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

        speechRecognizer?.destroy()
        speechRecognizer = recognizer

        recognizer.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                status = "अब बोलना शुरू करें..."
                isListening = true
            }

            override fun onBeginningOfSpeech() {
                status = "आपकी आवाज सुनी जा रही है..."
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                status = "आवाज प्रोसेस हो रही है..."
            }

            override fun onError(error: Int) {
                isListening = false
                status = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "माइक्रोफोन में समस्या।"
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "नेटवर्क की समस्या। इंटरनेट जाँचें।"
                    SpeechRecognizer.ERROR_NO_MATCH ->
                        "आवाज से टेक्स्ट नहीं मिला। दोबारा कोशिश करें।"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        "कोई आवाज नहीं मिली। दोबारा बोलें।"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "माइक्रोफोन की अनुमति नहीं मिली।"
                    else -> "Speech Recognition में समस्या। फिर कोशिश करें।"
                }
                speechRecognizer?.destroy()
                speechRecognizer = null
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )

                val result = matches?.firstOrNull().orEmpty()

                if (result.isNotBlank()) {
                    recognizedText = if (recognizedText.isBlank()) {
                        result
                    } else {
                        recognizedText.trimEnd() + "\n" + result
                    }
                    status = "टेक्स्ट सफलतापूर्वक मिला।"
                } else {
                    status = "कोई टेक्स्ट नहीं मिला।"
                }

                isListening = false
                speechRecognizer?.destroy()
                speechRecognizer = null
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            if (offlineMode) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        }

        try {
            isListening = true
            status = "Speech Recognition शुरू हो रहा है..."
            recognizer.startListening(intent)
        } catch (e: Exception) {
            isListening = false
            status = "शुरू नहीं हो सका: ${e.message}"
            recognizer.destroy()
            speechRecognizer = null
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startListening()
            } else {
                status = "माइक्रोफोन की अनुमति जरूरी है।"
            }
        }

    val saveLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/plain")
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    val output = context.contentResolver.openOutputStream(uri)
                        ?: throw Exception("फाइल नहीं खुल सकी")

                    output.use {
                        it.write(recognizedText.toByteArray(Charsets.UTF_8))
                    }
                    status = "TXT फाइल सेव हो गई।"
                } catch (e: Exception) {
                    status = "फाइल सेव नहीं हुई: ${e.message}"
                }
            }
        }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
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
            "Speech to Text",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text("बोलकर हिंदी या English में टेक्स्ट लिखें।")

        Text(
            "भाषा चुनें",
            style = MaterialTheme.typography.titleMedium
        )

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            RadioButton(
                selected = language == "hi-IN",
                onClick = { language = "hi-IN" },
                enabled = !isListening
            )
            Text("हिंदी")

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = language == "en-IN",
                onClick = { language = "en-IN" },
                enabled = !isListening
            )
            Text("English")
        }

        Text(
            "Recognition Mode",
            style = MaterialTheme.typography.titleMedium
        )

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            RadioButton(
                selected = offlineMode,
                onClick = { offlineMode = true },
                enabled = !isListening
            )
            Text("Offline")

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = !offlineMode,
                onClick = { offlineMode = false },
                enabled = !isListening
            )
            Text("Online")
        }

        Button(
            onClick = {
                if (isListening) {
                    speechRecognizer?.stopListening()
                } else {
                    val permissionGranted =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                    if (permissionGranted) {
                        startListening()
                    } else {
                        permissionLauncher.launch(
                            Manifest.permission.RECORD_AUDIO
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = true
        ) {
            Text(
                if (isListening) "सुनना बंद करें"
                else "🎤 बोलना शुरू करें"
            )
        }

        if (status.isNotBlank()) {
            Text(status)
        }

        Text(
            "आपका टेक्स्ट",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = recognizedText,
            onValueChange = { recognizedText = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            label = { Text("यहाँ आपका टेक्स्ट आएगा") }
        )

        if (recognizedText.isNotBlank()) {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as android.content.ClipboardManager

                    clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText(
                            "Speech Text",
                            recognizedText
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
                    saveLauncher.launch("Speech_Text.txt")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("TXT फाइल में सेव करें")
            }

            OutlinedButton(
                onClick = {
                    recognizedText = ""
                    status = "टेक्स्ट साफ हो गया।"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("टेक्स्ट साफ करें")
            }
        }
    }
}
