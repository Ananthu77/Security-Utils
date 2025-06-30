package com.q.security_sdk

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.util.Collections

object EmulatorCheck {

    fun isEmulator(context: Context): Boolean =
        checkBasicProperties() || checkTelephony(context) || checkQEmuProps() || checkPipes() || checkFiles() || checkIp()


    private fun checkBasicProperties(): Boolean {

        val buildProps = listOf(
            Build.FINGERPRINT,
            Build.MODEL,
            Build.MANUFACTURER,
            Build.BRAND,
            Build.DEVICE,
            Build.PRODUCT,
            Build.HARDWARE,
            Build.BOARD,
            Build.BOOTLOADER
        )

        val emulatorIndicators = listOf(
            "generic",
            "unknown",
            "google_sdk",
            "Emulator",
            "Android SDK built for x86",
            "goldfish",
            "ranchu",
            "sdk",
            "sdk_x86",
            "vbox86p",
            "genymotion"
        )

        for (prop in buildProps) {
            for (indicator in emulatorIndicators) {
                if (prop != null && prop.contains(indicator, ignoreCase = true)) {
                    return true
                }
            }
        }

        if (Build.MANUFACTURER.equals("Genymotion", ignoreCase = true)) {
            return true
        }
        if (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) {
            return true
        }
        return false
    }

    private fun checkTelephony(context: Context): Boolean {
        try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val networkOperatorName = telephonyManager.networkOperatorName
            if (networkOperatorName.equals("android", ignoreCase = true)) {
                return true
            }

            val imei = telephonyManager.deviceId
            if (imei == null || imei.trim().isEmpty() || imei == "000000000000000") {
                return true
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    private fun checkQEmuProps(): Boolean {
        val props = listOf(
            "ro.kernel.qemu", "ro.bootloader", "ro.boot.hardware", "ro.hardware"
        )
        for (prop in props) {
            val value = getSystemProperty(prop)
            if (value != null && (value.contains("goldfish") || value.contains("ranchu") || value.contains(
                    "qemu"
                ))
            ) {
                return true
            }
        }
        return false
    }

    private fun getSystemProperty(propName: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $propName")
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            bufferedReader.readLine()
        } catch (e: Exception) {
            null
        }
    }

    private fun checkPipes(): Boolean {
        val pipes = arrayOf(
            "/dev/socket/qemud", "/dev/qemu_pipe"
        )
        for (pipe in pipes) {
            if (File(pipe).exists()) {
                return true
            }
        }
        return false
    }

    private fun checkFiles(): Boolean {
        val files = arrayOf(
            "/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace", "/system/bin/qemu-props"
        )

        for (file in files) {
            if (File(file).exists()) {
                return true
            }
        }
        return false
    }

    private fun checkIp(): Boolean {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    val ip = addr.hostAddress ?: continue
                    if (ip.startsWith("10.0.2.") || ip.startsWith("10.0.3")) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {

        }
        return false
    }

}