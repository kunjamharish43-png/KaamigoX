
package com.kaamigox.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorTool() {
    var display by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var firstNumber by remember { mutableStateOf(0.0) }
    var operator by remember { mutableStateOf("") }
    var newNumber by remember { mutableStateOf(false) }

    fun press(value: String) {
        when (value) {
            "C" -> {
                display = "0"
                expression = ""
                firstNumber = 0.0
                operator = ""
                newNumber = false
            }

            "⌫" -> {
                if (display != "Error") {
                    display = if (display.length > 1) {
                        display.dropLast(1)
                    } else "0"
                }
            }

            "+", "-", "×", "÷" -> {
                firstNumber = display.toDoubleOrNull() ?: 0.0
                operator = value
                expression = "$display $value"
                newNumber = true
            }

            "=" -> {
                val second = display.toDoubleOrNull() ?: 0.0

                val result = when (operator) {
                    "+" -> firstNumber + second
                    "-" -> firstNumber - second
                    "×" -> firstNumber * second
                    "÷" -> if (second != 0.0) firstNumber / second else null
                    else -> second
                }

                expression = if (operator.isNotEmpty()) {
                    "$expression $second ="
                } else {
                    "$second ="
                }

                display = result?.let {
                    if (it % 1.0 == 0.0) it.toLong().toString()
                    else it.toString()
                } ?: "Error"

                operator = ""
                newNumber = true
            }

            "." -> {
                if (newNumber || display == "Error") {
                    if (operator.isEmpty()) expression = ""
                    display = "0."
                    newNumber = false
                } else if (!display.contains(".")) {
                    display += "."
                }
            }

            else -> {
                if (display == "Error" || newNumber || display == "0") {
                    if (operator.isEmpty()) expression = ""
                    display = value
                    newNumber = false
                } else {
                    display += value
                }
            }
        }
    }

    val rows = listOf(
        listOf("C", "⌫", "÷", "×"),
        listOf("7", "8", "9", "-"),
        listOf("4", "5", "6", "+"),
        listOf("1", "2", "3", "="),
        listOf("0", ".")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Calculator",
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = display,
                fontSize = 42.sp
            )
        }

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Button(
                        onClick = { press(key) },
                        modifier = Modifier
                            .weight(if (row.size == 2) 2f else 1f)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (
                                key in listOf("+", "-", "×", "÷", "=")
                            ) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(key, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}
