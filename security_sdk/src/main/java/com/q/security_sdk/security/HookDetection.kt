package com.q.security_sdk.security

import android.content.pm.PackageManager
import android.util.Log
import java.io.File

object HookDetection {

    fun isMaliciousDevice(pm: PackageManager): Boolean = checkBypassingTools(pm)
            || checkFridaServer()
            || checkFridaGadget()
            || checkTracePid()
            || isSELinuxPermissive()
            || checkLoadedLibraries()
            || checkRunningProcess()


    private fun checkBypassingTools(pm: PackageManager): Boolean {
        val packages = arrayOf(
            "com.chelpus.lackypatch",
            "com.damonetech.gameguardian",
            "com.hexview.android.cheatengine",
            "org.lsposed.manager",
            "com.topjohnwu.magisk",
            "de.robv.android.xposed.installer",
            "io.github.lsposed.lsposed"
        )

        for (pkg in packages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (e: Exception) {
                Log.w("security", "Tools check failed: ${e.message}")
            }
        }
        return false
    }

    private fun checkFridaServer(): Boolean {
        val ports = arrayOf(27042, 27043)
        for (port in ports) {
            try {
                val process = Runtime.getRuntime().exec("netstat -an | grep $port")
                val input = process.inputStream.bufferedReader().readLine()
                if (input != null && input.contains(port.toString())) {
                    return true
                }
            } catch (e: Exception) {
                Log.w("security", "Server checking failed: ${e.message}")
            }
        }
        return false
    }

    private fun checkFridaGadget(): Boolean {
        val fridaClasses = arrayOf(
            "re.frida.Server", "frida.Agent"
        )

        for (clazz in fridaClasses) {
            try {
                Class.forName(clazz)
                return true
            } catch (e: Exception) {
                Log.w("security", "Gadget checking failed: ${e.message}")
            }
        }
        return false
    }

    private fun checkTracePid(): Boolean {
        var isBeingTraced = false
        try {
            val file = File("/proc/self/status")
            if (file.exists()) {
                file.forEachLine { line ->
                    if (line.startsWith("TracerPid:")) {
                        val tracePid = line.split(":")[1].trim().toInt()
                        if (tracePid != 0) {
                            isBeingTraced = true
                            return@forEachLine
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("security", "TracePid checking failed: ${e.message}")
        }
        return isBeingTraced
    }

    private fun isSELinuxPermissive(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("getenforce")
            val result = process.inputStream.bufferedReader().readLine()
            result.equals("Permissive", ignoreCase = true)
        } catch (_: Exception) {
            false
        }
    }

    private fun checkLoadedLibraries(): Boolean {
        var isLibraryLoaded = false
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                mapsFile.forEachLine { line ->
                    if (line.contains("frida") || line.contains("substrate") || line.contains("xposed")) {
                        isLibraryLoaded = true
                        return@forEachLine
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("security", "Failed Checking loaded libraries: ${e.message}")
        }
        return isLibraryLoaded
    }

    private fun checkRunningProcess(): Boolean {
        var processIsRunning = false
        val suspicious = listOf("frida-server", "xposed", "magisk")
        val process = Runtime.getRuntime().exec("ps")
        process.inputStream.bufferedReader().forEachLine {
            if (suspicious.any() { tool -> it.contains(tool, ignoreCase = true)}) {
                processIsRunning = true
                return@forEachLine
            }
        }
        return processIsRunning
    }
}