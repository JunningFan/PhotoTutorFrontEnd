package com.example.phototutor.ui.localalbum;

import android.inputmethodservice.ExtractEditText;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.example.phototutor.helpers.PhotoUploader;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadFragment extends DialogFragment {

    private LocalAlbumViewModel mViewModel;
    private Photo photo;
    private String TAG = "UploadFragment";
    private String authKey ="eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJJRCI6MSwiQWNjZXNzIjp0cnVlLCJFeHBpcmUiOjE2MDQyMzMwNDZ9.RHeOoS2lMgAa2HLTTHUVsjNqUxOAxf9XUBgYxlvHY_CB68aUdl6BfUBCSjOU9jHw9W-ADt46xMqg5dH9pNIWuQ";

    View view;

    private MutableLiveData<Boolean> preloadDone = new MutableLiveData<Boolean>(new Boolean(false));
    private int imgId = -1;

    PhotoUploader photoUploader;
    EditText titleEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        int pos = getArguments().getInt("pos");
        mViewModel = ViewModelProviders.of(requireActivity()).get(LocalAlbumViewModel.class);
        mViewModel.getAllPhotos().observe(requireActivity(), photos ->{
            photo = photos.get(pos);
            ImageView imageView = view.findViewById(R.id.image_view);
            Glide.with(view)
                    .load(photo.imageURI)
                    .placeholder(R.drawable.ic_loading)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imageView);


        });

        titleEditText = ((TextInputLayout)view.findViewById(R.id.title_edit_container)).getEditText();
        photoUploader = new PhotoUploader(getContext());

        uploadImage();
        //
        view.findViewById(R.id.btn_submit).setOnClickListener(
                view1 -> {
                    lockViews();
                    if(preloadDone.getValue()) { // case: photo uploading is completed
                        uploadInfo();
                    } else { // case: photo is still uploading
                        preloadDone.observe(getActivity(), aBoolean -> {
                            if(preloadDone.getValue())
                                uploadInfo();
                        });
                    }
                }
        );
    }

    //
    private void uploadInfo() {
       photoUploader = new PhotoUploader(getContext());
        String title = titleEditText.getText().toString();
        if(title.isEmpty()) {
            title = "Untitled";
        }
        photoUploader.uploadPhotoInfo(authKey, photo, imgId, title,
                new PhotoUploader.PhotoUploaderCallback() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d(this.getClass().getSimpleName(), response.toString());
                        if(response.isSuccessful()) {
                            Toast.makeText(getContext(), "Upload Complete", Toast.LENGTH_SHORT).show();
                            getActivity().onBackPressed();
                        } else {
                            Toast.makeText(getContext(), "Server error, please try again later or contact technical support", Toast.LENGTH_SHORT).show();
                            unlockViews();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(
                                TAG,
                                "---TTTT :: POST msg from server :: " + t.toString()
                        );
                        Toast.makeText(getContext(),"Network issue, please try again later", Toast.LENGTH_SHORT).show();
                        unlockViews();
                    }
                }

        );
    }

    private void lockViews(){
        view.findViewById(R.id.btn_submit).setClickable(false);
        view.findViewById(R.id.et_desc).setFocusable(false);
        view.findViewById(R.id.et_title).setFocusable(false);
        ((TextView)view.findViewById(R.id.btn_submit)).setText("Submitting");

    }

    private void unlockViews() {
        view.findViewById(R.id.btn_submit).setClickable(true);
        view.findViewById(R.id.et_desc).setFocusable(true);
        view.findViewById(R.id.et_title).setFocusable(true);
        ((TextView)view.findViewById(R.id.btn_submit)).setText("Submit");

    }

    private void uploadImage() {
        photoUploader.uploadPhoto(authKey
                ,
                photo,
                new PhotoUploader.PhotoUploaderCallback() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.w(TAG,response.toString());
                        if(response.isSuccessful()) {
                            preloadDone.postValue(new Boolean(true));
                            try {
                                imgId = new JSONObject(response.body().string()).getInt("img");
                            } catch (JSONException e) {
                                Log.e(this.getClass().getSimpleName(), e.getMessage());
                            } catch (IOException e) {
                                Log.e(this.getClass().getSimpleName(), e.getMessage());
                            }
                        } else {
                            Toast.makeText(getContext(), "Server error, please try again later or contact technical support", Toast.LENGTH_SHORT).show();
                            unlockViews();
                            uploadImage();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(
                                TAG,
                                "---TTTT :: POST msg from server :: " + t.toString()
                        );
                        Toast.makeText(getContext(),"Network issue, please try again later", Toast.LENGTH_SHORT).show();
                        unlockViews();
                        uploadImage();
                    }
                }
        );
    }
}