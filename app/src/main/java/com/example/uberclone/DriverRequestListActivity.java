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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btnGetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ListView listView;
    private ArrayList<String> nearByRequest;
    private ArrayAdapter adapter;

    private ArrayList<Double> passengerLatitudes;
    private ArrayList<Double> passengerLongtitudes;
    private ArrayList<String> requestCarUsername;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        btnGetRequests = findViewById(R.id.btnGetRequests);
        btnGetRequests.setOnClickListener(this);

        listView = findViewById(R.id.listView);

        nearByRequest = new ArrayList<>();
        passengerLatitudes = new ArrayList<>();
        passengerLongtitudes = new ArrayList<>();
        requestCarUsername = new ArrayList<>();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearByRequest);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);


        nearByRequest.clear();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);


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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < 23) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        }


    }

    private void updateRequestsListView(Location driverLocation) {
        if (driverLocation != null) {

            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(), driverLocation.getLongitude());

            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
            requestCarQuery.whereNear("passengerLocation", driverCurrentLocation);
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (objects.size() > 0 && e == null) {

                        if (nearByRequest.size() > 0) {

                            nearByRequest.clear();

                        }

                        if (passengerLatitudes.size() > 0) {

                            passengerLatitudes.clear();

                        }

                        if (passengerLongtitudes.size() > 0) {

                            passengerLongtitudes.clear();

                        }

                        for (ParseObject nearRequest : objects) {

                            ParseGeoPoint passengerPoint = (ParseGeoPoint) nearRequest.get("passengerLocation");
                            double distanceInMiles = driverCurrentLocation.distanceInMilesTo(passengerPoint);

                            float roundedDistance = Math.round(distanceInMiles * 100000) / 10000f;
                            nearByRequest.add("There are " + roundedDistance + " miles to " + nearRequest.getString("username"));

                            passengerLatitudes.add(passengerPoint.getLatitude());
                            passengerLongtitudes.add(passengerPoint.getLongitude());
                            requestCarUsername.add(nearRequest.getString("username"));



                        }

                        adapter.notifyDataSetChanged();

                    }

                }
            });


        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_driver, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.driverLogoutItem) {

            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {

                    if (e == null) {

                        finish();
                        startActivity(new Intent(DriverRequestListActivity.this, MainActivity.class));


                    } else {

                        showToast(e.getMessage(), FancyToast.ERROR);

                    }

                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    private void showToast(String message, int type) {
        FancyToast.makeText(this, message, FancyToast.LENGTH_SHORT, type, false).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnGetRequests) {


            if (Build.VERSION.SDK_INT >= 23) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    updateRequestsListView(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));

                } else {

                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                }

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {

            if (grantResults.length > 0 && grantResults[0] == Activity.RESULT_OK) {

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
                updateRequestsListView(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent(this, ViewLocationMapActivity.class);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);

            return;
        }
        Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(cdLocation!=null){

            intent.putExtra("dLatitude",cdLocation.getLatitude());
            intent.putExtra("dLongtitude",cdLocation.getLongitude());
            intent.putExtra("pLatitude",passengerLatitudes.get(position));
            intent.putExtra("pLongtitude",passengerLongtitudes.get(position));
            intent.putExtra("rUsername",requestCarUsername.get(position));

            startActivity(intent);

        }




    }

}
