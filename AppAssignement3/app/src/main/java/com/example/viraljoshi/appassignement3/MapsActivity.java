package com.example.viraljoshi.appassignement3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
/* The reference of the below code is being taken from the link given below...*/
//https://www.youtube.com/watch?v=qS1E-Vrk60E
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    TextView textView;
    Button button;
    //The database reference is created
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        textView=(TextView)findViewById(R.id.txt1);
        button=(Button)findViewById(R.id.save);
        //The Database link is being taken from
        //https://www.youtube.com/watch?v=b9VoJ7mD2Os&list=PLGCjwl1RrtcSi2oV5caEVScjkM6r3HO9t&index=6#t=23.769799
        // Get database reference

        databaseReference= FirebaseDatabase.getInstance().getReference();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //check whether the network provide is enabled or not
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {


            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //get the latitude
                    double latitude = location.getLatitude();
                    //get the Longitude
                    double longitude = location.getLongitude();
                    //instantiate the class Latitude Longitude
                    LatLng latLng= new LatLng(latitude,longitude);
                    //instantiate the class, GeoCode class
                    Geocoder geocoder= new Geocoder(getApplicationContext());
                    try {
                        //get the Location address, location code, postal code, Country name and more..
                        List<Address> addressList= geocoder.getFromLocation(latitude,longitude,1);
                        String str= addressList.get(0).getLocality()+",";
                        str+=addressList.get(0).getCountryName()+",";
                        str+=addressList.get(0).getLatitude()+",";
                        str+=addressList.get(0).getLongitude();
                        //setting the values in Text view
                        textView.setText(str);
                        //To get the current location of the User
                        mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10.2f));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
        //if Network provider is not there then it will go in the else if condition where it will go through GPS Provide which is given below
        else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //get the latitude
                    double latitude = location.getLatitude();
                    //get the Longitude
                    double longitude = location.getLongitude();
                    //instantiate the class Latitude Longitude
                    LatLng latLng= new LatLng(latitude,longitude);
                    //instantiate the class, GeoCode class
                    Geocoder geocoder= new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList= geocoder.getFromLocation(latitude,longitude,1);
                        String str= addressList.get(0).getLocality()+",";
                        str+=addressList.get(0).getCountryName();
                        str+=addressList.get(0).getLatitude()+",";
                        str+=addressList.get(0).getLongitude();
                        textView.setText(str);

                        mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10.2f));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create child in root object
                //Assign value
                String name= textView.getText().toString().trim();
                //Storing value in the cloud
                //Here we have used Firebase as a cloud platform
                databaseReference.child("Name").setValue(name);
                //Toast Message
                Toast.makeText(getApplicationContext(),"You are done.. Data is stored in Cloud",Toast.LENGTH_LONG).show();

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,10.2f));
    */
    }
}
