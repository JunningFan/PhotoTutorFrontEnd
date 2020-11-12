package com.example.phototutor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.phototutor.ui.login.ui.login.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText username = findViewById(R.id.editTextUsername);
        final EditText nickname = findViewById(R.id.editTextNickname);
        final EditText password = findViewById(R.id.editTextPassword);
        final EditText confirmPassword = findViewById(R.id.editTextConfirmPassword);
        final Button confirmButton = findViewById(R.id.confirmButton);

        final TextInputLayout usernameLayout = (TextInputLayout) findViewById(R.id.UsernameLayout);
        final TextInputLayout nicknameLayout = (TextInputLayout) findViewById(R.id.editTextNicknameLayout);
        final TextInputLayout passwordLayout = (TextInputLayout) findViewById(R.id.editTextPasswordLayout);
        final TextInputLayout confirmPasswordLayout = (TextInputLayout) findViewById(R.id.editTextConfirmPasswordLayout);

        passwordLayout.passwordVisibilityToggleRequested(true);
        confirmPasswordLayout.passwordVisibilityToggleRequested(true);

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
                String Username = username.getText().toString();
                String Nickname = nickname.getText().toString();
                String password1 = password.getText().toString();
                String password2 = confirmPassword.getText().toString();
                confirmButton.setEnabled(false);

                if(!Username.isEmpty()){
                    usernameLayout.setErrorEnabled(false);

                } else {
                    usernameLayout.setErrorEnabled(true);
                    usernameLayout.setError("Please enter a valid username!");
                }

                if(!Nickname.isEmpty()){
                    nicknameLayout.setErrorEnabled(false);

                } else {
                    nicknameLayout.setErrorEnabled(true);
                    nicknameLayout.setError("Please enter a valid nickname!");
                }

                if(isPasswordValid(password1)) {
                    passwordLayout.setErrorEnabled(false);
                    if(password1.equals(password2) != true) {
                        confirmPasswordLayout.setErrorEnabled(true);
                        confirmPasswordLayout.setError("Passwords do not matched!");
                    } else {
                        confirmPasswordLayout.setErrorEnabled(false);
                    }
                    if(!Username.isEmpty() && !Nickname.isEmpty() && password1.equals(password2)) {
                        confirmButton.setEnabled(true);
                        postUserDataToDatabase(Username, Nickname, password1);
                    }
                } else {
                    passwordLayout.setErrorEnabled(true);
                    passwordLayout.setError("Password must be more than 5 characters!");

                }
            }
        };
        username.addTextChangedListener(afterTextChangedListener);
        nickname.addTextChangedListener(afterTextChangedListener);
        password.addTextChangedListener(afterTextChangedListener);
        confirmPassword.addTextChangedListener(afterTextChangedListener);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setCancelable(false);
                builder.setTitle("Register Successful")
                        .setMessage("Congratulation! Register was successful. Log in and start your photography journey!")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        });
    }

    public void postUserDataToDatabase(String username, String nickname, String password) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://whiteboard.house:8000/user/";
        Log.d("OKHTTP3", "POST Function called");
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject actualData = new JSONObject();
        try {
            actualData.put("username", username);
            actualData.put("password", password);
            actualData.put("nickname", nickname);

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
                Log.d("OKHTTP3", response.body().string());
            }
        });
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}