
package com.kaamigox.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.SecureRandom
import kotlin.math.roundToInt

@Composable
fun PasswordGeneratorTool() {
    val clipboard = LocalClipboardManager.current
    val random = remember { SecureRandom() }

    var length by remember { mutableFloatStateOf(16f) }
    var uppercase by remember { mutableStateOf(true) }
    var lowercase by remember { mutableStateOf(true) }
    var numbers by remember { mutableStateOf(true) }
    var symbols by remember { mutableStateOf(true) }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lowerChars = "abcdefghijklmnopqrstuvwxyz"
    val numberChars = "0123456789"
    val symbolChars = "!@#$%^&*()-_=+[]{}?"

    val selectedPools = buildList {
        if (uppercase) add(upperChars)
        if (lowercase) add(lowerChars)
        if (numbers) add(numberChars)
        if (symbols) add(symbolChars)
    }

    fun generatePassword() {
        if (selectedPools.isEmpty()) {
            password = ""
            message = "कम से कम एक विकल्प चुनें"
            return
        }

        val allChars = selectedPools.joinToString("")
        val result = mutableListOf<Char>()

        selectedPools.forEach { pool ->
            result.add(pool[random.nextInt(pool.length)])
        }

        while (result.size < length.roundToInt()) {
            result.add(allChars[random.nextInt(allChars.length)])
        }

        for (i in result.lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = result[i]
            result[i] = result[j]
            result[j] = temp
        }

        password = result.joinToString("")
        message = "Password तैयार है!"
    }

    val strength = when {
        password.isEmpty() -> "अभी Password बनाएँ"
        password.length >= 20 && selectedPools.size >= 3 -> "बहुत मजबूत"
        password.length >= 14 && selectedPools.size >= 2 -> "मजबूत"
        password.length >= 10 -> "मध्यम"
        else -> "कमजोर"
    }

    val strengthColor = when (strength) {
        "बहुत मजबूत" -> Color(0xFF168A45)
        "मजबूत" -> Color(0xFF258A55)
        "मध्यम" -> Color(0xFFE58A00)
        "कमजोर" -> Color(0xFFD32F2F)
        else -> MaterialTheme.colorScheme.secondary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Password Generator",
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            "अपना मजबूत और सुरक्षित Password बनाएँ",
            fontSize = 15.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3E8)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Password Length", fontSize = 18.sp)

                Text(
                    "${length.roundToInt()} Characters",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Slider(
                    value = length,
                    onValueChange = { length = it.roundToInt().toFloat() },
                    valueRange = 8f..64f,
                    steps = 55
                )
            }
        }

        Text("Password Options", fontSize = 20.sp)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(
                        checked = uppercase,
                        onCheckedChange = { uppercase = it }
                    )
                    Text("Capital Letters (A-Z)")
                }

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(
                        checked = lowercase,
                        onCheckedChange = { lowercase = it }
                    )
                    Text("Small Letters (a-z)")
                }

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(
                        checked = numbers,
                        onCheckedChange = { numbers = it }
                    )
                    Text("Numbers (0-9)")
                }

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(
                        checked = symbols,
                        onCheckedChange = { symbols = it }
                    )
                    Text("Special Symbols (!@#$)")
                }
            }
        }

        Button(
            onClick = { generatePassword() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("Generate Password", fontSize = 18.sp)
        }

        if (password.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Your Password", fontSize = 18.sp)

                    Text(
                        password,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        "Strength: $strength",
                        color = strengthColor,
                        fontSize = 16.sp
                    )

                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(password))
                            message = "Password Copy हो गया!"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copy Password")
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

        Text(
            "सुझाव: अपने Password को किसी के साथ साझा न करें।",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
