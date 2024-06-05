package com.example.transactions.util

import java.text.NumberFormat
import java.util.Locale

fun formatToIDR(balance: Number): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return formatter.format(balance)
}