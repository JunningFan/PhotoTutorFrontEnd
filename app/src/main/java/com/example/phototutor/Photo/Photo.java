package com.example.phototutor.Photo;

import android.graphics.Bitmap;
import android.util.Pair;

import androidx.camera.core.ImageProxy;

public class Photo {
    private Bitmap photo;
    private String title;
    private int iso;
    private int focal_length;
    private int aperture;
    private int shutter_speed;
    private int timestamp;
    private Pair<Double, Double> location;
    private double orientation;
    private double elevation;

    public Photo(Bitmap photo){
        this.photo = photo;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public Photo(Bitmap photo, String title, int iso, int focal_length,
                 int aperture, int shutter_speed,
                 int timestamp, Pair<Double, Double> location,
                 double orientation, double elevation) {
        this.photo = photo;
        this.title = title;
        this.iso = iso;
        this.focal_length = focal_length;
        this.aperture = aperture;
        this.shutter_speed = shutter_speed;
        this.timestamp = timestamp;
        this.location = location;
        this.orientation = orientation;
        this.elevation = elevation;
    }

}
