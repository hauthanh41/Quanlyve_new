package com.example.qlydatve.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm"

    fun getCurrentDate(): String =
        SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())

    fun getCurrentDateTime(): String =
        SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault()).format(Date())

    fun formatDate(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val display = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            display.format(sdf.parse(dateStr) ?: return dateStr)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatDateTime(dateTimeStr: String): String {
        return try {
            val sdf = SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault())
            val display = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
            display.format(sdf.parse(dateTimeStr) ?: return dateTimeStr)
        } catch (e: Exception) {
            dateTimeStr
        }
    }
}
