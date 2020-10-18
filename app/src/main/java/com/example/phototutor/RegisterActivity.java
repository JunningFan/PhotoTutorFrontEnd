package com.example.phototutor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.phototutor.ui.login.ui.login.LoginActivity;

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

                if(!isUsernameValid(Username)){
                    username.setError("Please enter a valid username!");
                }

                if(Nickname.isEmpty()) {
                    nickname.setError("Please enter a valid nickname!");
                }

                if(isPasswordValid(password1)) {
                    if(password1.equals(password2) != true) {
                        confirmPassword.setError("Passwords not matched!");
                    }
                    if(!Username.isEmpty() && !Nickname.isEmpty()) {
                        confirmButton.setEnabled(true);
                        postUserDataToDatabase(Username, Nickname, password1);
                    }
                } else {
                    password.setError("Password must be more than 5 characters!");
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
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public void postUserDataToDatabase(String username, String nickname, String password) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://whiteboard.house:8080/users/";
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
//        setResult(Activity.RESULT_OK);
    }

    // A placeholder username validation check
    private boolean isUsernameValid(String username) {
        if (username.isEmpty()) {
            return false;
        }
//        if (username.contains("@")) {
//            return true;
//        }else {
//            return false;
//        }
        return true;
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}