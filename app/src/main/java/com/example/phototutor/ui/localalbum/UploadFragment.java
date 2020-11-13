package com.example.phototutor.ui.localalbum;

import android.content.DialogInterface;
import android.inputmethodservice.ExtractEditText;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import me.gujun.android.taggroup.TagGroup;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class UploadFragment extends Fragment {

    private LocalAlbumViewModel mViewModel;
    private Photo photo;
    private String TAG = "UploadFragment";

    View view;

    private MutableLiveData<Boolean> preloadDone = new MutableLiveData<Boolean>(new Boolean(false));
    private int imgId = -1;

    PhotoUploader photoUploader;
    EditText titleEditText;
    // tags
    TagGroup mTagGroup;
    // weather
    AutoCompleteTextView weatherExposedMenu;

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
            if(pos >= photos.size()){return;}
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

        mTagGroup = (TagGroup) view.findViewById(R.id.photo_tags);
        mTagGroup.setTags(new String[]{});
        mTagGroup.getTags();

        String[] weathers = new String[]{Photo.CLEAR, Photo.PARTLY_CLOUDY, Photo.MOSTLY_CLOUDY, Photo.OVERCAST, Photo.RAIN, Photo.SNOW, Photo.MISTY, Photo.UNKNOWN};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this.getContext(),
                        R.layout.weather_dropdown_item,
                        weathers);
        weatherExposedMenu = (AutoCompleteTextView)getView().findViewById(R.id.filled_exposed_dropdown);
        weatherExposedMenu.setAdapter(adapter);
        weatherExposedMenu.setText(photo.weather);
        weatherExposedMenu.setInputType(InputType.TYPE_NULL);
        adapter.getFilter().filter(null);
        weatherExposedMenu.showDropDown();
        weatherExposedMenu.dismissDropDown();
        weatherExposedMenu.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!weatherExposedMenu.getText().toString().equals(""))
                            adapter.getFilter().filter(null);
                        weatherExposedMenu.showDropDown();
                        return false;
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
        String[] tags = mTagGroup.getTags();
        Log.w(TAG,weatherExposedMenu.getText().toString());

        photoUploader.uploadPhotoInfo(photo, imgId, title, tags, weatherExposedMenu.getText().toString(),
                new PhotoUploader.PhotoInfoUploaderCallback() {
                    @Override
                    public void onFailResponse(String message, int code) {
                        Toast.makeText(getContext(), "Server error, please try again later or contact technical support", Toast.LENGTH_SHORT).show();
                        unlockViews();
                    }

                    @Override
                    public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                        Log.e(
                                TAG,
                                "---TTTT :: POST msg from server :: " + t.toString()
                        );
                        Toast.makeText(getContext(),"Network issue, please try again later", Toast.LENGTH_SHORT).show();
                        unlockViews();
                    }

                    @Override
                    public void onSuccessResponse() {
                        Toast.makeText(getContext(), "Upload Complete", Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed();
                    }

                }

        );
    }

    private void lockViews(){
        view.findViewById(R.id.btn_submit).setClickable(false);
        view.findViewById(R.id.photo_tags).setFocusable(false);
        view.findViewById(R.id.et_title).setFocusable(false);
        ((TextView)view.findViewById(R.id.btn_submit)).setText("Submitting");
        view.findViewById(R.id.filled_exposed_dropdown).setFocusable(false);

    }

    private void unlockViews() {
        view.findViewById(R.id.btn_submit).setClickable(true);
        view.findViewById(R.id.photo_tags).setFocusable(true);
        view.findViewById(R.id.et_title).setFocusable(true);
        ((TextView)view.findViewById(R.id.btn_submit)).setText("Submit");
        view.findViewById(R.id.filled_exposed_dropdown).setFocusable(true);

    }

    private void uploadImage() {
        photoUploader.uploadPhoto(photo,
                new PhotoUploader.PhotoUploaderCallback() {
                    @Override
                    public void onFailResponse(String message, int code) {
                        Toast.makeText(getContext(), "Server error, please try again later or contact technical support", Toast.LENGTH_SHORT).show();
                        unlockViews();
                        uploadImage();
                    }

                    @Override
                    public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getContext(),"Network issue, please try again later", Toast.LENGTH_SHORT).show();
                        unlockViews();
                        uploadImage();
                    }

                    @Override
                    public void onSuccessResponse(int id) {
                        preloadDone.postValue(new Boolean(true));
                        imgId = id;
                    }

                }
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.getSelected().removeObservers(this);
        mViewModel.getAllPhotos().removeObservers(this);
    }
}