package com.example.ViralJoshi;

import com.firebase.client.Firebase;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    //GPS
    TextView testViewStatus;
    TextView textViewLatitude;
    TextView textViewLongitude;
    LocationManager myLocationManager;
    String PROVIDER = LocationManager.GPS_PROVIDER;
    long TIME_UPDATES = 1000;
    float DISTANCE_UPDATES = 0;
    boolean LocationAvailable;

    //Sensor
    // define the display assembly compass picture
    private ImageView compass_image;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    //SeekBar
    private SeekBar mTargetSeekBar;
    private TextView mTargetDistance;
    private int currentProcess = 0;

    //Firebase
    Firebase myFirebaseRef;
    //String latitude, longitude;

    //Target
    Target enemyTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testViewStatus = (TextView)findViewById(R.id.status);
        textViewLatitude = (TextView)findViewById(R.id.latitude);
        textViewLongitude = (TextView)findViewById(R.id.longitude);

        //default target Gps
        enemyTarget = new Target(43.3894038, -80.4065549);

        //Initialize the GPS manager
        myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        if (checkPermission()) {
            myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
        } else {
            requestPermission();
        }

        //Sensor
        // our compass image
        compass_image = (ImageView) findViewById(R.id.compass_imageView);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        //SeekBar
        mTargetSeekBar = (SeekBar) findViewById(R.id.targetseekBar);
        mTargetDistance = (TextView) findViewById(R.id.targetdistance);
        mTargetDistance.setText(" " + 0 + "/" + mTargetSeekBar.getMax() + "KM");

        mTargetSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                currentProcess = progresValue;
                //Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //     Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mTargetDistance.setText(" " + mTargetSeekBar.getProgress() + "/" + mTargetSeekBar.getMax() + "KM");
                //  Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
                currentProcess = mTargetSeekBar.getProgress();
            }
        });

        //Setup Firebase on Android
        Firebase.setAndroidContext(this);

     }

    //Sent target button event
    public void setTargetOnClick(View v)
    {
        myFirebaseRef = new Firebase("https://fiery-inferno-8082.firebaseio.com/");
        myFirebaseRef.child("target").setValue(enemyTarget);
    }

    public void showMyLocation(Location l){
        if(l == null){
            testViewStatus.setText("GPS_Status : Could not Get Current GPS Location.");
        }
        else{
            //latitude = Double.toString(l.getLatitude());
            //longitude = Double.toString(l.getLongitude());
            testViewStatus.setText("GPS_Status : Showing Current GPS Location.");
            textViewLatitude.setText("Latitude: " + l.getLatitude());
            textViewLongitude.setText("Longitude: " + l.getLongitude());

            NewGps newgps= GetNewLocation(l.getLatitude(), l.getLongitude(), mCurrentDegree, currentProcess);
            enemyTarget = new Target(newgps.getLatitude(), newgps.getLongitude());
        }
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onLocationChanged(Location location) {
        showMyLocation(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    // @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }


    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){
            LocationAvailable = true;
            return true;
        } else {
            LocationAvailable = false;
            return false;
        }
    }

    private void requestPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this, "This app relies on location data for it's main functionality. Please enable GPS data to access all features.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},999);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 999:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /**
                     * We are good, turn on monitoring
                     */
                    if (checkPermission()) {
                        myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
                    } else {
                        requestPermission();
                    }
                } else {
                    /**
                     * No permissions, block out all activities that require a location to function
                     */
                    Toast.makeText(this, "Permission Not Granted.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    //Compass
    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            compass_image.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    //set firebase DATABASE BY class target
    public class Target {
        private double latitude;
        private double longitude;

        public Target() {}

        public Target(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }
        public double getLongitude() {
            return longitude;
        }
    }

    //Get new GPS location
    private NewGps GetNewLocation(double latitude, double longitude, float degress, int distance){

        float radius = 6371;

        //New latitude in degrees
        double new_latitude = rad2deg(Math.asin(Math.sin(deg2rad(latitude)) * Math.cos(distance / radius) + Math.cos(deg2rad(latitude)) * Math.sin(distance / radius) * Math.cos(deg2rad(degress))));

        //	New longitude in degrees.
        double new_longitude = rad2deg(deg2rad(longitude) + Math.atan2(Math.sin(deg2rad(degress)) * Math.sin(distance / radius) *  Math.cos(deg2rad(latitude)), Math.cos(distance / radius) - Math.sin(deg2rad(latitude)) * Math.sin(deg2rad(new_latitude))));

        //  Assign new latitude and longitude to an array to be returned to the caller.
        NewGps newTarget = new NewGps(new_latitude, new_longitude);

        return newTarget;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private class NewGps {
        private double latitude;
        private double longitude;

        public NewGps() {}

        public NewGps(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }
        public double getLongitude() {
            return longitude;
        }
    }

}