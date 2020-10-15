package com.example.phototutor.GpsModule;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

//Assumed knowledge: LocationManager
//Contract for defining valid, invalid Gps coordination reading
//Defined GPS coordination as a Bundle, "latitude":double (-90 to 90), "longitude":double (-180 to 80)
//Defines latitude=720, longitude=720 as the marker of invalid coordination reading

public class GpsHelper {
    // GPS permission state code
    public static int PERMISSION_STATE_DENIED = 1;
    public static int PERMISSION_STATE_GRANTED= 0;

    public static int checkGpsPermission(FragmentActivity context) {
        if(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            return PERMISSION_STATE_DENIED;
        }
        return PERMISSION_STATE_GRANTED;
    }

    public static void requestLocationPermission(FragmentActivity context, int requestCode) {
        if (context.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast toast_request_location = Toast.makeText(context, "Your location is necessary for this app to recommend you photos taken nearby", Toast.LENGTH_SHORT);
            toast_request_location.show();
        }
        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }

    public static void requestLocationPermission(Fragment context, int requestCode) {
        if (context.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast toast_request_location = Toast.makeText(context.getActivity(), "Your location is necessary for this app to recommend you photos taken nearby", Toast.LENGTH_SHORT);
            toast_request_location.show();
        }
        context.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }

    public static LocationManager getLocationManager(FragmentActivity context) {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            return null;
        }
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static Bundle getCoordination(FragmentActivity context, LocationManager locationManager) {

        if (GpsHelper.checkGpsPermission(context) != GpsHelper.PERMISSION_STATE_GRANTED) {
            return GpsHelper.getInvalidCoordination();
        }
        Location gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Bundle gps_bundle = new Bundle();
        if(gps_loc != null) {
            gps_bundle.putDouble("latitude", gps_loc.getLatitude());
            gps_bundle.putDouble("longitude", gps_loc.getLongitude());
        } else {
            return GpsHelper.getInvalidCoordination();
        }
        return gps_bundle;
    }

    public static Bundle getInvalidCoordination() {
        Bundle gps_bundle = new Bundle();
        gps_bundle.putDouble("latitude", 720);
        gps_bundle.putDouble("longitude", 720);
        return gps_bundle;
    }

    public static boolean isCoordinationValid(Bundle coord) {
        return coord.getDouble("latitude") != -720 && coord.getDouble("longitude") != 720;
    }

    public static boolean isGpsEnabled(LocationManager locationManager) {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    @SuppressLint("MissingPermission")
    public static void startGpsUpdate(LocationManager locationManager) {
        if(GpsHelper.dummyLocationListener == null)
            GpsHelper.dummyLocationListener = new DummyLocationListener();
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 200, 0, dummyLocationListener);
    }

    @SuppressLint("MissingPermission")
    public static void startGpsUpdate(LocationManager locationManager, LocationListener locationListener) {

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 200, 0, locationListener);
    }

    //shared dummy listener
    static class DummyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }
    }
    static DummyLocationListener dummyLocationListener = null;
}