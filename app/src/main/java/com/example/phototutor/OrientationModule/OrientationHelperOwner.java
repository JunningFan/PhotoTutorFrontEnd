package com.example.phototutor.OrientationModule;

public interface OrientationHelperOwner {
    public abstract void onOrientationUpdate(float[] orientation); //In radian form: [0]: roll, [1]:pitch, [2]:azimuth
}
