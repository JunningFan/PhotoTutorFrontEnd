package com.example.phototutor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

public class MainActivity extends MyAppCompatActivity {

    private static SharedPreferences sharedPreferences;
    public static final String fileName = "login";
    public static final String Username = "username";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_user_profile)
                .build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        configNavigationItemSelectedListener(navView);


    }

    private void configNavigationItemSelectedListener(BottomNavigationView navView ){
        navView.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.dialog_selection){
                PopupMenu popup = new PopupMenu(this,findViewById(R.id.dialog_selection));
                popup.getMenuInflater().inflate(R.menu.camera_local_album_selection_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(menuItem -> {
                    Intent intent = null;
                    switch (menuItem.getItemId()){
                        case R.id.navigation_camera:
                            intent = new Intent(this, CameraActivity.class);
                            break;
                        case R.id.navigation_local_album:
                            intent = new Intent(this, LocalAlbumActivity.class);
                            break;


                    }
                    startActivity(intent);
                    return true;
                });
                popup.setOnDismissListener(popupMenu -> {
                    navView.setSelectedItemId(R.id.navigation_home);

                });
                popup.show();
            }
            else if (item.getItemId() == R.id.navigation_user_profile){
                Bundle bundle = new Bundle();
                bundle.putInt("userId",getPrimaryUserId()); //TODO add primary user id
                bundle.putBoolean("primaryUser",true);
                Navigation.findNavController(this,R.id.nav_host_fragment)
                        .navigate(R.id.navigation_user_profile,bundle);
            }
            else if (item.getItemId() == R.id.navigation_home){
                Navigation.findNavController(this,R.id.nav_host_fragment)
                        .navigate(R.id.navigation_home);
            }
            return true;
        });
    }

//    @Override
//    public void onBackPressed() {
//        sharedPreferences = getSharedPreferences(fileName, Context.MODE_PRIVATE);
//        if(sharedPreferences.contains(Username)) {
//            finishAffinity();
//            finish();
//        }
//
//    }




    //for fused location listenr

}