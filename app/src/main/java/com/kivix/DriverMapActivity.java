package com.kivix;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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

import java.util.List;

public class  DriverMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int ACCESS_LOCATION_REQUEST_CODE = 1001;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location lastLocation;

    private Button LogOutDriverButton, SettingsDriverButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private boolean LogoutState;
    private DatabaseReference assignedCustomerRef, AssignedCustomerPositionRef;
    private String driverID , customerID = "" ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        driverID = mAuth.getCurrentUser().getUid();
        LogOutDriverButton = (Button)findViewById(R.id.driverLogout);
        SettingsDriverButton = (Button)findViewById(R.id.settingsButtonDriver);

        LogOutDriverButton.setOnClickListener(v -> {

            LogoutState = true;
            mAuth.signOut();
            DisconnectDriver();
            LogOutDriver();

        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getAssignedCustomerRequest();
    }

    private void getAssignedCustomerRequest() {

        assignedCustomerRef = FirebaseDatabase.getInstance("https://kivix-8a820-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(driverID).child("customerRideID");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    customerID = dataSnapshot.getValue().toString();

                    getAssignedCustomerPosition();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedCustomerPosition() {
        AssignedCustomerPositionRef = FirebaseDatabase.getInstance().getReference().child("Customer Request").child(customerID).child("l");
        AssignedCustomerPositionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> customerPositionMap = (List<Object>) dataSnapshot.getValue();
                    double LocationLat = 0;
                    double LocationLong = 0;

                    if (customerPositionMap.get(0) != null) {
                        LocationLat = Double.parseDouble(customerPositionMap.get(0).toString());

                    }

                    if (customerPositionMap.get(1) != null) {
                        LocationLong = Double.parseDouble(customerPositionMap.get(1).toString());

                    }

                    LatLng DriverLatLong = new LatLng(LocationLat, LocationLong);
                    mMap.addMarker(new MarkerOptions().position(DriverLatLong).title("Take the client from here"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void LogOutDriver() {
        Intent WelcomeIntent = new Intent(DriverMapActivity.this, WelcomeActivity.class);
        startActivity(WelcomeIntent);
        finish();
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



    private void zoomToUserLocation() {
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
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
           public void onSuccess(Location location) {
                if (getApplicationContext() != null) {
                    lastLocation = location;

                    LatLng latLng = new LatLng( location.getLongitude(), location.getLatitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference DriverAvailabilityRef = FirebaseDatabase.getInstance("https://kivix-8a820-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Driver Availability");
                    DatabaseReference DriverWorkingRef = FirebaseDatabase.getInstance("https://kivix-8a820-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Driver Working");
                    GeoFire geoFireAvailability = new GeoFire(DriverAvailabilityRef);
                    GeoFire geoFireWorking = new GeoFire(DriverWorkingRef);


                    switch (customerID) {
                        case "":
                            geoFireWorking.removeLocation(userID);
                            geoFireAvailability.setLocation(userID, new GeoLocation(location.getLatitude(),location.getLongitude()));
                            break;
                        default:
                            geoFireAvailability.removeLocation(userID);
                            geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(),location.getLongitude()));
                            break;
                    }

                }
            }
        });

    }

    private void enableUserLocation() {
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
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
                zoomToUserLocation();
            } else {
                //Dialog if permission not granted.
            }
        }

    }

    @Override
    protected void onStop() {
        if (!LogoutState) {
            DisconnectDriver();
            super.onStop();

        }
    }

    private void DisconnectDriver() {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference DriverAvailabilityRef = FirebaseDatabase.getInstance("https://kivix-8a820-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Driver Available");

            GeoFire geoFire = new GeoFire(DriverAvailabilityRef);
            geoFire.removeLocation(userID);
    }


}