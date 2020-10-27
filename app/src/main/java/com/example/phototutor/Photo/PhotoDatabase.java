package com.example.phototutor.Photo;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.Update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {Photo.class},version = 1)
@TypeConverters({Converter.class})
public abstract class PhotoDatabase extends RoomDatabase {
    public abstract PhotoDAO photoDAO();

    private static volatile PhotoDatabase instance;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static public PhotoDatabase getDatabase(final Context context){
        if (instance == null) {
            synchronized (PhotoDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            PhotoDatabase.class, "photo_album")
                            .build();
                }
            }
        }
        return instance;
    }

    public void insertPhotos(Photo... photos){
        databaseWriteExecutor.execute(
                () -> {
                    photoDAO().insertPhotos(photos);
                }
        );
    }

    public void deletePhotos(Photo... photos){
        databaseWriteExecutor.execute(
                () -> {
                    Log.w("Database","delete " + photos[0].id);
                    photoDAO().deletePhotos(photos);
                }
        );
    }
    public void updatePhotos(Photo... photos){
        databaseWriteExecutor.execute(
                () -> {
                    Log.w("Database","update " + photos[0].id);
                    photoDAO().updatePhotos(photos);
                }
        );
    }
}