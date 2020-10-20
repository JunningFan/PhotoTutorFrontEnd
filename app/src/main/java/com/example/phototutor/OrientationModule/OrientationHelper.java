package com.example.phototutor.OrientationModule;

import android.Manifest;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import static java.lang.Math.PI;

public class OrientationHelper implements SensorEventListener {

    private Context context;
    private OrientationHelperOwner owner;

    private SensorManager mSensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;
    private Sensor rotationVectorSensor;

    private final float[] rotationVector = new float[4];


    private final float[] rotationMatrix = new float[9];
    private final float[] rotationMatrix_rotated = new float[9];

    private final float[] orientationAngles = new float[3];
    private float lastKnownA = -200;

    public OrientationHelper(Context context, OrientationHelperOwner owner) {
        this.context = context;
        this.owner = owner;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        rotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mSensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.v(OrientationHelper.class.getSimpleName(), "sensor event captured");
        switch (event.sensor.getType()) {

            case Sensor.TYPE_ROTATION_VECTOR:
                rotationVector[0] = event.values[0];
                rotationVector[1] = event.values[1];
                rotationVector[2] = event.values[2];
                rotationVector[3] = event.values[3];
                updateAngles();
                break;
            default:
        }
    }

    private void updateAngles() {

        mSensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);
        int rotation;
        //get 90-degree step application orientation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rotation = context.getDisplay().getRotation();  //context.getDisplay is only available after API Level 30
        } else {
            rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        }
        //rotate the rotation matrix for different orientation (the A,P,R axis are different in landscape and portrait mode )
        //select different axisX&Y reference for different app orientation
        int axisX, axisY;
        axisX = SensorManager.AXIS_X;
        axisY = SensorManager.AXIS_Y;

        switch (rotation) {
            case Surface.ROTATION_0:
                axisX = SensorManager.AXIS_X;
                axisY = SensorManager.AXIS_Y;
                break;

            case Surface.ROTATION_90:
                axisX = SensorManager.AXIS_Y;
                axisY = SensorManager.AXIS_MINUS_X;
                break;

            case Surface.ROTATION_180:
                axisX = SensorManager.AXIS_MINUS_X;
                axisY = SensorManager.AXIS_MINUS_Y;
                break;

            case Surface.ROTATION_270:
                axisX = SensorManager.AXIS_MINUS_Y;
                axisY = SensorManager.AXIS_X;
                break;

            default:
                break;
        }
        mSensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, rotationMatrix_rotated);

        mSensorManager.getOrientation(rotationMatrix_rotated, orientationAngles);

        /*
        double theta = Math.acos(rotationVector[3])*2;
        double sinv = Math.sin(theta/2);

         orientationAngles[0] = (float) (rotationVector[0]/sinv);
         orientationAngles[1] = (float) (rotationVector[1]/sinv);
         orientationAngles[2] = (float) (rotationVector[2]/sinv);

         */
        orientationAngles[2] =orientationAngles[0]; //record azimuth
        orientationAngles[0] = (float) Math.atan2(rotationMatrix[6], rotationMatrix[7]);
        orientationAngles[1] = (float) Math.acos(rotationMatrix[8]);
        //orientationAngles[2] = - (float) Math.atan2(rotationMatrix[2], rotationMatrix[5]);
        //orientationAngles[2] = - (float) Math.atan2(rotationMatrix[2], rotationMatrix[5]);

        switch (rotation) {
            case Surface.ROTATION_0:
                orientationAngles[0] = orientationAngles[0];
                break;

            case Surface.ROTATION_90:
                orientationAngles[0] = (float)(orientationAngles[0] - PI/2);
                break;

            case Surface.ROTATION_180:
                orientationAngles[0] =  (float)(orientationAngles[0] - PI);
                break;

            case Surface.ROTATION_270:
                orientationAngles[0] = (float)(orientationAngles[0] + PI/2);
                break;

            default:
                break;
        }

        if(orientationAngles[1] > Math.PI/2 + 0.017){
            if(orientationAngles[2] <= 0) {
                orientationAngles[2] =(float) (orientationAngles[2] + Math.PI);
            } else {
                orientationAngles[2] =(float)((orientationAngles[2] + Math.PI) % Math.PI - Math.PI);
            }

        }

        if(lastKnownA != -200 && orientationAngles[1] > (Math.PI/2- 0.034) && orientationAngles[1] < (Math.PI/2 + 0.051)) {
            orientationAngles[2] = lastKnownA;
        }
        lastKnownA = orientationAngles[2];

        owner.onOrientationUpdate(orientationAngles);
    }

    public void unregister() {
        mSensorManager.unregisterListener(this);
    }

    public void manualRegister() {
        mSensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //converts orientation data from radian form degree form
    //deg[2]: compass(0~360)
    public static void getDegreesFromRadian(float[] rad, float[] deg) {
        deg[0] = (float)(rad[0] / PI * 180);
        deg[1] =  (float)(rad[1] / PI * 180);
        deg[2] =  (float)((rad[2] / PI * 180 + 360 )%360);
    }


}
