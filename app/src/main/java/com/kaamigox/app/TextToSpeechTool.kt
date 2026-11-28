
package com.kaamigox.app

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun TextToSpeechTool() {
    val context = LocalContext.current

    var text by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("hi") }
    var speed by remember { mutableFloatStateOf(1f) }
    var pitch by remember { mutableFloatStateOf(1f) }
    var status by remember { mutableStateOf("अपना टेक्स्ट लिखें।") }
    var ready by remember { mutableStateOf(false) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { result ->
            ready = result == TextToSpeech.SUCCESS
            status = if (ready) {
                "आवाज तैयार है।"
            } else {
                "Text to Speech इंजन उपलब्ध नहीं है।"
            }
        }

        tts = engine

        onDispose {
            engine.stop()
            engine.shutdown()
            tts = null
        }
    }

    fun speakText() {
        val engine = tts

        if (!ready || engine == null) {
            status = "आवाज अभी तैयार नहीं है।"
            return
        }

        if (text.isBlank()) {
            status = "पहले टेक्स्ट लिखें।"
            return
        }

        val locale = if (language == "hi") {
            Locale("hi", "IN")
        } else {
            Locale("en", "IN")
        }

        val languageResult = engine.setLanguage(locale)

        if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
            languageResult == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            status = "इस भाषा की आवाज उपलब्ध नहीं है। फोन में भाषा पैक डाउनलोड करें।"
            return
        }

        engine.setSpeechRate(speed)
        engine.setPitch(pitch)

        engine.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "KAAMIGO_TTS"
        )

        status = "आवाज चलाई जा रही है।"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Text to Speech",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("टेक्स्ट को आवाज में सुनें।")

        Text("भाषा चुनें", style = MaterialTheme.typography.titleMedium)

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            RadioButton(
                selected = language == "hi",
                onClick = { language = "hi" }
            )
            Text("हिंदी")

            Spacer(Modifier.width(20.dp))

            RadioButton(
                selected = language == "en",
                onClick = { language = "en" }
            )
            Text("English")
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            label = { Text("अपना टेक्स्ट लिखें") },
            placeholder = { Text("यहाँ टेक्स्ट लिखें...") }
        )

        Text(
            "आवाज की Speed: ${String.format("%.1f", speed)}x",
            style = MaterialTheme.typography.titleMedium
        )

        Slider(
            value = speed,
            onValueChange = { speed = it },
            valueRange = 0.5f..2.0f
        )

        Text(
            "आवाज की Pitch: ${String.format("%.1f", pitch)}",
            style = MaterialTheme.typography.titleMedium
        )

        Slider(
            value = pitch,
            onValueChange = { pitch = it },
            valueRange = 0.5f..2.0f
        )

        Button(
            onClick = { speakText() },
            enabled = ready && text.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔊 आवाज सुनें")
        }

        OutlinedButton(
            onClick = {
                tts?.stop()
                status = "आवाज बंद कर दी गई।"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⏹ आवाज बंद करें")
        }

        OutlinedButton(
            onClick = {
                text = ""
                tts?.stop()
                status = "टेक्स्ट साफ कर दिया गया।"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("टेक्स्ट साफ करें")
        }

        Text(status)
    }
}
