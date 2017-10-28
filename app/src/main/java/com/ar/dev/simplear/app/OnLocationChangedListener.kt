package com.ar.dev.simplear.app

import android.location.Location


interface OnLocationChangedListener {
    fun onLocationChanged(currentLocation: Location)
}