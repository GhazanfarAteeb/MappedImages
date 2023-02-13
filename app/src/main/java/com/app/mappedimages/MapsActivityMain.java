package com.app.mappedimages;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.mappedimages.databinding.ActivityMapsMainBinding;
import com.app.mappedimages.databinding.ItemDialogBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivityMain extends AppCompatActivity implements OnMapReadyCallback {
//    THIS WILL HELP US TO SAVE THE CURRENT LOCATION OF THE USER
    private static Location currentLocation;
//    INSTANCE OF GOOGLE MAPS
    private GoogleMap mMap;
    private ActivityMapsMainBinding binding;
//    ITS THE LOCATION REQUEST FOR THE MAPS IN WHICH WE DEFINE THE TIME INTERVAL OF THE LOCAITON
    private LocationRequest locationRequest;
//    LOCATION CLIENT WHICH PROVIDES US THE CONTINUOUS LOCATION OF THE USER
    private FusedLocationProviderClient client;
//    A STATIC LIST OF THE MARKERS WHICH WILL BE PLACED ON THE MAPS
    private static List<MarkerOptions> markersList = new ArrayList<>();
//    THE LIST OF PERMISSIONS WHICH ARE REQUIRED FOR THIS APPLICATION TO WORK PROPERLY
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    // CALLBACK IN WHICH WE WILL GET THE LOCATION OF THE USER
    LocationCallback callback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            //THE LAST LOCATION FUNCTION WILL PROVIDE US THE LATEST LAST LOCATION OF THE USER
            currentLocation = locationResult.getLastLocation();
            // THE DATA CONTAINS THE IMAGE URI
            Uri data = getIntent().getData();
            //THE GET DATA FUNCTION WILL TRY TO PLACE MARKER AGAIN AND AGAIN SO TO STOP THIS THE IMAGE URI IS STORED IN THE ABOVE VARIABLE
            // AND THEN THE DATA OF THE INTENT IS SET TO NULL SO THERE SHOULD BE NO REPETATION OF THE MARKER
            getIntent().setData(null);
            if (data != null) {
//                CREATING THE INSTANCE OF MARKER TO BE PLACED ON THE MAPS
                MarkerOptions marker = new MarkerOptions();
                marker.draggable(false);
                marker.position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                marker.title(data.toString());
                marker.visible(true);
                // ADDING MARKER TO THE LIST SO THE MARKER WILL KEEP SHOWING DESPITE THE SCREEN IS BEING CHANGED BECAUSE MARKER LIST IS STATIC
                markersList.add(marker);
                if (mMap != null) {
                    // THIS LOOP IS USED TO PLACE ALL THE MARKERS ON THE MAP
                    for (MarkerOptions m: markersList) {
                        mMap.addMarker(m);
                    }
//                    THIS WILL ZOOM THE MARKER TO THE CURRENT ADDED MARKER
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18));
                }
            }
        }
    };

    // THIS WILL INITIALIZE THE FUSED LOCATION PROVIDER CLIENT AND THEN CHECK THE PERMISSION IF THE PERMISSIONS ARE GRANTED THEN THE CLIENT
    // WILL REQUEST FOR LOCATION UPDATES
    private void startLocationUpdates() {
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper());
    }
    //SETTING UP THE INTERVAL OF THE LOCATION REQUEST

    private void createLocationRequest() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(0)
                .setFastestInterval(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // ENABLING THE LOCATION SERVICES IF THE PERMISSIONS ARE GIVEN
        if (PermissionUtils.checkPermission(MapsActivityMain.this, android.Manifest.permission.ACCESS_FINE_LOCATION) && PermissionUtils.checkPermission(MapsActivityMain.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            enableLocationSettings();
        } else {
            // IN CASE IF PERMISSIONS ARE NOT PROVIDED THEN THE PERMISSIONS DIALOG BOX WILL APPEAR DUE TO THE FOLLOWING CODE AND WILL ASK FOR PERMISSIONS
            ActivityResultLauncher<String[]> permissionRequest = registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        if (fineLocationGranted != null && fineLocationGranted && coarseLocationGranted != null && coarseLocationGranted) {
                            Log.d("LoginActivity", "Location permissions granted");
                            enableLocationSettings();
                        }
                    });
            permissionRequest.launch(PERMISSIONS);
        }
//        enableLocationSettings();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        createLocationRequest();
        startLocationUpdates();
        binding.fabCaptureImage.setOnClickListener(view -> {
            startActivity(new Intent(MapsActivityMain.this, CameraActivity.class));
        });
    }


    // THIS FUNCTION WILL WORK WHEN THE MAP IS READY TO SHOW
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
//                marker.hideInfoWindow();
                // THIS DIALOG BOX WILL SHOW THE IMAGE WHEN ANY MARKER IS CLICKED
                AlertDialog dialog = new AlertDialog.Builder(MapsActivityMain.this)
                        .create();
                ItemDialogBinding itemDialogBinding = ItemDialogBinding.inflate(LayoutInflater.from(MapsActivityMain.this));
                dialog.setView(itemDialogBinding.getRoot());
                Glide.with(MapsActivityMain.this).load(marker.getTitle()).into(itemDialogBinding.ivImage);
                dialog.show();
                return true;
            }
        });

    }

    public void enableLocationSettings() {
        // CHECKING IF THE LOCATION SERVICES ARE RUNNING OR NOT IF NOT RUNNING THEN THE LOCATION REQUEST WILL BE MADE FOR TURNING ON THE LOCATION
        if (!isLocationEnabled(this)) {

            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(0)
                    .setFastestInterval(0)
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
                    .addOnSuccessListener(this, (LocationSettingsResponse response) -> {

                    }).addOnFailureListener(this, ex -> {
                        if (ex instanceof ResolvableApiException) {
                            // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                            try {
//                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                                ResolvableApiException resolvable = (ResolvableApiException) ex;
                                resolvable.startResolutionForResult(MapsActivityMain.this, 0);
                            } catch (IntentSender.SendIntentException sendEx) {
                                // Ignore the error.
                            }
                        }
                    });

        }
    }

//    THIS FUNCTION WILL CHECK THE LOCATION SERVICES
    public static boolean isLocationEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (NullPointerException ex) {
            ex.getLocalizedMessage();
        }

        boolean network_enabled = false;
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (NullPointerException ex) {
            ex.getLocalizedMessage();
        }

        return (gps_enabled || network_enabled);
    }

    // WHEN THE PERMISSIONS ARE GRANTED THEN THE LOCATION SERVICES WILL BE REQUESTED BY THIS ENABLE LOCATION SETTINGS FUNCTION
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            enableLocationSettings();
        }

    }
}