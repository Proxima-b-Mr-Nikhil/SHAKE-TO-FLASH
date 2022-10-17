package com.proxima_b.flash;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class FlashService extends Service implements SensorEventListener {
    public static final String CHANNEL_ID ="Shake to Tourch";
    Context context;
    private boolean isFlashOn;
    private long lastUpdate = 0;
    private long oldtime = 0;
    private float last_x, last_y, last_z;
    private static  int SHAKE_THRESHOLD = 500;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {



        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.icon24)
                .setContentText("Shake to Tourch is active")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.icon24,"Navigate to app",
                        pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {

        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Shake to Tourch",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            serviceChannel.enableVibration(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(serviceChannel);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;


                //double speed = (Math.abs( y -last_y ))/ diffTime ;
                double speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;


                speed = (long) (speed * 10000) / 10000.0;
                if (speed>SHAKE_THRESHOLD) {
                    // get the camera

                    long newtime = System.currentTimeMillis();
                    if(newtime-oldtime>2000) {

                        if (isFlashOn) {
                            isFlashOn=false;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                                String cameraId = null;
                                try {
                                    cameraId = camManager.getCameraIdList()[0];
                                    camManager.setTorchMode(cameraId, false);   //Turn ON
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            isFlashOn=true;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                                String cameraId = null;
                                try {
                                    cameraId = camManager.getCameraIdList()[0];
                                    camManager.setTorchMode(cameraId, true);   //Turn ON
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        oldtime = newtime;
                    }
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}


