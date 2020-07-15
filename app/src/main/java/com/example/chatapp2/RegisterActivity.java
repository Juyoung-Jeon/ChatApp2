package com.example.chatapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp2.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText email;
    private EditText name;
    private EditText password;
    private Button register;
    private String splash_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 스테이터스 바 색깔 받아옴 from 로그인 액티비티 코드
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance(); // 원격으로 배경 받아오기 위함
        splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color)); // 원격으로 받아오기 위함
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        email = (EditText)findViewById(R.id.registerActivity_etEmail);
        name = (EditText)findViewById(R.id.registerActivity_etName);
        password = (EditText)findViewById(R.id.registerActivity_etPassword);
        register = (Button) findViewById(R.id.registerActivity_btnRegister);
        register.setBackgroundColor(Color.parseColor(splash_background));

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (email.getText().toString() == null || name.getText().toString() == null || password.getText().toString() == null){
                    return; // null 이면 진행 못하도록
                }

                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                UserModel userModel = new UserModel();
                                userModel.userName = name.getText().toString();

                                String uid = task.getResult().getUser().getUid();
                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel); // uid 는 주민번호 같은 역할로 암호화된 것
                                                                                                                             // child users 는 이 하위 데이터로 만들겠다.
                                
                            }
                        });
            }
        });

    }
}