package com.example.uberclone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

public class ViewLocationMapActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private Button btnDrive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        showToast(getIntent().getStringExtra("rUsername"),FancyToast.SUCCESS);

        btnDrive = findViewById(R.id.btnDrive);
        btnDrive.setOnClickListener(this);

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
       // showToast(getIntent().getDoubleExtra("pLatitude",0)+"",FancyToast.SUCCESS);

        // Add a marker in Sydney and move the camera
        LatLng dLocation = new LatLng(getIntent().getDoubleExtra("dLatitude",0), getIntent().getDoubleExtra("dLongtitude",0));

        LatLng pLocation = new LatLng(getIntent().getDoubleExtra("pLatitude",0),getIntent().getDoubleExtra("pLongtitude",0));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver"));
        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger"));

        ArrayList<Marker> allMarker = new ArrayList<>();

        allMarker.add(passengerMarker);
        allMarker.add(driverMarker);

        for (Marker marker:allMarker){

            builder.include(marker.getPosition());

        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,0);
        mMap.animateCamera(cameraUpdate);


    }

    private void showToast(String message, int type) {
        FancyToast.makeText(this, message, FancyToast.LENGTH_SHORT, type, false).show();
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btnDrive){

            showToast(getIntent().getStringExtra("rUsername")+"",FancyToast.SUCCESS);

            ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
            carRequestQuery.whereEqualTo("username",getIntent().getStringExtra("rUsername")+"");
            carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if(objects.size()>0&&e==null){

                        for(ParseObject requestCar:objects){

                            requestCar.put("driverOfMe", ParseUser.getCurrentUser().getUsername());
                            requestCar.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null){

                                        //URL to guide path in google map app
                                        String url = "http://maps.google.com/maps?saddr="+
                                                getIntent().getDoubleExtra("dLatitude",0)+","+
                                                getIntent().getDoubleExtra("dLongtitude",0)+
                                                "&daddr="+getIntent().getDoubleExtra("pLatitude",0)+","+
                                                getIntent().getDoubleExtra("pLongtitude",0);
                                        showToast(url,FancyToast.SUCCESS);

                                        Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                        startActivity(googleIntent);

                                    }
                                }
                            });
                        }

                    }

                }
            });



        }

    }
}
