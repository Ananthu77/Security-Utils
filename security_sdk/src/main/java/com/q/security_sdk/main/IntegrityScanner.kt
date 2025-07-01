package com.q.security_sdk.main

import android.content.Context
import com.q.security_sdk.security.HookDetection
import com.q.security_sdk.security.EmulatorCheck
import com.q.security_sdk.IntegrityCallback
import com.q.security_sdk.security.RootCheck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IntegrityScanner private constructor(
    private val context: Context,
    private val enableRootCheck: Boolean,
    private val enableEmulatorCheck: Boolean,
    private val enableRuntimeHookCheck: Boolean,
    private val callback: IntegrityCallback
) {

    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            val pm = context.packageManager
            val result = DeviceIntegrityCheck()

            if (enableRootCheck) {
                result.isRooted = RootCheck.isDeviceRooted(pm)
            }
            if (enableEmulatorCheck) {
                result.isEmulator = EmulatorCheck.isEmulator(context)
            }
            if (enableRuntimeHookCheck) {
                result.isRuntimeHooked = HookDetection.isMaliciousDevice(pm)
                result.hasBypassTools = result.isRuntimeHooked
            }
            CoroutineScope(Dispatchers.Main).launch {
                callback.onResult(result)
            }
        }
    }

    private fun isCompromised(result: DeviceIntegrityCheck): Boolean =
        result.isRooted || result.isEmulator || result.isRuntimeHooked

    class Builder(private val context: Context) {
        private var enableRootCheck = false
        private var enableEmulatorCheck = false
        private var enableRuntimeHookCheck = false
        private lateinit var callback: IntegrityCallback

        fun enableRootCheck(enable: Boolean) = apply { this.enableRootCheck = enable }
        fun enableEmulatorCheck(enable: Boolean) = apply { this.enableEmulatorCheck = enable }
        fun enableRuntimeHookCheck(enable: Boolean) = apply { this.enableRuntimeHookCheck = enable }

        fun setCallBack(callback: IntegrityCallback) = apply { this.callback = callback }

        fun build(): IntegrityScanner = IntegrityScanner(
            context,
            enableRootCheck,
            enableEmulatorCheck,
            enableRuntimeHookCheck,
            callback
        )

    }
}