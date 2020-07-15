package com.example.chatapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    private EditText id;
    private EditText password;

    private Button login;
    private Button register;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAuth firebaseAuth; // 파이어베이스 로그인 관리
    private FirebaseAuth.AuthStateListener authStateListener; // 로그인 처리 결과 알려주는 리스너

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance(); // 원격으로 배경 받아오기 위함
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut(); // 로그아웃 기능 추가

        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_color)); // 원격으로 받아오기 위함
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        id = (EditText) findViewById(R.id.loginActivity_etID);
        password = (EditText) findViewById(R.id.loginActivity_etPassword);

        login = (Button) findViewById(R.id.loginActivity_btnLogin);
        register = (Button) findViewById(R.id.loginActivity_btnRegister);
        login.setBackgroundColor(Color.parseColor(splash_background));
        register.setBackgroundColor(Color.parseColor(splash_background));

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEvent();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // 로그인 성공 시 화면전환 인터페이스 리스너
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // 유저 있을 때, 즉 로그인 됐을 때
                if (user != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // 자기는 닫으면서 Main 여는 것
                } else {
                    // 유저 없을 때, 즉 로그아웃 됐을 때
                }
            }
        };
    }
    // 로그인 이벤트 생성 - 로그인 여부만 알려주는 것(auth)
    void loginEvent () {
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    //로그인 실패시
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show(); // 실패원인 toast
                }
            }
        });
    }

    // 리스너가 로그인과 같이 붙어 작동하게 해줌.
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    // 중지 시 같이 멈추게
    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}