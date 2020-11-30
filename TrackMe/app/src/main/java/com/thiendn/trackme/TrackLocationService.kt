package com.thiendn.trackme

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class TrackLocationService: Service() {

    private val TAG = this.javaClass.simpleName
    private val CHANNEL_ID = "channel_01"
    private val EXTRA_STARTED_FROM_NOTIFICATION = "abc"
    private val UPDATE_INTERVAL_IN_MILLISECONDS = 10000L
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

    private val mLocationRequest by lazy {
        LocationRequest().apply {
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private val mLocationCallback by lazy {
        object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                Toast.makeText(applicationContext, "thiendn: onLocationResult", Toast.LENGTH_SHORT).show()
                super.onLocationResult(p0)
            }
        }
    }

    private val mFusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var mLocation: Location? = null


    override fun onBind(intent: Intent?): IBinder? {
        return object : Binder(){
        }
    }

    override fun onCreate() {
        super.onCreate()
        println("thiendn111: onCreateService")
        getLastLocation()
    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation.addOnCompleteListener {
                if (it.isSuccessful && it.result != null){
                    mLocation = it.result
                } else {
                    Log.w(TAG, "Fail to get location!")
                }
            }
        } catch (ex: Exception){
            ex.printStackTrace()
        }
    }

    private fun getNotification(): Notification? {
        val intent = Intent(
            this,
            TrackLocationService::class.java
        )

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(
            EXTRA_STARTED_FROM_NOTIFICATION,
            true
        )

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        val servicePendingIntent = PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // The PendingIntent to launch activity.
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), 0
        )
        val builder = NotificationCompat.Builder(this)
            .addAction(R.mipmap.ic_launcher, getString(R.string.app_name), activityPendingIntent)
            .addAction(R.mipmap.ic_launcher, getString(R.string.app_name), servicePendingIntent)
            .setContentText("text")
            .setContentTitle("ccc")
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("text")
            .setWhen(System.currentTimeMillis())

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID) // Channel ID
        }
        return builder.build()
    }
}