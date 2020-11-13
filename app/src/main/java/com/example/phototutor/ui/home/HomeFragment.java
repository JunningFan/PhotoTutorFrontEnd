package com.example.phototutor.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;


import com.example.phototutor.MainActivity;
import com.example.phototutor.Photo.CloudPhoto;
import com.example.phototutor.Photo.Photo;
import com.example.phototutor.R;
import com.example.phototutor.adapters.AlbumAdapter;
import com.example.phototutor.adapters.CloudAlbumAdapter;
import com.example.phototutor.helpers.PhotoDownloader;
import com.example.phototutor.ui.cloudphoto.CloudPhotoDetailViewModel;
import com.example.phototutor.ui.login.ui.login.LoginActivity;
import com.fivehundredpx.greedolayout.GreedoLayoutManager;
import com.fivehundredpx.greedolayout.GreedoSpacingItemDecoration;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private CloudPhotoDetailViewModel cloudPhotoDetailViewModel;
        private String TAG ="HomeFragment";
    private CloudAlbumAdapter adapter;
    private RecyclerView cloud_photo_gallery;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isScrolling = false;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    MutableLiveData<Double[]> coordinate = new MutableLiveData<Double[]>(new Double[]{Double.valueOf(720), Double.valueOf(720)});
    private final int FUSED_LOCATION_REQUEST_CODE = 0;

    MaterialToolbar topAppBar;

    //define distances
    private final float[] DISTANCE_FACTORS = new float[]{0.25f, 0.5f, 1f, 2f, 4f, 8f, 16f};
    private final String[] DISTANCE_STRINGS = new String[]{"250M", "500M", "1KM", "2KM", "4KM", "8KM", "16KM"};

    AutoCompleteTextView distanceExposedMenu;
    ArrayAdapter<String> menuAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        cloudPhotoDetailViewModel = ViewModelProviders.of(requireActivity()).get(CloudPhotoDetailViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;

    }

    @SuppressLint("MissingPermission")
    private void listenLocationChange() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(100);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location: locationResult.getLocations()) {
                    if(location != null) {
                        Log.d(this.getClass().getSimpleName(), "change" + coordinate.getValue()[0].toString() +" " +  coordinate.getValue()[1].toString());
                        coordinate.setValue(new Double[]{location.getLatitude(), location.getLongitude()});
                    }
                }
            }
        };
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    Log.d(this.getClass().getSimpleName(), "change" + coordinate.getValue()[0].toString() +" " +  coordinate.getValue()[1].toString());
                    coordinate.setValue(new Double[]{location.getLatitude(), location.getLongitude()});

                }
            }
        });
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void configTopAppBarItemSelectedListener(MaterialToolbar topAppBar ){
        topAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.navigation_logout:
                    LoginActivity.userLogout();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    startActivity(intent);
                    break;
                default:return false;
            }

            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FUSED_LOCATION_REQUEST_CODE:
                if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    listenLocationChange();
                } else {
                    Toast.makeText(requireContext(), "location information is necessary for the photo recommendation service", Toast.LENGTH_SHORT);
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, FUSED_LOCATION_REQUEST_CODE);
                }
                break;
            default:
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        configTopAppBarItemSelectedListener(view.findViewById(R.id.topAppBar));
        topAppBar = (MaterialToolbar)view.findViewById(R.id.topAppBar);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if(requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && requireActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, FUSED_LOCATION_REQUEST_CODE);
        } else {
            listenLocationChange();
        }

        coordinate.observe(getViewLifecycleOwner(), observer -> {
            if(coordinate.getValue()[0] != 720) {
                try {
                    Log.d(this.getClass().getSimpleName(), "coordination" + coordinate.getValue()[0].toString() + " " + coordinate.getValue()[1].toString());
                    Geocoder geocoder = new Geocoder(requireContext());
                    String po;
                    String adminArea;
                    String locality;
                    try {
                         po = (geocoder.getFromLocation(coordinate.getValue()[0], coordinate.getValue()[1], 1)).get(0).getPostalCode();
                         adminArea = (geocoder.getFromLocation(coordinate.getValue()[0], coordinate.getValue()[1], 1)).get(0).getAdminArea();
                         locality = (geocoder.getFromLocation(coordinate.getValue()[0], coordinate.getValue()[1], 1)).get(0).getLocality();
                    } catch (IndexOutOfBoundsException e) {
                        // not in area with valid po. admin and locality
                        locality = po = "Unknown";
                    } catch (IllegalArgumentException e) {
                        // wierd latlng readling
                        locality = po = "Unknown";

                    }
                    topAppBar.setTitle(locality );// + " " + po);
                } catch (IOException e) {
                    topAppBar.setTitle("Disconnected");
                }
            }



        });
        Log.d("home_frag", Boolean.toString(topAppBar.getMenu().findItem(R.id.range).getActionView().findViewById(R.id.distance_filled_exposed_dropdown) == null));

        String[] distances = DISTANCE_STRINGS.clone();
        menuAdapter =
                new ArrayAdapter<>(
                        this.getContext(),
                        R.layout.weather_dropdown_item,
                        distances);
        distanceExposedMenu = (AutoCompleteTextView)topAppBar.getMenu().findItem(R.id.range).getActionView().findViewById(R.id.distance_filled_exposed_dropdown);
        distanceExposedMenu.setAdapter(menuAdapter);
        distanceExposedMenu.setText(distances[2]);
        distanceExposedMenu.setInputType(InputType.TYPE_NULL);
        menuAdapter.getFilter().filter(null);
        distanceExposedMenu.clearFocus();
        //distanceExposedMenu.showDropDown();
        distanceExposedMenu.dismissDropDown();

        distanceExposedMenu.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!distanceExposedMenu.getText().toString().equals(""))
                            menuAdapter.getFilter().filter(null);
                        distanceExposedMenu.showDropDown();
                        return false;
                    }
                }
        );

        distanceExposedMenu.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                refreshPhotos();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        cloud_photo_gallery = view.findViewById(R.id.cloud_photo_gallery);
        adapter = new CloudAlbumAdapter(requireContext());
        adapter.setPhotos(new ArrayList<>());
        GreedoLayoutManager layoutManager = new GreedoLayoutManager(adapter);

        cloud_photo_gallery.setLayoutManager(layoutManager);
        int spacing =  (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                requireContext().getResources().getDisplayMetrics());
        cloud_photo_gallery.addItemDecoration(new GreedoSpacingItemDecoration(spacing));

        cloud_photo_gallery.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.swap_fresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> refreshPhotos());

        swipeRefreshLayout.setRefreshing(true);
        cloud_photo_gallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int currentItem = layoutManager.getChildCount();
                int totalItems = layoutManager.getItemCount();
                int scrollOutItems = layoutManager.findFirstVisibleItemPosition();
                if (isScrolling && currentItem + scrollOutItems == totalItems){
                    swipeRefreshLayout.setRefreshing(true);
                    isScrolling = false;
                    downloadPhotos();

                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState ==RecyclerView.SCROLL_STATE_DRAGGING) {
                    isScrolling = true;
                }
            }
        });
    }

    private void refreshPhotos(){
        adapter.setPhotos(new ArrayList<>());
        downloadPhotos();
    }




    private void downloadPhotos(){

        if (coordinate.getValue()[0] == 720) {
            Observer<Double[]> observerValidLocation = new Observer<Double[]>() {
                @Override
                public void onChanged(Double[] doubles) {
                    if(coordinate.getValue()[0] != 720) {
                        coordinate.removeObserver(this);
                        downloadPhotosWithValidGeo();
                    }
                }
            };
            coordinate.observe(getViewLifecycleOwner(), observerValidLocation);
        } else {
            downloadPhotosWithValidGeo();
        }
    }

    private void downloadPhotosWithValidGeo() {
        PhotoDownloader downloader = new PhotoDownloader(requireContext());
        downloader.downloadPhotosByGeo( coordinate.getValue()[0], coordinate.getValue()[1],adapter.getItemCount(),30,
                DISTANCE_FACTORS[java.util.Arrays.asList(DISTANCE_STRINGS).indexOf(distanceExposedMenu.getText().toString())],
                new PhotoDownloader.OnPhotoDownloadedByGeo(){
            @Override
            public void onFailResponse(String message, int code) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(),
                        "Network Failed. Please check the network",Toast.LENGTH_LONG);
                Snackbar.make(requireView(),
                        message,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", view -> {
                            swipeRefreshLayout.setRefreshing(true);
                            refreshPhotos();
                        })

                        .show();
            }

            @Override
            public void onFailRequest(Call<ResponseBody> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(),
                        "Network Failed. Please check the network",Toast.LENGTH_LONG);
                Snackbar.make(getView(),
                        "Network Failed. Please check the network",
                        Snackbar.LENGTH_INDEFINITE)

                        .setAction("Retry", view -> {
                            swipeRefreshLayout.setRefreshing(true);
                            refreshPhotos();
                        })
                        .setAnchorView(requireView().findViewById(R.id.nav_view))
                        .show();
            }

            @Override
            public void onSuccessResponse(PhotoDownloader.PhotoDownloadResult result) {
                List<CloudPhoto> photoList = result.getImageArray();
                //filter goes here
                String selectedWeather = "all";
                boolean sortByUpvote = false;
                if(selectedWeather != "all") {
                    //photoList = photoList.stream().filter(photo -> photo.weather == selectedWeather).collect(Collectors.toList());
                }
                if(sortByUpvote) {
                    //photoList = photoList.stream().sorted(photo -> photo.getnLike() -)
                }

                adapter.addPhotos(photoList);
                Log.w(this.getClass().getName(), "" + adapter.getItemCount());
                adapter.setImageGridOnClickCallBack(pos -> {

                    Bundle args = new Bundle();
                    args.putInt("pos",pos);
                    cloudPhotoDetailViewModel.setDataset(adapter.getPhotoList());
                    Log.w(TAG,cloudPhotoDetailViewModel.getDataset().toString());
                    Navigation.findNavController(requireActivity(),R.id.nav_host_fragment)
                            .navigate(R.id.action_navigation_home_to_navigation_cloud_photo_detail,args);
                });

                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPhotos();
        if(distanceExposedMenu != null) {
            distanceExposedMenu.clearFocus();
            distanceExposedMenu.dismissDropDown();
            menuAdapter.getFilter().filter(null);
        }

    }
}