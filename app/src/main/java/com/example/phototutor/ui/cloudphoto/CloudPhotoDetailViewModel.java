package com.example.phototutor.ui.cloudphoto;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phototutor.Photo.CloudPhoto;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.Photo.PhotoDatabase;

import java.util.List;

public class CloudPhotoDetailViewModel extends ViewModel {
    static private String TAG = "CloudPhotoDetailViewModel";
    private MutableLiveData<List<CloudPhoto>> allPhotos = new MutableLiveData<>();
    private MutableLiveData<Integer> currIdx = new MutableLiveData<>();

    public void setDataset(List<CloudPhoto> photo){
        Log.w(TAG,"setDataset "+photo.size());
        allPhotos.setValue(photo);
    }

    public MutableLiveData<List<CloudPhoto>> getDataset(){
        return allPhotos;
    }

    public void select(int i){
        currIdx.setValue(i);
    }

    public MutableLiveData<Integer> getCurrIdx(){
        return currIdx;
    }

}
