package com.example.smartzone.helpers

class PasswordValidator {
    companion object {
        fun isValid(password: String): Boolean {
            return password.length >= 8 &&
                    password.any { it.isUpperCase() } &&
                    password.any { it.isLowerCase() } &&
                    password.any {
                        it.isDigit()
                    }
        }
    }
}