package com.example.phototutor.cameraFragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phototutor.Photo.Photo;

public class CameraViewModel extends ViewModel {
    private final MutableLiveData<Photo> selected = new MutableLiveData<Photo>();

    public void select(Photo item) {
        selected.postValue(item);
    }

    public LiveData<Photo> getSelected() {
        return selected;
    }

}