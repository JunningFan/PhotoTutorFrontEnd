package com.example.phototutor.ui.navigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.phototutor.NavigationActivity;
import com.example.phototutor.OrientationModule.OrientationHelper;
import com.example.phototutor.OrientationModule.OrientationHelperOwner;
import com.example.phototutor.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, NavigationActivity.RequestPhotoData, OrientationHelperOwner {
    //maps
    GoogleMap mMap;
    View view;
    SupportMapFragment mapFragment;
    private Marker photoMarker;
    private Marker userMarker;
    MapFragment self;

    //Direction API
    DirectionsHelper directionsHelper;

    // implement RequestPhotoData
    Bitmap bitmap;
    double photoLatitude;
    double photoLongitude;
    double photoOrientation;
    double photoElevation;

    //device orientation
    OrientationHelper orientationHelper;
    float[] orientationRad = {0,0,0};
    float[] orientationDeg = {0,0,0};

    //for fused location
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    MutableLiveData<Double[]> coordinate = new MutableLiveData<Double[]>(new Double[]{Double.valueOf(720), Double.valueOf(720)});
    private final int FUSED_LOCATION_REQUEST_CODE = 0;

    @Override
    public void receivePhotoBitMap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void receiveCoordinate(double latitude, double longitude) {
        this.photoLatitude = latitude;
        this.photoLongitude = longitude;
    }

    @Override
    public void receiveOrientationElevation(double orientation, double elevation) {
        this.photoOrientation = orientation;
        this.photoElevation = elevation;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_map, container, false);
        self = this;
        Log.w("mapfrag","start");
        ((NavigationActivity)getActivity()).requestPhotoData(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orientationHelper = new OrientationHelper(getContext(), this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, FUSED_LOCATION_REQUEST_CODE);
        } else {
            listenLocationChange();
        }

        mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map_preview);

        Observer waitForLocation =new Observer<Double[]>() {
            @Override
            public void onChanged(Double[] doubles) {
                if(doubles[0] != 720) {
                    coordinate.removeObserver(this);
                    mapFragment.getMapAsync(self);
                }
            }
        };
        coordinate.observe(getViewLifecycleOwner(), waitForLocation);

        directionsHelper = new DirectionsHelper();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapFragment.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.w("mapfrag","map ready");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.getUiSettings().setAllGesturesEnabled(false);  // disable touching to the map before the animation is rendered

        mMap.setMinZoomPreference(12.0f);
        mMap.setMaxZoomPreference(21.0f);

        LatLng photoLL = new LatLng(photoLatitude, photoLongitude);

        float degrees = (float) photoOrientation;
        photoMarker = mMap.addMarker(new MarkerOptions()
                .title("Photo")
                .position(photoLL)
                .rotation(degrees)
                .flat(true)
                .icon(vectorToBitmap(R.drawable.ic_baseline_arrow_upward_21, Color.parseColor("#FC771A")))
        );

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });

        LatLng userLL = new LatLng(coordinate.getValue()[0], coordinate.getValue()[1]);

        userMarker = mMap.addMarker(new MarkerOptions()
                .title("You")
                .position(userLL)
                .rotation((float) orientationDeg[2])
                .flat(true)
                .icon(vectorToBitmap(R.drawable.ic_baseline_navigation_18, ContextCompat.getColor(getContext(),R.color.orientation_arrow)))
        );

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds latLngBounds = builder.include(photoLL).include(userLL).build();

        int height = getResources().getDisplayMetrics().heightPixels;
        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int)(width*.45);

        //compute the boundary and zoom to fit the markers
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(latLngBounds, width, height, padding);

        coordinate.observe(this, updateUserMarker -> {
            Log.d("mapFrag", "location update");
            userMarker.setPosition(new LatLng(coordinate.getValue()[0], coordinate.getValue()[1]));
        });

        mMap.animateCamera(cu);

        mMap.setOnMapLoadedCallback(this);
    }



    @Override
    public void onMapLoaded() {
        // draw path
        directionsHelper.getWalkDirection(photoLatitude, photoLongitude, coordinate.getValue()[0], coordinate.getValue()[1],
                new DirectionsHelper.DirectionCallback() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()) {
                            try {
                                JSONObject path = new JSONObject(response.body().string());
                                Log.w("mapFrag", path.toString());
                                JSONArray routes = path.getJSONArray("routes");
                                JSONObject route_obj = routes.getJSONObject(0);
                                String polyPointsRaw = route_obj.getJSONObject("overview_polyline").getString("points");
                                Log.w("mapFrag", polyPointsRaw);
                                List<LatLng> polyPointsList = PolyUtil.decode(polyPointsRaw);
                                Polyline polyline = mMap.addPolyline(new PolylineOptions().clickable(false).addAll(polyPointsList));
                            } catch (JSONException e) {
                                Log.e("mapFrag", "Invalid JSON format");
                                e.printStackTrace();
                            } catch (IOException e) {
                                Log.e("mapFrag", "Network Error");
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
        photoMarker.showInfoWindow();
        mMap.getUiSettings().setAllGesturesEnabled(true);

    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FUSED_LOCATION_REQUEST_CODE:
                if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    listenLocationChange();
                } else {
                    Toast.makeText(getContext(), "location information is necessary for map guidance", Toast.LENGTH_SHORT);
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, FUSED_LOCATION_REQUEST_CODE);
                }
                break;
            default:
        }

    }
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
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
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

    @Override
    public void onOrientationUpdate(float[] orientation) {
;
        OrientationHelper.getDegreesFromRadian(orientation, orientationDeg);
        if(userMarker != null) {
            userMarker.setRotation((float) orientationDeg[2]);
        }
    }
}
