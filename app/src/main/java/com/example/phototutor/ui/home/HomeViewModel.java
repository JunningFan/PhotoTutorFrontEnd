package com.example.phototutor.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phototutor.Photo.CloudPhoto;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<CloudPhoto>> photos;

    public HomeViewModel() {
        photos = new MutableLiveData<>();
    }

    public void setPhotos(List<CloudPhoto> photos) {
        this.photos.setValue(photos);
    }

    public MutableLiveData<List<CloudPhoto>> getPhotos() {
        return photos;
    }
}