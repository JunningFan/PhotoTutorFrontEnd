package com.example.phototutor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.helpers.PhotoUploader;
import com.example.phototutor.helpers.ProfileEditor;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.thefuntasty.hauler.DragDirection;
import com.thefuntasty.hauler.HaulerView;
import com.thefuntasty.hauler.OnDragDismissedListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import okhttp3.ResponseBody;
import retrofit2.Call;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class EditProfileActivity extends MyAppCompatActivity {
    private SharedPreferences sharedPreferences;

    TextInputEditText name;
    TextInputEditText bio;
    ImageView userImage;
    Button changeProfilePhotoButton;
    Button updateButton;
    PhotoUploader uploader;
    ACProgressFlower loadingDialog;
    private boolean isAvatarEditted = false;
    private MutableLiveData<Boolean> preloadDone = new MutableLiveData<Boolean>(new Boolean(false));
    private int newAvatarImgID;

    private ProfileEditor.ProfileEditorCallback profileEditorCallback = new ProfileEditor.ProfileEditorCallback() {
        @Override
        public void onFailResponse(String message, int code) {

        }

        @Override
        public void onFailRequest(Call<ResponseBody> call, Throwable t) {
            new MaterialAlertDialogBuilder(EditProfileActivity.this)
                    .setMessage("Upload failed. Do you want to retry?")
                    .setNegativeButton("Cancel", (dialog,which)->{})
                    .setPositiveButton("Retry", (dialog,which)->{})
                    .show();
        }

        @Override
        public void onSuccessResponse() {
            loadingDialog.hide();
            loadingDialog.cancel();
            Toast.makeText(getApplicationContext(), "Details updated", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ((HaulerView)findViewById(R.id.hauler_view)).setOnDragDismissedListener(new OnDragDismissedListener() {
            @Override
            public void onDismissed(@NotNull DragDirection dragDirection) {
                finish();
            }
        });
        name = findViewById(R.id.editTextName);
        bio = findViewById(R.id.editTextBio);
        userImage = findViewById(R.id.userImage);
        changeProfilePhotoButton = findViewById(R.id.changeProfilePhoto);
        updateButton = findViewById(R.id.updateButton);
        uploader = new PhotoUploader(this);

        String initSignature = getIntent().getStringExtra("signature");
        String initNickname = getIntent().getStringExtra("nickname");
        name.setText(initSignature);
        bio.setText(initNickname);

        Uri initAvatarUrl = Uri.parse(getIntent().getStringExtra("avatarUrl"));
        Glide
                .with(this)
                .load(initAvatarUrl)
                .centerCrop()
                .into(userImage);

        changeProfilePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(EditProfileActivity.this);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().isEmpty() || bio.getText().toString().isEmpty()){
                    new MaterialAlertDialogBuilder(EditProfileActivity.this)
                            .setMessage("No empty information accepted")
                            .setNegativeButton("Back",null).show();
                    return;
                }

                loadingDialog = new ACProgressFlower.Builder(EditProfileActivity.this)
                        .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                        .themeColor(Color.WHITE)
                        .text("Updating")
                        .fadeColor(Color.DKGRAY).build();
                loadingDialog.show();
                ProfileEditor profileEditor = new ProfileEditor(EditProfileActivity.this);
                if(!isAvatarEditted){
                    profileEditor.uploadDetails(name.getText().toString(), bio.getText().toString(), profileEditorCallback);
                }else{
                    if(preloadDone.getValue())
                        profileEditor.uploadDetails(name.getText().toString(), bio.getText().toString(),newAvatarImgID, profileEditorCallback);
                    else{
                        preloadDone.observe(EditProfileActivity.this,preloaded->{
                            if(preloaded) profileEditor.uploadDetails(name.getText().toString(), bio.getText().toString(),newAvatarImgID, profileEditorCallback);
                        });
                    }
                }


            }
        });
    }

    private void selectImage(Object context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
    
        AlertDialog.Builder builder = new AlertDialog.Builder((Context) context);
        builder.setTitle("Choose your profile picture");
    
        builder.setItems(options, (dialog, item) -> {

            if (options[item].equals("Take Photo")) {
                if(hasCameraPermissions(this)){
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                }
                else{
                    requestPermissions(new String[]{Manifest.permission.CAMERA},PERMISSIONS_CAMERA_REQUEST_CODE);
                }

            } else if (options[item].equals("Choose from Gallery")) {
                if(hasReadExternalPermissions(this)){
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);
                }
                else{
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_STORAGE_REQUEST_CODE);
                }

            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        userImage.setImageBitmap(selectedImage);
                        Photo photo = new Photo();
                        photo.setBitmap(selectedImage);
                        photo.saveImage(getFilesDir());
                        isAvatarEditted= true;
                        uploadPhoto(photo);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                selectedImage = data.getData();
                                userImage.setImageURI(selectedImage);
                                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                                Log.w("EditProfileActivity",cursor.getString(column_index));
                                Photo photo = new Photo();
                                photo.imageURI = Uri.parse(cursor.getString(column_index));
                                isAvatarEditted = true;
                                uploadPhoto(photo);
                                cursor.close();
                            }

                        }


                    }
                    break;
            }
        }
    }

    static private String[] PERMISSIONS_REQUIRED = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    static private final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 0x1045;
    static private final int PERMISSIONS_CAMERA_REQUEST_CODE = 10;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_REQUEST_CODE:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
                break;
            case PERMISSIONS_CAMERA_REQUEST_CODE:
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
                break;

        }
    }

    static public boolean hasReadExternalPermissions(Context context){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PERMISSION_GRANTED;

    };

    static public boolean hasCameraPermissions(Context context){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PERMISSION_GRANTED;
    };



    private void uploadPhoto(Photo photo){
        uploader.uploadPhoto(photo,new PhotoUploader.PhotoUploaderCallback(){
            @Override
            public void onFailResponse(String message, int code) {
                Log.e("uploadPhoto",message);
            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                Log.e("uploadPhoto",t.getMessage());
            }

            @Override
            public void onSuccessResponse(int imgId) {
                preloadDone.setValue(true);
                newAvatarImgID = imgId;
            }
        });
    }
}