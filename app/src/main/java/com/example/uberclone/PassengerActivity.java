package com.example.uberclone;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.List;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    private Button btnRequestCar;



    private LocationManager locationManager;
    private LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRequestCar = findViewById(R.id.btnRequestACar);
        btnRequestCar.setOnClickListener(this);
        findViewById(R.id.btnLogoutFromPassengerActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                finish();
                startActivity(new Intent(PassengerActivity.this,MainActivity.class));
            }
        });

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("RequestCar");
        parseQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects!=null&&objects.size()>0&&e == null){

                    btnRequestCar.setText("Cancel the car");

                }else{

                    btnRequestCar.setText("Request a car");

                }
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateCameraPassengerLocation(location);

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
        };

        // Add a marker in Sydney and move the amera

        if (Build.VERSION.SDK_INT < 23) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

            updateCameraPassengerLocation(location);

        }else if(Build.VERSION.SDK_INT>=23){

            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);


            }else{

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

                updateCameraPassengerLocation(location);

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1000 &&grantResults.length>0&& grantResults[0] == Activity.RESULT_OK){
            if(Build.VERSION.SDK_INT>=23&& checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

                updateCameraPassengerLocation(location);


            }

        }



    }
    private void updateCameraPassengerLocation(Location location){
        LatLng passengerLocation = new LatLng(location.getLatitude(),location.getLongitude());

        //mMap.clear();
        //Move google Map camera

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation,15));
        mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You are here"));


    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btnRequestACar){

            if(btnRequestCar.getText().toString().equals("Request a car")){

                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Location passengerCurrentLocation =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(passengerCurrentLocation!=null){

                        ParseObject requestCar = new ParseObject("RequestCar");
                        requestCar.put("username", ParseUser.getCurrentUser().getUsername());

                        ParseGeoPoint userLocation = new ParseGeoPoint(passengerCurrentLocation.getLatitude(),passengerCurrentLocation.getLongitude());
                        requestCar.put("passengerLocation",userLocation);

                        requestCar.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null){

                                    showToast("A car request is called",FancyToast.SUCCESS);
                                    btnRequestCar.setText("Cancel the car");


                                }else{

                                    showToast(e.getMessage(),FancyToast.ERROR);
                                }


                            }
                        });





                    }else{

                        showToast("Some thing went wrong",FancyToast.ERROR);


                    }


                }

            }else if(btnRequestCar.getText().toString().equals("Cancel the car")){

                ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("RequestCar");
                parseQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                parseQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {

                        if(objects!=null && e == null){

                            for(ParseObject request:objects){

                                request.deleteInBackground();

                            }
                            btnRequestCar.setText("Request the car");
                            showToast("Deleted  your request",FancyToast.SUCCESS);


                        }else{



                        }

                    }
                });




                

            }



        }



    }
    private void showToast(String message, int type) {
        FancyToast.makeText(this,message,FancyToast.LENGTH_SHORT,type,false).show();
    }
}
