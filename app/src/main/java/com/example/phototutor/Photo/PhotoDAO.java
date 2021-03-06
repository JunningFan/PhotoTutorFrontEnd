package com.example.phototutor.Photo;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PhotoDAO{

    @Insert
    public void insertPhotos(Photo... photos);

    @Update(entity = Photo.class)
    public void updatePhotos(Photo... photos);

    @Query("SELECT * FROM photo_album WHERE id=:id ")
    public LiveData<Photo> getPhotoById(int id);

    @Delete
    public void deletePhotos(Photo... photos);

    @Query("SELECT * FROM photo_album")
    public LiveData<List<Photo>> loadAllPhotos();

    @Query("SELECT * FROM photo_album order by timestamp ")
    public LiveData<List<Photo>> loadAllPhotosSortedByTime();

    @Query("DELETE FROM PHOTO_ALBUM")
    public void clearPhotos();
}