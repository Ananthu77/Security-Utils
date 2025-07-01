package com.q.security_sdk.security

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.q.security_sdk.getSystemProperty
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.collections.iterator

object RootCheck {

    fun isDeviceRooted(pm: PackageManager): Boolean = checkBuildTags()
            || checkSuBinary()
            || checkDangerousFiles()
            || checkDangerousPackages(pm)
            || checkBusyBoxBinary()
            || checkSystemProperties()
            || checkMagisk()
            || checkXposed()

    private fun checkBuildTags(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/bin/",
            "/system/xbin/",
            "/sbin/",
            "/system/sd/xbin/",
            "/system/usr/we-need-root/",
            "/system/development/",
            "/data/local/xbin/",
            "/data/local/bin/",
            "/data/local/"
        )
        return paths.map { File(it, "su") }.any { it.exists() && it.canExecute() }
    }

    private fun checkDangerousFiles(): Boolean {
        val files = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/daemonsu",
            "/system/etc/init.d/99SuperSUDaemon",
            "/system/bin/.ext/.su",
            "/system/usr/su-backup",
            "/system/bin/magisk",
            "/sbin/magisk"
        )

        return files.map { File(it) }.any { it.exists() }
    }

    private fun checkDangerousPackages(pm: PackageManager): Boolean {
        val packages = arrayOf(
            "com.noshufou.android.su",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk",
            "de.robv.android.xposed.installer",
            "com.devadvance.rootcloak",
            "com.zachspong.temprootremovejb",
            "com.ramdroid.appquarantine"
        )

        for (pkg in packages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (e: Exception) {
                Log.w("security", "Package check failed: ${e.message}")
            }
        }
        return false
    }

    private fun checkBusyBoxBinary(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "busybox"))
            val input = process.inputStream
            val reader = BufferedReader(InputStreamReader(input))
            reader.readLine() != null
        } catch (_: Exception) {
            false
        }
    }

    private fun checkSystemProperties(): Boolean {
        val props = mapOf(
            "ro.debuggable" to "1", "ro.secure" to "0"
        )
        return props.any { (key, value) ->
            getSystemProperty(key) == value
        }
    }

    private fun checkMagisk(): Boolean {
        val paths = arrayOf(
            "/sbin/magisk", "/sbin/.magisk", "/data/adb/magisk", "/data/adb/modules"
        )
        return paths.map { File(it) }.any { it.exists() }
    }

    private fun checkXposed(): Boolean {
        return try {
            Class.forName("de.robv.android.xposed.XposedHelpers")
            true
        } catch (_: Exception) {
            false
        }
    }

}