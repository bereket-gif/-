package com.example.util

object GeezUtil {
    private val GEEZ_ONES = arrayOf("", "፩", "፪", "፫", "፬", "፭", "፮", "፯", "፰", "፱")
    private val GEEZ_TENS = arrayOf("", "፲", "፳", "፴", "፵", "፭", "፷", "፯", "፹", "፺")

    fun toGeezNumeral(number: Int): String {
        if (number <= 0) return number.toString()
        if (number < 10) return GEEZ_ONES[number]
        if (number < 100) {
            val tens = number / 10
            val ones = number % 10
            return GEEZ_TENS[tens] + GEEZ_ONES[ones]
        }
        val hundreds = number / 100
        val remainder = number % 100
        val hundredStr = if (hundreds > 1) toGeezNumeral(hundreds) + "፪" else "፪" // ፪ is 100
        return if (remainder > 0) hundredStr + toGeezNumeral(remainder) else hundredStr
    }
}
