package com.example.menuapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.menuapplication.databinding.FragmentCalculatorBinding
import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorFragment : Fragment() {
    private lateinit var binding: FragmentCalculatorBinding
    private var currentInput = StringBuilder()
    private var lastCalculation = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalculatorBinding.inflate(inflater, container, false)

        setupButtons()

        binding.buttonClear.setOnClickListener { clearInput() }
        binding.buttonEqual.setOnClickListener { calculateResult() }

        return binding.root
    }

    private fun setupButtons() {
        val operationButtons = setOf(
            binding.buttonAdd, binding.buttonSubtract,
            binding.buttonMultiply, binding.buttonDivide,
            binding.buttonPercent
        )

        val allButtons = listOf(
            binding.button0, binding.button1, binding.button2,
            binding.button3, binding.button4, binding.button5,
            binding.button6, binding.button7, binding.button8,
            binding.button9, binding.buttonDecimal,
            binding.buttonOpenParenthesis, binding.buttonCloseParenthesis
        ) + operationButtons

        allButtons.forEach { button ->
            button.setOnClickListener {
                val value = button.text.toString()
                if (lastCalculation && operationButtons.contains(button)) {
                    currentInput.clear()
                    lastCalculation = false
                }
                appendToInput(value, operationButtons.contains(button) || value == ".")
            }
        }
    }

    private fun appendToInput(str: String, isOperation: Boolean) {
        if (isOperation && currentInput.isNotEmpty()) {
            val lastChar = currentInput.last()
            if (lastChar in listOf('+', '-', '*', '/', '.') || (str == "." && currentInput.contains("."))) {
                return  // Avoid adding an operator or extra decimal if not appropriate
            }
        }
        if (lastCalculation && !isOperation) {
            currentInput.clear()
            lastCalculation = false
        }
        currentInput.append(str)
        updateDisplay(currentInput.toString())
    }

    private fun calculateResult() {
        if (currentInput.isNotEmpty()) {
            try {
                val expression = ExpressionBuilder(currentInput.toString()).build()
                val result = expression.evaluate()

                // Check if the result is an integer
                val resultText = if (result % 1.0 == 0.0) {
                    result.toInt().toString()  // Convert to integer string if no decimal part
                } else {
                    result.toString()  // Keep as is if there's a decimal part
                }

                currentInput.clear()
                currentInput.append(resultText)
                updateDisplay(resultText)
                lastCalculation = true
            } catch (e: Exception) {
                updateDisplay("Error")
                currentInput.clear()
            }
        }
    }


    private fun clearInput() {
        currentInput.clear()
        lastCalculation = false
        updateDisplay("0")
    }

    private fun updateDisplay(text: String) {
        binding.textViewCalculatorOutput.text = text
    }
}
