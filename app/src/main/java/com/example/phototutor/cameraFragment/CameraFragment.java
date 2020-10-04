package com.example.phototutor.cameraFragment;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.phototutor.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {
    private CameraViewModel mViewModel;
    @Nullable private Preview preview = null;
    @Nullable private ImageCapture imageCapture = null;
    @Nullable private ImageAnalysis imageAnalyzer = null;
    @Nullable private Camera camera = null;
    @Nullable private ProcessCameraProvider cameraProvider = null;
    @Nullable private ExecutorService cameraExecutor = null;
    private  LocalBroadcastManager broadcastManager;
    private DisplayManager displayManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayManager= (DisplayManager)requireContext().getSystemService(Context.DISPLAY_SERVICE);


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
            if(id == getView().getId()){
                imageCapture.setTargetRotation(getView().getDisplay().getRotation());
                imageAnalyzer.setTargetRotation(getView().getDisplay().getRotation());
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

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //close camera
        cameraExecutor.shutdown();
        broadcastManager.unregisterReceiver(volumeDownReceiver);
        displayManager.unregisterDisplayListener(displayListener);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_camera, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);
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
        // Set up the intent filter(target:volumn down button) that will
        // receive events from our main activity
        IntentFilter filter = new IntentFilter("key_event_action");
        broadcastManager.registerReceiver(volumeDownReceiver,filter);

        // Every time the orientation of device changes, update rotation for use cases
        displayManager.registerDisplayListener(
                (DisplayManager.DisplayListener) displayListener, null);

        if (!hasPermissions(requireContext())) {
            // Request camera-related permissions
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE);
        }
        else{
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
        controls.findViewById(R.id.camera_capture_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (imageCapture == null) return;

                        imageCapture.takePicture(
                               ContextCompat.getMainExecutor(getContext()),
                                new ImageCapture.OnImageCapturedCallback() {
                                    @Override
                                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                                        super.onCaptureSuccess(image);
                                        Log.d("take_photo", image.toString());
                                    }

                                    @Override
                                    public void onError(@NonNull ImageCaptureException exception) {
                                        super.onError(exception);
                                    }
                                }
                        );
                    }
                }
        );


    }
    private void startUpCamera(){
        View cameraView = getView().findViewById(R.id.cameraView);

        cameraView.post(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.w("CameraView.post Runnable","here");
                        updateCameraUi();
                        setUpCamera();
                    }
                }
        );
    }

    private void setUpCamera(){
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture
                            = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            cameraProvider = cameraProviderFuture.get();
                            // Enable or disable switching between cameras
                            updateCameraSwitchButton();

                            // Build and bind the camera use cases
                            bindCameraUseCases();

                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }

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
        if (cameraProvider == null)
            throw new IllegalStateException("Camera initialization failed.");
        try{

            //select camera direction
            CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(getLensFacing()).build();

            //build Preview
            Preview.Builder previewBuilder = new Preview.Builder();
            PreviewView cameraView = getView().findViewById(R.id.cameraView);
            previewBuilder.setTargetRotation(cameraView.getDisplay().getRotation());
            preview = previewBuilder.build();

            //build Image Capture
            ImageCapture.Builder imageCaptureBuilder = new ImageCapture.Builder();
            imageCaptureBuilder.setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY);
            imageCaptureBuilder.setTargetRotation(cameraView.getDisplay().getRotation());
            imageCapture = imageCaptureBuilder.build();

            //build image analysis
            ImageAnalysis.Builder imageAnalysisBuilder = new ImageAnalysis.Builder();
            imageAnalysisBuilder.setTargetRotation(cameraView.getDisplay().getRotation());
            imageAnalyzer = imageAnalysisBuilder.build();

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




        }
        catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }


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
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length>0 && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                // Take the user to the success fragment when permission is granted
                startUpCamera();
                Toast.makeText(requireContext(), "Permission request granted", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_LONG).show();
            }
        }

    }

    static private int PERMISSIONS_REQUEST_CODE = 10;
    static private String[] PERMISSIONS_REQUIRED = new String[] {Manifest.permission.CAMERA};
    static public boolean hasPermissions(Context context){
        for(String permission: PERMISSIONS_REQUIRED){
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;

    };
}