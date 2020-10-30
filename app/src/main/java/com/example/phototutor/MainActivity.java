package com.example.phototutor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import com.example.phototutor.ui.login.ui.login.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

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
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        configNavigationItemSelectedListener(navView);
        configTopAppBarItemSelectedListener(findViewById(R.id.topAppBar));

    }

    private void configTopAppBarItemSelectedListener(MaterialToolbar topAppBar ){
        topAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.navigation_logout:
                    LoginActivity.userLogout();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    break;
                default:return false;
            }

            return true;
        });
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
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        sharedPreferences = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        if(sharedPreferences.contains(Username)) {
            finishAffinity();
            finish();
        }
    }


}