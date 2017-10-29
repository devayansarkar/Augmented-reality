package com.ar.dev.simplear

import android.graphics.PixelFormat
import android.hardware.Camera
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import android.view.SurfaceHolder.Callback
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.ar.dev.simplear.app.CurrentAzimuth
import com.ar.dev.simplear.app.CurrentLocation
import com.ar.dev.simplear.app.OnAzimuthChangedListener
import com.ar.dev.simplear.app.OnLocationChangedListener
import com.ar.dev.simplear.models.POI
import java.io.IOException

class MainActivity : AppCompatActivity(), Callback, OnLocationChangedListener, OnAzimuthChangedListener {



    private var mCamera: Camera? = null
    private var mSurfaceHolder: SurfaceHolder? = null
    private var isCameraviewOn = false
    private var mPoi: POI? = null

    private var mAzimuthReal = 0.0
    private var mAzimuthTheoretical = 0.0
    private var AZIMUTH_ACCURACY = 5.0
    private var mMyLatitude = 0.0
    private var mMyLongitude = 0.0

    private var myCurrentAzimuth: CurrentAzimuth? = null
    private var myCurrentLocation: CurrentLocation? = null

    var descriptionTextView: TextView? = null
    var pointerIcon: ImageView? = null



    override fun onAzimuthChanged(azimuthFrom: Float, azimuthTo: Float) {
        mAzimuthReal = azimuthTo.toDouble()
        mAzimuthTheoretical = calculateTeoreticalAzimuth()

        pointerIcon = findViewById<ImageView>(R.id.icon) as ImageView
        val minAngle = calculateAzimuthAccuracy(mAzimuthTheoretical)[0]
        val maxAngle = calculateAzimuthAccuracy(mAzimuthTheoretical)[1]

        if (isBetween(minAngle, maxAngle, mAzimuthReal)) {
            pointerIcon!!.visibility = View.VISIBLE
        } else {
            pointerIcon!!.visibility = View.INVISIBLE
        }

        updateDescription()
    }

    override fun onLocationChanged(currentLocation: Location) {
        mMyLatitude = currentLocation.latitude
        mMyLongitude = currentLocation.longitude
        mAzimuthTheoretical = calculateTeoreticalAzimuth()
        Toast.makeText(this, "latitude: " + currentLocation.latitude + " longitude: " + currentLocation.longitude, Toast.LENGTH_SHORT).show()
        updateDescription()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

        if (isCameraviewOn) {
            mCamera!!.stopPreview()
            isCameraviewOn = false
        }

        if (mCamera != null) {
            try {
                mCamera!!.setPreviewDisplay(mSurfaceHolder)
                mCamera!!.startPreview()
                isCameraviewOn = true
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mCamera!!.stopPreview()
        mCamera!!.release()
        mCamera = null
        isCameraviewOn = false
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mCamera = Camera.open()
        mCamera!!.setDisplayOrientation(90)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupListeners()
        initiateViews()
        createPOI()

    }


    private fun initiateViews() {

        descriptionTextView = findViewById<TextView>(R.id.cameraTextview) as TextView
        window.setFormat(PixelFormat.UNKNOWN)
        val surfaceView = findViewById<SurfaceView>(R.id.cameraview) as SurfaceView
        mSurfaceHolder = surfaceView.holder
        mSurfaceHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        mSurfaceHolder?.addCallback(this@MainActivity)


    }

    private fun createPOI() {
        mPoi = POI(
                "Johannes Calvijnlaan",
                "Johannes Calvijnlaan, Amstelveen",
                52.2956846,
                4.8615233
        )
    }

    private fun setupListeners() {
        myCurrentLocation = CurrentLocation(this)
        myCurrentLocation!!.buildGoogleApiClient(this)
        myCurrentLocation!!.start()
        myCurrentAzimuth = CurrentAzimuth(this, this)
        myCurrentAzimuth!!.start()
    }

    fun calculateTeoreticalAzimuth(): Double {
        val dX = mPoi!!.mLatitude - mMyLatitude
        val dY = mPoi!!.mLongitude - mMyLongitude

        var phiAngle: Double
        val tanPhi: Double

        tanPhi = Math.abs(dY / dX)
        phiAngle = Math.atan(tanPhi)
        phiAngle = Math.toDegrees(phiAngle)

        if (dX > 0 && dY > 0) {
            return phiAngle
        } else if (dX < 0 && dY > 0) {
            return 180 - phiAngle
        } else if (dX < 0 && dY < 0) {
            return 180 + phiAngle
        } else if (dX > 0 && dY < 0) {
            return 360 - phiAngle
        }

        return phiAngle
    }

    private fun updateDescription() {
        descriptionTextView!!.text = (mPoi!!.mName + " azimuthTeoretical "
                + mAzimuthTheoretical + " azimuthReal " + mAzimuthReal + " latitude "
                + mMyLatitude + " longitude " + mMyLongitude)
    }

    private fun isBetween(minAngle: Double, maxAngle: Double, azimuth: Double): Boolean {
        if (minAngle > maxAngle) {
            if (isBetween(0.0, maxAngle, azimuth) && isBetween(minAngle, 360.0, azimuth))
                return true
        } else {
            if (azimuth > minAngle && azimuth < maxAngle)
                return true
        }
        return false
    }

    private fun calculateAzimuthAccuracy(azimuth: Double): List<Double> {
        var minAngle = azimuth - AZIMUTH_ACCURACY
        var maxAngle = azimuth + AZIMUTH_ACCURACY
        val minMax = ArrayList<Double>()

        if (minAngle < 0)
            minAngle += 360.0

        if (maxAngle >= 360)
            maxAngle -= 360.0

        minMax.clear()
        minMax.add(minAngle)
        minMax.add(maxAngle)

        return minMax
    }

    override fun onStop() {
        myCurrentAzimuth!!.stop()
        myCurrentLocation!!.stop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        myCurrentAzimuth!!.start()
        myCurrentLocation!!.start()
    }
}
