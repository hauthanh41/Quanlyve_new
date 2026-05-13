package com.example.qlydatve.utils

object Validator {

    fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPhone(phone: String): Boolean =
        phone.matches(Regex("^(0|\\+84)[0-9]{9,10}$"))

    fun isValidUsername(username: String): Boolean =
        username.length in 4..20 && username.matches(Regex("^[a-zA-Z0-9_]+$"))

    fun isValidPassword(password: String): Boolean =
        password.length >= 6

    fun isNotEmpty(value: String): Boolean = value.isNotBlank()
}
