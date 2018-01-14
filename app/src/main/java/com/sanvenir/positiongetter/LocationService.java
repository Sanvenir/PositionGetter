package com.sanvenir.positiongetter;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service {
    private static final String TAG = LocationService.class.getSimpleName();

    private LocationManager locationManager;
    private String locationProvider;
    private Timer mTimer;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    private void showLocation(Location location) {
        Log.d(TAG, "===================>" + location);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        if(providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if(providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            Log.d(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!No location provider available");
            return;
        }
        Log.d(TAG, "==========================>location manager: " + locationManager);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Log.d(TAG, "==========================>Location setup complete");
        locationManager.requestLocationUpdates(locationProvider, 3000, 0.1f, locationListener);

    }

    public static String bindParams(Map<String, String> paramMap){
        StringBuilder builder = new StringBuilder();
        builder.append("?");
        for(String key : paramMap.keySet()) {
            builder.append(key);
            builder.append("=");
            builder.append(paramMap.get(key));
            builder.append("&");
        }
        return builder.toString();
    }

    public void startTimer() {
        if(mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("Timer Thread", "========================>Running");
                if(ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.d("Timer Thread", "no permission");
                    return;
                }
                Location location = locationManager.getLastKnownLocation(locationProvider);
                Log.d("Timer Thread", String.valueOf(location));
                if(location != null) {
                    try {
                        Map<String, String> paramMap = new HashMap<>();
                        paramMap.put("method", "upload");
                        paramMap.put("la", String.valueOf(location.getLatitude()));
                        paramMap.put("lo", String.valueOf(location.getLongitude()));
                        paramMap.put("el", String.valueOf(location.getAltitude()));
                        String response = HttpMethod.postMethod(paramMap);
                        Log.d("Response", response);
                    } catch(Exception e) {
                        Log.d(TAG, e.toString());
                    }
                }
            }
        }, 1000, 5000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification.Builder builder = new Notification.Builder(this);
        Intent nfIntent = new Intent(this, MapsActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Geo Service")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Getting your position")
                .setWhen(System.currentTimeMillis());
        Notification notification = builder.build();
        startForeground(110, notification);
        startTimer();
        return super.onStartCommand(intent, flags, startId);
    }

    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent i = new Intent(context, LocationService.class);
            context.startService(i);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}
