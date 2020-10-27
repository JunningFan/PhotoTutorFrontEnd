package com.example.phototutor.ui.localalbum;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phototutor.Photo.Photo;
import com.example.phototutor.Photo.PhotoDAO;
import com.example.phototutor.Photo.PhotoDatabase;

import java.util.List;

public class LocalAlbumViewModel extends ViewModel {

    private LiveData<List<Photo>> allPhotos;
    private final MutableLiveData<Integer> currentSelect = new MutableLiveData<>();

    LiveData<List<Photo>> getAllPhotos(){
        return allPhotos;
    }

    public void loadDatabase(Context context){
        PhotoDatabase db = PhotoDatabase.getDatabase(context);
        PhotoDAO photoDao = db.photoDAO();
        allPhotos = photoDao.loadAllPhotosSortedByTime();
    }


    public void select(Integer pos){
        Log.w("LocalAlbumViewModel",pos.toString());
        currentSelect.setValue(pos);
        Log.w("LocalAlbumViewModel",currentSelect.toString());
    }

    public LiveData<Integer> getSelected() {
        return currentSelect;
    }

    public void updateDataset(Context context, Photo photo){
        PhotoDatabase db = PhotoDatabase.getDatabase(context);
        db.updatePhotos(photo);

    }

    public void deletePhoto(Context context, Photo photo){
        PhotoDatabase db = PhotoDatabase.getDatabase(context);
        photo.delete();
        db.deletePhotos(photo);
    }
}