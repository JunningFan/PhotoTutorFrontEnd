package com.example.phototutor.Photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import androidx.room.TypeConverter;

import java.io.ByteArrayOutputStream;
import java.net.URI;

class Converter {
    @TypeConverter
    static public double DoubleTodouble(Double num){
        return num.doubleValue();
    }

    @TypeConverter
    static public Double DoubleTodouble(double num){
        return new Double(num);
    }

    @TypeConverter
    static public String uriToString(Uri uri){
        return uri.toString();
    }

    @TypeConverter
    static public Uri uriToString(String uri){
        return Uri.parse(uri);
    }
}
