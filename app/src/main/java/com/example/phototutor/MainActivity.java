package com.example.phototutor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.phototutor.ui.login.ui.login.LoginActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static SharedPreferences sharedPreferences;
    public static final String fileName = "login";
    public static final String Username = "username";

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    MutableLiveData<Double[]> coordinate = new MutableLiveData<Double[]>(new Double[]{Double.valueOf(720), Double.valueOf(720)});
    private final int FUSED_LOCATION_REQUEST_CODE = 0;

    MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_notifications)
                .build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        configNavigationItemSelectedListener(navView);
        configTopAppBarItemSelectedListener(findViewById(R.id.topAppBar));
        topAppBar = (MaterialToolbar)findViewById(R.id.topAppBar);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, FUSED_LOCATION_REQUEST_CODE);
        } else {
            listenLocationChange();
        }

        /*coordinate.observe(this, observer -> {
            if(coordinate.getValue()[0] != 720) {
                try {
                    Log.d(this.getClass().getSimpleName(), "coordination" + coordinate.getValue()[0].toString() + " " + coordinate.getValue()[1].toString());
                    Geocoder geocoder = new Geocoder(this);
                    String po = (geocoder.getFromLocation(coordinate.getValue()[0], coordinate.getValue()[1], 1)).get(0).getPostalCode();
                    String adminArea = (geocoder.getFromLocation(coordinate.getValue()[0], coordinate.getValue()[1], 1)).get(0).getAdminArea();
                    String locality = (geocoder.getFromLocation(coordinate.getValue()[0], coordinate.getValue()[1], 1)).get(0).getLocality();
                    topAppBar.setTitle(locality  + " " + po);
                } catch (IOException e) {
                    topAppBar.setTitle("Disconnected");
                }
            }

        });*/
    }

    private void configTopAppBarItemSelectedListener(MaterialToolbar topAppBar ){
        topAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.navigation_logout:
                    LoginActivity.userLogout();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    break;
                default:return false;
            }

            return true;
        });
    }


    private void configNavigationItemSelectedListener(BottomNavigationView navView ){
        navView.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.dialog_selection){
                PopupMenu popup = new PopupMenu(this,findViewById(R.id.dialog_selection));
                popup.getMenuInflater().inflate(R.menu.camera_local_album_selection_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(menuItem -> {
                    Intent intent = null;
                    switch (menuItem.getItemId()){
                        case R.id.navigation_camera:
                            intent = new Intent(this, CameraActivity.class);
                            break;
                        case R.id.navigation_local_album:
                            intent = new Intent(this, LocalAlbumActivity.class);
                            break;


                    }
                    startActivity(intent);
                    return true;
                });
                popup.setOnDismissListener(popupMenu -> {
                    navView.setSelectedItemId(R.id.navigation_home);

                });
                popup.show();
            }
            else if (item.getItemId() == R.id.navigation_notifications){
                Navigation.findNavController(this,R.id.nav_host_fragment)
                        .navigate(R.id.navigation_notifications);
            }
            else if (item.getItemId() == R.id.navigation_home){
                Navigation.findNavController(this,R.id.nav_host_fragment)
                        .navigate(R.id.navigation_home);
            }
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        sharedPreferences = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        if(sharedPreferences.contains(Username)) {
            finishAffinity();
            finish();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FUSED_LOCATION_REQUEST_CODE:
                if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    listenLocationChange();
                } else {
                    Toast.makeText(this, "location information is necessary for the photo recommendation service", Toast.LENGTH_SHORT);
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, FUSED_LOCATION_REQUEST_CODE);
                }
                break;
            default:
        }
    }

    //for fused location listenr
    @SuppressLint("MissingPermission")
    private void listenLocationChange() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(100);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location: locationResult.getLocations()) {
                    if(location != null) {
                        Log.d(this.getClass().getSimpleName(), "change" + coordinate.getValue()[0].toString() +" " +  coordinate.getValue()[1].toString());
                        coordinate.setValue(new Double[]{location.getLatitude(), location.getLongitude()});
                    }
                }
            }
        };
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    Log.d(this.getClass().getSimpleName(), "change" + coordinate.getValue()[0].toString() +" " +  coordinate.getValue()[1].toString());
                    coordinate.setValue(new Double[]{location.getLatitude(), location.getLongitude()});

                }
            }
        });
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
}