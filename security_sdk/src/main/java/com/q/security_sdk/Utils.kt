package com.q.security_sdk

import java.io.BufferedReader
import java.io.InputStreamReader

fun getSystemProperty(propName: String): String? {
    return try {
        val process = Runtime.getRuntime().exec("getprop $propName")
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        bufferedReader.readLine()
    } catch (e: Exception) {
        null
    }
}