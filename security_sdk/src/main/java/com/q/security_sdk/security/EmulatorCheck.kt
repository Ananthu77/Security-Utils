package com.q.security_sdk.security

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.q.security_sdk.anyFileExists
import com.q.security_sdk.getSystemProperty
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

        return buildProps.any { prop ->
            emulatorIndicators.any { indicator ->
                prop.contains(indicator, ignoreCase = true)
            }
        } || Build.MANUFACTURER.equals(
            "Genymotion", true
        ) || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))

    }

    private fun checkTelephony(context: Context): Boolean {
        try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val networkOperatorName = telephonyManager.networkOperatorName
            if (networkOperatorName.equals("android", ignoreCase = true)) {
                return true
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val imei = telephonyManager.deviceId
                if (imei.isNullOrBlank() || imei == "000000000000000") return true
            }
        } catch (e: Exception) {
        }
        return false
    }

    private fun checkQEmuProps(): Boolean {
        val props = listOf(
            "ro.kernel.qemu", "ro.bootloader", "ro.boot.hardware", "ro.hardware"
        )
        return props.any {
            val value = getSystemProperty(it)
            value?.contains("qemu", ignoreCase = true) == true || value?.contains(
                "goldfish",
                true
            ) == true || value?.contains("ranchu", true) == true
        }
    }


    private fun checkPipes(): Boolean =
        anyFileExists(arrayOf("/dev/socket/qemud", "/dev/qemu_pipe"))

    private fun checkFiles(): Boolean = anyFileExists(
        arrayOf(
            "/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace", "/system/bin/qemu-props"
        )
    )

    private fun checkIp(): Boolean {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            interfaces.any { intf ->
                Collections.list(intf.inetAddresses).any { addr ->
                    val ip = addr.hostAddress ?: return@any false
                    ip.startsWith("10.0.2.") || ip.startsWith("10.0.3.")
                }
            }
        } catch (e: Exception) {
            false
        }
    }

}