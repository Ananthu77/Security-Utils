package com.q.security_sdk.main

data class DeviceIntegrityCheck(
    var isRooted: Boolean = false,
    var isEmulator: Boolean = false,
    var hasBypassTools: Boolean = false,
    var isRuntimeHooked: Boolean = false
)
