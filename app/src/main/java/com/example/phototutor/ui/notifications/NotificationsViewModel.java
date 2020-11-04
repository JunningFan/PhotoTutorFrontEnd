package com.example.phototutor.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phototutor.Photo.CloudPhoto;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class NotificationsViewModel extends ViewModel {
    private List<CloudPhoto> photoList = new ArrayList<>();
    private MutableLiveData<List<CloudPhoto>> photoListLiveData = new MutableLiveData<>();

    public MutableLiveData<List<CloudPhoto>> getPhotoList() {
        return photoListLiveData;
    }


    public void addPhotosToList(List<CloudPhoto> photoList) {
        photoList.addAll(photoList);
        this.photoListLiveData.setValue(photoList);
    }
}