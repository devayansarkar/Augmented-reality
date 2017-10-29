package com.ar.dev.simplear.app


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class CurrentLocation(private var onLocationChangedListener: OnLocationChangedListener) : ConnectionCallbacks, OnConnectionFailedListener, LocationListener {


    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e("AR-App", "Location services connection failed with code " + connectionResult.errorCode)
    }

    @SuppressLint("MissingPermission")
    override fun onLocationChanged(location: Location?) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        onLocationChangedListener.onLocationChanged(mLastLocation)

    }

    @SuppressLint("MissingPermission")
    override fun onConnected(bundle: Bundle?) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        onLocationChangedListener.onLocationChanged(mLastLocation)

    }

    @Synchronized fun buildGoogleApiClient(context: Context) {
        mGoogleApiClient = GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval((10 * 1000).toLong())
                .setFastestInterval((1 * 1000).toLong())
    }

    fun start() {
        mGoogleApiClient.connect()
    }

    fun stop() {
        mGoogleApiClient.disconnect()
    }

    override fun onConnectionSuspended(p0: Int) {}
}