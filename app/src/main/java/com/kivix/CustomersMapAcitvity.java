package com.kivix;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomersMapAcitvity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location lastLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private int ACCESS_LOCATION_REQUEST_CODE = 1001;
    private FirebaseAuth mAuth;
    Marker driverMarker;
    private FirebaseUser currentUser;
    private boolean LogoutState;
    private String customerID;
    private DatabaseReference CustomerDatabaseRef;
    private LatLng CustomerPosition;
    private DatabaseReference DriversAvailabilityRef;
    private DatabaseReference DriversRef;
    private DatabaseReference DriversLocationRef;

    private int radius = 1;
    private boolean driverFound = false;
    private String driverFoundID;

    private Button customerLogoutButton;
    private Button orderTaxiButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_map_acitvity);
        orderTaxiButton = (Button) findViewById(R.id.orderTaxiButton);
        mAuth = FirebaseAuth.getInstance();
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUser = mAuth.getCurrentUser();
        CustomerDatabaseRef = FirebaseDatabase.getInstance("https://kivix-8a820-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Customers Requests");
        DriversAvailabilityRef = FirebaseDatabase.getInstance("https://kivix-8a820-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Driver Availability");
        DriversLocationRef = FirebaseDatabase.getInstance("https://kivix-8a820-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Driver working");
        customerLogoutButton = (Button) findViewById(R.id.customerLogoutBtn);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        orderTaxiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                geoFire.setLocation(customerID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                CustomerPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(CustomerPosition).title("The customer is here."));

                orderTaxiButton.setText("Looking for drivers");
                getNearbyDrivers();
            }
        });
        customerLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutState = true;
                mAuth.signOut();
                DisconnectCustomer();
                LogOutCustomer();
            }
        });
    }


    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    private void zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));

            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager
                .PERMISSION_GRANTED) {
            enableUserLocation();
            zoomToUserLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Showing a dialog for user why location is needed.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCESS_LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCESS_LOCATION_REQUEST_CODE);

            }
        }
    }

    private void DisconnectCustomer() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference CustomerAvailabilityRef = FirebaseDatabase.getInstance("https://kivix-8a820-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Customer Available");

        GeoFire geoFire = new GeoFire(CustomerAvailabilityRef);
        geoFire.removeLocation(userID);
    }

    private void LogOutCustomer() {
        Intent WelcomeIntent = new Intent(CustomersMapAcitvity.this, WelcomeActivity.class);
        startActivity(WelcomeIntent);
        finish();
    }

    @Override
    protected void onStop() {
        if (!LogoutState) {
            DisconnectCustomer();
            super.onStop();

        }
    }

    private void getNearbyDrivers() {
        GeoFire geoFire = new GeoFire(DriversAvailabilityRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPosition.latitude, CustomerPosition.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound) {
                    driverFound = true;
                    driverFoundID = key;

                    DriversRef = FirebaseDatabase.getInstance("https://kivix-8a820-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(driverFoundID);
                    HashMap driverMap = new HashMap();
                    driverMap.put("customerRideID", customerID);
                    DriversRef.updateChildren(driverMap);

                    GetDriverLocation();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius++;
                    getNearbyDrivers();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void GetDriverLocation() {
            DriversLocationRef.child(driverFoundID).child("l").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                        double LocationLat = 0;
                        double LocationLong = 0;
                        orderTaxiButton.setText("Driver founded");

                        if (driverLocationMap.get(0) != null) {
                            LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());

                        }

                        if (driverLocationMap.get(1) != null) {
                            LocationLong = Double.parseDouble(driverLocationMap.get(1).toString());

                        }

                        LatLng DriverLatLong = new LatLng(LocationLat, LocationLong);

                        if (driverMarker != null) {
                            driverMarker.remove();
                        }

                        Location location1 = new Location("");
                        location1.setLatitude(CustomerPosition.latitude);
                        location1.setLongitude(CustomerPosition.longitude);

                        Location location2 = new Location("");
                        location2.setLatitude(DriverLatLong.latitude);
                        location2.setLongitude(DriverLatLong.longitude);

                        float Distance = location1.distanceTo(location2);
                        orderTaxiButton.setText("Distance to taxi" + String.valueOf(Distance));
                        driverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLong).title("Your taxi is here"));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

}