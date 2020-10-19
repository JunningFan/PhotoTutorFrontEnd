package com.example.phototutor.cameraFragment;

import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;

import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.CameraView;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.ColorDrawable;
import android.hardware.display.DisplayManager;
import android.icu.number.Scale;
import android.location.LocationManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.Navigation;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.phototutor.GpsModule.GpsHelper;
import com.example.phototutor.OrientationModule.OrientationHelper;
import com.example.phototutor.OrientationModule.OrientationHelperOwner;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class CameraFragment extends Fragment implements OrientationHelperOwner {
    private CameraViewModel mViewModel;
    @Nullable private Preview preview = null;
    @Nullable private ImageCapture imageCapture = null;
    @Nullable private ImageAnalysis imageAnalyzer = null;
    @Nullable private Camera camera = null;
    @Nullable private ProcessCameraProvider cameraProvider = null;
    @Nullable private ExecutorService cameraExecutor = null;
    private  LocalBroadcastManager broadcastManager;
    private DisplayManager displayManager;
    private int lensFacing;
    private int displayId;

    private OrientationHelper orientationHelper;
    private float[] orientationDegrees = new float[3]; //R, P, A


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayManager= (DisplayManager)requireContext().getSystemService(Context.DISPLAY_SERVICE);
        displayId = this.displayId;

    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    /** Volume down button receiver used to trigger shutter */
    private BroadcastReceiver volumeDownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("key_event_action", KeyEvent.KEYCODE_UNKNOWN)){
                case KeyEvent.KEYCODE_VOLUME_DOWN: {
                    ((ImageButton)getView().findViewById(R.id.camera_capture_button)).performClick();
                }
            };
        }
    };

    private DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int id) {
            return;
        }

        @Override
        public void onDisplayRemoved(int id) {
            return;
        }

        @Override
        public void onDisplayChanged(int id) {
            if(id == displayId){
                Log.w("onDisplayChanged", String.valueOf(imageAnalyzer.getTargetRotation()));
                imageCapture.setTargetRotation(getView().getDisplay().getRotation());
                imageAnalyzer.setTargetRotation(getView().getDisplay().getRotation());
                Log.w("onDisplayChanged", String.valueOf(imageAnalyzer.getTargetRotation()));
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        /*
         Make sure that all permissions are still present, since the
         user could have removed them while the app was in paused state.
        */

        if (!hasPermissions(requireContext())) {
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE);
        }
        if(GpsHelper.checkGpsPermission(getActivity()) != GpsHelper.PERMISSION_STATE_GRANTED) {
            GpsHelper.requestLocationPermission(this, PERMISSION_CODE_GPS);
        }
        orientationHelper.manualRegister();

    }

    @Override
    public void onPause() {
        super.onPause();
        orientationHelper.unregister();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //close camera
        cameraExecutor.shutdown();
        broadcastManager.unregisterReceiver(volumeDownReceiver);
        displayManager.unregisterDisplayListener(displayListener);
        orientationHelper.unregister();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_camera, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(CameraViewModel.class);
    }

    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     *
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Redraw the camera UI controls
        updateCameraUi();

        // Enable or disable switching between cameras
        updateCameraSwitchButton();
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        broadcastManager = LocalBroadcastManager.getInstance(view.getContext());
        cameraExecutor = Executors.newSingleThreadExecutor();
        // Set up the intent filter(target:volunm down button) that will
        // receive events from our main activity
        IntentFilter filter = new IntentFilter("key_event_action");
        broadcastManager.registerReceiver(volumeDownReceiver,filter);

        // Every time the orientation of device changes, update rotation for use cases
        displayManager.registerDisplayListener(
                (DisplayManager.DisplayListener) displayListener, null);

        // Set up the orientation sensor helper
        orientationHelper = new OrientationHelper(getContext(), this);
        orientationDegrees[0] = orientationDegrees[1] = orientationDegrees[2] = 0;

        if ((!hasPermissions(requireContext())) || !(GpsHelper.checkGpsPermission(getActivity()) == GpsHelper.PERMISSION_STATE_GRANTED)) {
            // Request camera-related permissions
            if (!hasPermissions(requireContext())) {
                requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE);
            }
            if (!(GpsHelper.checkGpsPermission(getActivity()) == GpsHelper.PERMISSION_STATE_GRANTED)) {
                GpsHelper.requestLocationPermission(this, PERMISSION_CODE_GPS);
            }
        } else {
            startUpCamera();
        }
    }

    /* Method used to re-draw the camera UI controls,
     * called every time configuration changes. */
    private void updateCameraUi() {

        // Remove previous UI if any
        ConstraintLayout camera_ui_container = getView().findViewById(R.id.camera_ui_container);
        if(camera_ui_container != null)
            ((ConstraintLayout)getView()).removeView(camera_ui_container);



        // Inflate a new view containing all UI for controlling the camera
        View controls = View.inflate(requireContext(),
                R.layout.camera_ui_container, (ConstraintLayout) getView());
        controls.findViewById(R.id.camera_switch_button).setOnClickListener(
                view -> {
                    if(CameraSelector.LENS_FACING_BACK == lensFacing){
                        lensFacing = CameraSelector.LENS_FACING_FRONT;
                    }
                    else if (CameraSelector.LENS_FACING_FRONT == lensFacing){
                        lensFacing = CameraSelector.LENS_FACING_BACK;
                    }
                    bindCameraUseCases();

                }
        );
        controls.findViewById(R.id.camera_capture_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (imageCapture == null) return;

                        if (!GpsHelper.isGpsEnabled(locationManager)) {
                            Toast.makeText(getActivity(), "please turn on location service", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!GpsHelper.isCoordinationValid(GpsHelper.getCoordination(getActivity(), locationManager))) {
                            Toast.makeText(getActivity(), "GPS signal lost", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        orientationHelper.unregister();
                        imageCapture.takePicture(
                                ContextCompat.getMainExecutor(getContext()),
                                new ImageCapture.OnImageCapturedCallback() {
                                    @Override
                                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                                        Bitmap bitmap = imageProxyToBitmap(image);
                                        bitmap = photoPreprocess(bitmap);
                                        //
                                        Bundle gps_bdl = GpsHelper.getCoordination(getActivity(), locationManager);
                                        mViewModel.select(
                                                new Photo(bitmap, System.currentTimeMillis(),  gps_bdl.getDouble("latitude"), gps_bdl.getDouble("longitude"), orientationDegrees[1], orientationDegrees[2])
                                        );
                                        Log.w("in camera fragment", mViewModel.getSelected().getValue().toString());
                                        Navigation.findNavController(
                                                requireActivity(), R.id.camera_nav_host_fragment
                                        ).navigate(R.id.action_camera_fragment_to_preview_fragment);

                                        super.onCaptureSuccess(image);

                                    }

                                    @Override
                                    public void onError(@NonNull ImageCaptureException exception) {
                                        super.onError(exception);
                                    }
                                }
                        );
                        orientationHelper.manualRegister();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            // Display flash animation to indicate that photo was captured
                            getView().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getView().setForeground(new ColorDrawable(Color.WHITE));
                                    getView().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            getView().setForeground(null);
                                        }
                                    },100);

                                }
                            },200);
                        }
                    }
                }
        );



    }

    private int aspectRatio(int width, int height) {
        double previewRatio = Double.max(width, height) /
                Double.min(width, height);

        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    private Bitmap imageProxyToBitmap(ImageProxy image){
        Log.w("imageProxyToBitmap", String.valueOf(image.getFormat()));
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        buffer.rewind();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        return  bitmap;
    }

    private Bitmap photoPreprocess(Bitmap bitmap){
        Matrix matrix = new Matrix();


        matrix.postRotate(90);
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            matrix.postRotate(180);
            matrix.postScale(-1, 1, bitmap.getWidth(), bitmap.getHeight());
        }
        switch (imageAnalyzer.getTargetRotation()){
            case Surface.ROTATION_90:matrix.postRotate(270);break;
            case Surface.ROTATION_270:matrix.postRotate(90);break;
        }


        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(),bitmap.getHeight(), matrix, true);


        return rotatedBitmap;

    }
    private void startUpCamera(){
        View cameraView = getView().findViewById(R.id.cameraView);

        locationManager = GpsHelper.getLocationManager(getActivity());
        //check if location service is enable in the system
        if (!GpsHelper.isGpsEnabled(locationManager)) {
            Toast.makeText(getActivity(), "please turn on location service", Toast.LENGTH_LONG).show();
        }

        //Request live update of GPS location
        GpsHelper.startGpsUpdate(locationManager);
        if (!GpsHelper.isCoordinationValid(GpsHelper.getCoordination(getActivity(), locationManager))) {
            Toast.makeText(getActivity(), "GPS signal lost, please move to an uncovered area", Toast.LENGTH_SHORT).show();
            //while (!GpsHelper.isCoordinationValid(GpsHelper.getCoordination(getActivity(),locationManager))){};
        }

        cameraView.post(
                () -> {
                    Log.w("CameraView.post Runnable","here");
                    displayId = cameraView.getDisplay().getDisplayId();
                    updateCameraUi();
                    setUpCamera();
                }
        );
    }

    private void setUpCamera(){
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture
                = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(
                () -> {
                    try {
                        cameraProvider = cameraProviderFuture.get();
                        // Enable or disable switching between cameras
                        lensFacing = getLensFacing();
                        updateCameraSwitchButton();

                        // Build and bind the camera use cases
                        bindCameraUseCases();

                    } catch (ExecutionException | InterruptedException | CameraInfoUnavailableException e) {
                        e.printStackTrace();
                    }

                }, ContextCompat.getMainExecutor(requireContext())

        );


    }

    /** Enabled or disabled a button to switch cameras depending on the available cameras */
    private void updateCameraSwitchButton() {
        ImageButton switchCamerasButton = getView().findViewById(R.id.camera_switch_button);
        try {
            switchCamerasButton.setEnabled(hasBackCamera() && hasFrontCamera());
        } catch (CameraInfoUnavailableException exception) {
            switchCamerasButton.setEnabled(false);
        }
    }

    private void bindCameraUseCases(){
        DisplayMetrics metrics = new DisplayMetrics();
        getView().findViewById(R.id.cameraView).getDisplay().getRealMetrics(metrics);
        int screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels);
        if (cameraProvider == null)
            throw new IllegalStateException("Camera initialization failed.");


        //select camera direction
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing).build();

        PreviewView cameraView = getView().findViewById(R.id.cameraView);
        //build Preview
        preview = new Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(cameraView.getDisplay().getRotation())
                .build();

        //build Image Capture
        imageCapture =  new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(cameraView.getDisplay().getRotation())
                .build();

        //build image analysis
        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetRotation(cameraView.getDisplay().getRotation())
                .setTargetAspectRatio(screenAspectRatio)
                .build();

        cameraProvider.unbindAll();
        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer);

            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(cameraView.getSurfaceProvider());
        } catch (Exception exc) {
            Log.e("camera fragment", "Use case binding failed", exc);
        }
        setUpPinchToZoom();


    }

    private int getLensFacing() throws CameraInfoUnavailableException {
        if (hasBackCamera())
            return CameraSelector.LENS_FACING_BACK;
        else if (hasFrontCamera())
            return CameraSelector.LENS_FACING_FRONT;
        else
            throw new IllegalStateException("Back and front camera are unavailable");
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private Boolean hasBackCamera() throws CameraInfoUnavailableException {

        return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)?true: false;
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private Boolean hasFrontCamera() throws CameraInfoUnavailableException {
        return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?true: false;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && PERMISSION_GRANTED == grantResults[0]) {
                    // Take the user to the success fragment when permission is granted
                    Toast.makeText(requireContext(), "Permission request granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_LONG).show();
                }

                break;
            case PERMISSION_CODE_GPS:
                if (grantResults.length > 0 && grantResults[0] != PERMISSION_GRANTED) {
                    Toast location_permission_not_granted = Toast.makeText(getActivity(), "Location Permission denied, this is necessary for this activity to work", Toast.LENGTH_SHORT);
                    location_permission_not_granted.show();
                }
                break;
            default:

        }
        if (hasPermissions(requireContext()) && (GpsHelper.checkGpsPermission(getActivity()) == GpsHelper.PERMISSION_STATE_GRANTED)) {
            startUpCamera();
        }

    }

    static private final int PERMISSIONS_REQUEST_CODE = 10;
    static private String[] PERMISSIONS_REQUIRED = new String[] {Manifest.permission.CAMERA};

    //for location
    LocationManager locationManager;
    private static final int PERMISSION_CODE_GPS = 1212;

    static public boolean hasPermissions(Context context){
        for(String permission: PERMISSIONS_REQUIRED){
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PERMISSION_GRANTED)
                return false;
        }
        return true;

    };



    private void setUpPinchToZoom(){
        PreviewView imageView = getView().findViewById(R.id.cameraView);
        ScaleGestureDetector.SimpleOnScaleGestureListener listener =
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {

                        CameraSelector cameraSelector = new CameraSelector.Builder()
                                .requireLensFacing(lensFacing).build();

                        Camera camera = CameraX.getCameraWithCameraSelector(cameraSelector);
                        CameraControl cameraControl = camera.getCameraControl();
                        CameraInfo cameraInfo = camera.getCameraInfo();
                        cameraControl.setZoomRatio(
                                cameraInfo.getZoomState()
                                        .getValue()
                                        .getZoomRatio() * detector.getScaleFactor());


                        return true;

                    }

                };

        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(requireContext(), listener);
        imageView.setOnTouchListener(
                (view, motionEvent) -> {
                    scaleGestureDetector.onTouchEvent(motionEvent);

                    return true;
                }
        );
    }

    public void onOrientationUpdate(float[] orientation) {
        OrientationHelper.getDegreesFromRadian(orientation, orientationDegrees);
    }
}