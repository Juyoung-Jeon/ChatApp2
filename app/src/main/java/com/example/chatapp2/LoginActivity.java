package com.example.chatapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {
    private Button login;
    private Button register;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance(); // 원격으로 배경 받아오기 위함
        String splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color)); // 원격으로 받아오기 위함
        getWindow().setStatusBarColor(Color.parseColor(splash_background));



        login = (Button)findViewById(R.id.loginActivity_btnLogin);
        register = (Button)findViewById(R.id.loginActivity_btnRegister);
        login.setBackgroundColor(Color.parseColor(splash_background));
        register.setBackgroundColor(Color.parseColor(splash_background));

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }
}