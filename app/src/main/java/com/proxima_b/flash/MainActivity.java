package com.proxima_b.flash;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.w3c.dom.Text;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    boolean isServiceActive;
    ImageView btn_switch;
    TextView txtv;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private boolean hasFlash;
    private boolean isFlashOn;
    private long lastUpdate = 0;
    private long oldtime = 0;
    private float last_x, last_y, last_z;
    private static  int SHAKE_THRESHOLD = 500;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        // First check if device is supporting flashlight or not
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);


        btn_switch = (ImageView) findViewById(R.id.btnswitch);
        txtv = (TextView) findViewById(R.id.textView);

        btn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFlashOn) {
                    isFlashOn=false;
                    btn_switch.setImageResource(R.drawable.btn_switch_off);
                    txtv.setText("Torch Status: Off");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                        String cameraId = null;
                        try {
                            cameraId = camManager.getCameraIdList()[0];
                            camManager.setTorchMode(cameraId, false);   //Turn ON
                        } catch (CameraAccessException e) {
                            Toast.makeText(MainActivity.this,String.valueOf(e),Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                } else {
                    // turn on flash
                    btn_switch.setImageResource(R.drawable.btn_switch_on);
                    txtv.setText("Torch Status: On");
                    isFlashOn=true;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                        String cameraId = null;
                        try {
                            cameraId = camManager.getCameraIdList()[0];
                            camManager.setTorchMode(cameraId, true);   //Turn ON
                        } catch (CameraAccessException e) {
                            Toast.makeText(MainActivity.this,String.valueOf(e),Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }
            }


        });
        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                }
            });
            alert.show();
            return;
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
                            btn_switch.setImageResource(R.drawable.btn_switch_off);
                            txtv.setText("Torch Status: Off");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                                String cameraId = null;
                                try {
                                    cameraId = camManager.getCameraIdList()[0];
                                    camManager.setTorchMode(cameraId, false);   //Turn ON
                                } catch (CameraAccessException e) {
                                    Toast.makeText(MainActivity.this,String.valueOf(e),Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            // turn on flash
                            btn_switch.setImageResource(R.drawable.btn_switch_on);
                            txtv.setText("Torch Status: On");
                            isFlashOn=true;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                                String cameraId = null;
                                try {
                                    cameraId = camManager.getCameraIdList()[0];
                                    camManager.setTorchMode(cameraId, true);   //Turn ON
                                } catch (CameraAccessException e) {
                                    Toast.makeText(MainActivity.this,String.valueOf(e),Toast.LENGTH_LONG).show();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_start) {

            startService();
        }
        if (id == R.id.action_stop) {

            stopService();

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();

        // on starting the app get the camera params

    }
    public void startService() {
            Intent serviceIntent = new Intent(this, FlashService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, FlashService.class);
        stopService(serviceIntent);
    }
}

