package com.q.security_sdk

import android.content.Context
import android.location.LocationManager
import android.provider.Settings

object MockLocation {

    fun isMockLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ALLOW_MOCK_LOCATION) != "0"
    }

}