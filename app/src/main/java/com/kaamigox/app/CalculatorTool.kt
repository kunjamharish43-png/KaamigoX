
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
    var firstNumber by remember { mutableStateOf(0.0) }
    var operator by remember { mutableStateOf("") }
    var newNumber by remember { mutableStateOf(false) }

    fun press(value: String) {
        when (value) {
            "C" -> {
                display = "0"
                firstNumber = 0.0
                operator = ""
                newNumber = false
            }

            "⌫" -> {
                display = if (display.length > 1) {
                    display.dropLast(1)
                } else "0"
            }

            "+", "-", "×", "÷" -> {
                firstNumber = display.toDoubleOrNull() ?: 0.0
                operator = value
                newNumber = true
            }

            "=" -> {
                val second = display.toDoubleOrNull() ?: 0.0

                val result = when (operator) {
                    "+" -> firstNumber + second
                    "-" -> firstNumber - second
                    "×" -> firstNumber * second
                    "÷" -> if (second != 0.0) {
                        firstNumber / second
                    } else null
                    else -> second
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
                    display = "0."
                    newNumber = false
                } else if (!display.contains(".")) {
                    display += "."
                }
            }

            else -> {
                if (display == "Error" || newNumber || display == "0") {
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
        listOf("0", "0", ".", "=")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Calculator",
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomEnd
        ) {
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
                row.forEachIndexed { index, key ->
                    val isZeroDuplicate =
                        row == rows.last() && index == 1
                    if (!isZeroDuplicate) {
                        Button(
                            onClick = { press(key) },
                            modifier = Modifier
                                .weight(if (key == "0" && row == rows.last()) 2f else 1f)
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
}
