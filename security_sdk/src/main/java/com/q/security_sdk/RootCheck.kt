package com.q.security_sdk

import android.content.pm.PackageManager
import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.collections.iterator

object RootCheck {

    fun isDeviceRooted(pm: PackageManager): Boolean =
        checkBuildTags()
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

        for (path in paths) {
            val suFile = File(path + "su")
            if (suFile.exists()) {
                return true
            }
        }
        return false
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

        for (file in files) {
            if (File(file).exists()) {
                return true
            }
        }
        return false
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

            }
        }
        return false
    }

    private fun checkBusyBoxBinary(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "busybox"))
            val input = process.inputStream
            val reader = BufferedReader(InputStreamReader(input))
            reader.readLine() != null
        } catch (e: Exception) {
            false
        }
    }

    private fun checkSystemProperties(): Boolean {
        val props = mapOf(
            "ro.debuggable" to "1",
            "ro.secure" to "0"
        )
        for ((prop, expected) in props) {
            val value = getSystemProperty(prop)
            if (value == expected) {
                return true
            }
        }
        return false
    }

    private fun checkMagisk(): Boolean {
        val paths = arrayOf(
            "/sbin/magisk",
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/data/adb/modules"
        )
        for (path in paths) {
            if (File(path).exists()) {
                return true
            }
        }
        return false
    }

    private fun checkXposed(): Boolean {
        return try {
            val clazz = Class.forName("de.robv.android.xposed.XposedHelpers")
            clazz != null
        } catch (e: Exception) {
            false
        }
    }

}