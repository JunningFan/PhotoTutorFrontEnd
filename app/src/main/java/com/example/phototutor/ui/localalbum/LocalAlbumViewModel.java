package com.example.phototutor.ui.localalbum;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phototutor.Photo.Photo;
import com.example.phototutor.Photo.PhotoDAO;
import com.example.phototutor.Photo.PhotoDatabase;

import java.util.List;

public class LocalAlbumViewModel extends ViewModel {

    private LiveData<List<Photo>> allPhotos;
    private MutableLiveData<Integer> currentSelect = new MutableLiveData<Integer>();;

    LiveData<List<Photo>> getAllPhotos(){
        return allPhotos;
    }

    public void loadDatabase(Context context){
        PhotoDatabase db = PhotoDatabase.getDatabase(context);
        PhotoDAO photoDao = db.photoDAO();
        allPhotos = photoDao.loadAllPhotosSortedByTime();
    }

    public void select(int pos){
        currentSelect.setValue(pos);
    }

    public LiveData<Integer> getSelected() {
        return currentSelect;
    }
}