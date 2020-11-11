package com.example.phototutor.ui.login.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.phototutor.EditProfileActivity;
import com.example.phototutor.MainActivity;
import com.example.phototutor.R;
import com.example.phototutor.RegisterActivity;
import com.example.phototutor.helpers.ProfileEditor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static SharedPreferences sharedPreferences;
    private LoginViewModel loginViewModel;
    public static final String filename = "login";
    public static final String Username = "username";
    public static final String Password = "password";
    public static final String AccessToken = "accessToken";
    public static final String RefreshToken = "refreshToken";
    public static final String Nickname = "nickname";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final Button registerButton = findViewById(R.id.register);
//        final Button openButton = findViewById(R.id.open);
//
//        openButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                openDialog();
//            }
//        });
        sharedPreferences = getSharedPreferences(filename, Context.MODE_PRIVATE);
//        if(sharedPreferences.contains(Username)) {
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            startActivity(intent);
//        }

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {

                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                OkHttpClient client = new OkHttpClient();
                String url = "http://whiteboard.house:8000/user/login/";
                Log.d("OKHTTP3", "POST Function called");
                MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                JSONObject actualData = new JSONObject();
                try {
                    actualData.put("username", usernameEditText.getText().toString());
                    actualData.put("password", passwordEditText.getText().toString());
                } catch (JSONException e) {
                    Log.d("OKHTTP3", "JSON Exception");
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(JSON, actualData.toString());
                Log.d("OKHTTP3", "Request body created");
                Request newReq = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                client.newCall(newReq).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("OKHTTP3", "Exception while doing request.");
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d("OKHTTP3", "Request Done, got the response.");
//                       Log.d("OKHTTP3", response.body().string());
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            if(json.has("error")) {
                                openDialog();
//                                showLoginFailed(json.getString("error"));
                            } else {
                                JSONObject user = json.getJSONObject("user");
                                String accessToken = json.getString("access");
                                String refreshToken = json.getString("refresh");
                                String username = user.getString("Username");
                                String nickname = user.getString("Nickname");

                                if (loginResult == null) {
                                    return;
                                }

                                if (loginResult.getSuccess() != null) {
                                    saveToSharedPreferences(username, passwordEditText.getText().toString(), accessToken, refreshToken, nickname);
                                    updateUiWithUser(nickname);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                setResult(Activity.RESULT_OK);
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

    private void updateUiWithUser(String nickname) {
        // initiate successful logged in experience
        String welcome = getString(R.string.welcome) + " " + nickname;
        Looper.prepare();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(String errorMessage) {
        Looper.prepare();
        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    private static void saveToSharedPreferences(String username, String password, String accessToken, String refreshToken, String nickname) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Username, username);
        editor.putString(Password, password);
        editor.putString(AccessToken, accessToken);
        editor.putString(RefreshToken, refreshToken);
        editor.putString(Nickname, nickname);
        editor.commit();
    }
    public void openDialog() {
        LoginFailedDialog loginFailedDialog = new LoginFailedDialog();
        loginFailedDialog.show(getSupportFragmentManager(), "example dialog");
    }

    public static void userLogout() {
        saveToSharedPreferences(null, null, null, null, null);
    }
}