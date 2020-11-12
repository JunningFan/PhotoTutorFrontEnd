package com.example.phototutor.ui.navigation;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.rendering.LocationNode;
import uk.co.appoly.arcorelocation.rendering.LocationNodeRender;
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper;


public class ARFragment extends Fragment implements OrientationHelperOwner, NavigationActivity.RequestPhotoData {
    private final int FUSED_LOCATION_REQUEST_CODE = 0;
    boolean photoSet = false;
    View view;
    //device orientation
    OrientationHelper orientationHelper;
    float[] orientationRad = {0, 0, 0};
    float[] orientationDeg = {0, 0, 0};
    MutableLiveData<float[]> orientationDegLiveData = new MutableLiveData<>();
    MutableLiveData<Double[]> coordinate = new MutableLiveData<Double[]>(new Double[]{Double.valueOf(720), Double.valueOf(720)});
    // implement RequestPhotoData
    Bitmap bitmap;
    double latitude;
    double longitude;
    double orientation;
    double elevation;
    private boolean installRequested;
    private boolean hasFinishedLoading = false;
    private final Snackbar loadingMessageSnackbar = null;
    private ArSceneView arSceneView;
    // Renderables for this example
    private ViewRenderable photoRenderable;
    // Our ARCore-Location scene
    private LocationScene locationScene;
    //for fused location
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    public void receivePhotoBitMap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void receiveCoordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void receiveOrientationElevation(double orientation, double elevation) {
        this.orientation = orientation;
        this.elevation = elevation;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sceneform, container, false);
    }

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((NavigationActivity) getActivity()).requestPhotoData(this);

        this.view = view;
        orientationHelper = new OrientationHelper(getContext(), this);


        arSceneView = view.findViewById(R.id.ar_scene_view);

        CompletableFuture<ViewRenderable> photoLayout =
                ViewRenderable.builder()
                        .setView(getContext(), R.layout.photo_layout)
                        .build();


        CompletableFuture.allOf(
                photoLayout)
                .handle(
                        (notUsed, throwable) -> {
                            // When you build a Renderable, Sceneform loads its resources in the background while
                            // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                            // before calling get().

                            if (throwable != null) {
                                ARUtils.displayError(getContext(), "Unable to load renderables", throwable);
                                return null;
                            }

                            try {
                                photoRenderable = photoLayout.get();
                                hasFinishedLoading = true;

                            } catch (InterruptedException | ExecutionException ex) {
                                ARUtils.displayError(getContext(), "Unable to load renderables", ex);
                            }

                            return null;
                        });

        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {
                            if (!hasFinishedLoading) {
                                return;
                            }

                            if (locationScene == null) {
                                // If our locationScene object hasn't been setup yet, this is a good time to do it
                                // We know that here, the AR components have been initiated.
                                locationScene = new LocationScene(getActivity(), arSceneView);
                                locationScene.setAnchorRefreshInterval(4000);
                                locationScene.setDistanceLimit(25);
                                locationScene.refreshAnchors();
                                // Now lets create our location markers.
                                // First, a layout

                                LocationMarker photoLocationMarker = new LocationMarker(longitude,
                                        latitude,
                                        getPhotoView());
                                photoLocationMarker.setHeight(-3f);
                                photoLocationMarker.setGradualScalingMaxScale(1f);
                                photoLocationMarker.setGradualScalingMinScale(1f);
                                photoLocationMarker.setScaleModifier(1F);
                                photoLocationMarker.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode node) {
                                        View eView = photoRenderable.getView();
                                        TextView distanceText = eView.findViewById(R.id.distance);
                                        distanceText.setText(node.getDistance() + "M");
                                        ImageView photoImage = eView.findViewById(R.id.photo);
                                        if (!photoSet) {
                                            photoImage.setImageBitmap(bitmap);
                                            photoImage.setAlpha(0.5f);
                                            photoSet = true;
                                        }
                                    }
                                });

                                locationScene.mLocationMarkers.add(photoLocationMarker);
                            }

                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }

                            if (locationScene != null) {
                                locationScene.processFrame(frame);
                            }

                            if (loadingMessageSnackbar != null) {
                                for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                    if (plane.getTrackingState() == TrackingState.TRACKING) {
                                    }
                                }
                            }
                        });


        // Lastly request CAMERA & fine location permission which is required by ARCore-Location.
        ARLocationPermissionHelper.requestPermission(getActivity());
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        listenLocationChange();
        Observer waitForLocation = new Observer<Double[]>() {
            @Override
            public void onChanged(Double[] doubles) {
                if (doubles[0] != 720) {
                    coordinate.removeObserver(this);
                    ConstraintLayout camera_ui_container = getView().findViewById(R.id.orientation_overlay);
                    if (camera_ui_container != null)
                        ((ConstraintLayout) getView()).removeView(camera_ui_container);
                    orientationDegLiveData.removeObservers(getViewLifecycleOwner());
                    // Inflate a new view containing all UI for controlling the camera
                    View overlay = View.inflate(requireContext(),
                            R.layout.orientation_overlay, (ConstraintLayout) getView());
                    orientationDegLiveData.observe(getViewLifecycleOwner(), listener -> {
                        //Source
                        double lat1 = coordinate.getValue()[0];
                        double lng1 = coordinate.getValue()[1];

                        // destination
                        double lat2 = latitude;
                        double lng2 = longitude;

                        double dLon = (lng2 - lng1);
                        double y = Math.sin(dLon) * Math.cos(lat2);
                        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
                        double brng = Math.toDegrees((Math.atan2(y, x)));
                        brng = (360 - ((brng + 360) % 360));

                        double azi = brng;
                        // disable pitch marker
                        overlay.findViewById(R.id.imageViewUp).setAlpha(0);
                        overlay.findViewById(R.id.imageViewDown).setAlpha(0);

                        //azimuth marker
                        if (Math.abs(orientationDegLiveData.getValue()[2] - azi) < 12) {
                            overlay.findViewById(R.id.imageViewLeft).setAlpha(1);
                            overlay.findViewById(R.id.imageViewLeft).setRotation(180f);
                            overlay.findViewById(R.id.imageViewRight).setAlpha(1);
                            overlay.findViewById(R.id.imageViewRight).setRotation(180f);

                        } else {
                            overlay.findViewById(R.id.imageViewLeft).setRotation(0);
                            overlay.findViewById(R.id.imageViewRight).setRotation(0);

                            if (orientationDegLiveData.getValue()[2] > azi) {
                                if (Math.abs(azi + Math.abs(360 - orientationDegLiveData.getValue()[2])) < Math.abs(azi - orientationDegLiveData.getValue()[2])) {
                                    float opacity = (float) Math.abs(azi + Math.abs(360 - orientationDegLiveData.getValue()[2])) / 120;
                                    if (opacity > 1) {
                                        opacity = 1f;
                                    }
                                    overlay.findViewById(R.id.imageViewLeft).setAlpha(0);
                                    overlay.findViewById(R.id.imageViewRight).setAlpha(opacity);
                                } else {
                                    float opacity = (float) Math.abs(azi - orientationDegLiveData.getValue()[2]) / 120;
                                    if (opacity > 1) {
                                        opacity = 1f;
                                    }
                                    overlay.findViewById(R.id.imageViewLeft).setAlpha(opacity);
                                    overlay.findViewById(R.id.imageViewRight).setAlpha(0);
                                }
                            } else {
                                if (Math.abs(azi - orientationDegLiveData.getValue()[2]) < Math.abs(orientationDegLiveData.getValue()[2]) + (360 - azi)) {
                                    float opacity = (float) Math.abs(azi - orientationDegLiveData.getValue()[2]) / 120;
                                    if (opacity > 1) {
                                        opacity = 1f;
                                    }
                                    overlay.findViewById(R.id.imageViewLeft).setAlpha(0);
                                    overlay.findViewById(R.id.imageViewRight).setAlpha(opacity);
                                } else {
                                    float opacity = (float) (Math.abs(orientationDegLiveData.getValue()[2]) + (360 - azi)) / 120;
                                    if (opacity > 1) {
                                        opacity = 1f;
                                    }
                                    overlay.findViewById(R.id.imageViewLeft).setAlpha(opacity);
                                    overlay.findViewById(R.id.imageViewRight).setAlpha(0);
                                }
                            }

                        }
                    });
                }
            }
        };
        coordinate.observe(getViewLifecycleOwner(), waitForLocation);
    }

    /**
     * Example node of a layout
     *
     * @return
     */

    /***
     * Example Node of a 3D model
     *
     * @return
     */


    private Node getPhotoView() {
        Node base = new Node();
        base.setRenderable(photoRenderable);
        return base;
    }

    /**
     * Make sure we call locationScene.resume();
     */
    @Override
    public void onResume() {
        super.onResume();

        if (locationScene != null) {
            locationScene.resume();
        }

        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = ARUtils.createArSession(getActivity(), installRequested);
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(getActivity());
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                ARUtils.handleSessionException(getActivity(), e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            ARUtils.displayError(getContext(), "Unable to get camera", ex);
            getActivity().finish();
            return;
        }

        if (arSceneView.getSession() != null) {
        }

    }

    /**
     * Make sure we call locationScene.pause();
     */
    @Override
    public void onPause() {
        super.onPause();

        if (locationScene != null) {
            locationScene.pause();
        }

        arSceneView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        arSceneView.destroy();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!ARLocationPermissionHelper.hasPermission(getActivity())) {
            if (!ARLocationPermissionHelper.shouldShowRequestPermissionRationale(getActivity())) {
                // Permission denied with checking "Do not ask again".
                ARLocationPermissionHelper.launchPermissionSettings(getActivity());
            } else {
                Toast.makeText(
                        getContext(), "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
            }
            getActivity().finish();
        }
    }

    @SuppressLint("MissingPermission")
    private void listenLocationChange() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(100);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d(this.getClass().getSimpleName(), "change" + coordinate.getValue()[0].toString() + " " + coordinate.getValue()[1].toString());
                        coordinate.setValue(new Double[]{location.getLatitude(), location.getLongitude()});
                    }
                }
            }
        };
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d(this.getClass().getSimpleName(), "change" + coordinate.getValue()[0].toString() + " " + coordinate.getValue()[1].toString());
                    coordinate.setValue(new Double[]{location.getLatitude(), location.getLongitude()});

                }
            }
        });
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onOrientationUpdate(float[] orientation) {
        OrientationHelper.getDegreesFromRadian(orientation, orientationDeg);
        orientationDegLiveData.setValue(orientationDeg);
    }

}
