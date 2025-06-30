package com.q.security_sdk

import com.q.security_sdk.main.DeviceIntegrityCheck

interface IntegrityCallback {
    fun onResult(result: DeviceIntegrityCheck)
}