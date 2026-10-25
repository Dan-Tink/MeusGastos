package com.meusgastos.app

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

val PtBR: Locale = Locale("pt", "BR")

fun Long.toMoney(): String = NumberFormat.getCurrencyInstance(PtBR).format(this / 100.0)

fun Long.toPlain(): String = String.format(PtBR, "%.2f", this / 100.0)

/** Converte "4,50", "4.50" ou "1.234,50" em centavos. Retorna null se inválido ou zero. */
fun parseCents(input: String): Long? {
    var t = input.trim().replace("R$", "").replace(" ", "")
    if (t.isEmpty()) return null
    if (t.contains(',')) t = t.replace(".", "").replace(',', '.')
    val value = t.toBigDecimalOrNull() ?: return null
    val cents = value.multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toLong()
    return if (cents > 0) cents else null
}

fun LocalDate.fmt(): String = format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

fun YearMonth.label(): String =
    month.getDisplayName(TextStyle.FULL, PtBR).replaceFirstChar { it.uppercase() } + " " + year
