package com.example.phototutor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EditProfileActivity extends AppCompatActivity {
    EditText name;
    EditText bio;
    ImageView userImage;
    TextView changeProfilePhotoButton;
    Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        name = findViewById(R.id.editTextName);
        bio = findViewById(R.id.editTextBio);
        userImage = (ImageView) findViewById(R.id.userImage);
        changeProfilePhotoButton=(TextView)findViewById(R.id.changeProfilePhoto);
        updateButton = findViewById(R.id.updateButton);

        int imageResource = getResources().getIdentifier("@drawable/avatar", null, this.getPackageName());
        userImage.setImageResource(imageResource);

        changeProfilePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("onClick!", "change profile button is clicked");
                selectImage(EditProfileActivity.this);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name = name.getText().toString();
                String Bio = bio.getText().toString();
                Integer Photo = 1;
                if(name.getText().toString().isEmpty()) {
                    Name = null;
                }
                if(bio.getText().toString().isEmpty()) {
                    Bio = null;
                }
                Toast.makeText(getApplicationContext(), "Details updated", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void selectImage(Object context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
    
        AlertDialog.Builder builder = new AlertDialog.Builder((Context) context);
        builder.setTitle("Choose your profile picture");
    
        builder.setItems(options, new DialogInterface.OnClickListener() {
    
            @Override
            public void onClick(DialogInterface dialog, int item) {
    
                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
    
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);
    
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
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
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }
}