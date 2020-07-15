package com.example.chatapp2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatapp2.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int PICK_FROM_ALBUM = 10;
    private EditText email;
    private EditText name;
    private EditText password;
    private Button register;
    private String splash_background;
    private ImageView profile;
    private Uri imageUri; // 업로드 할 때 담는 역할

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 스테이터스 바 색깔 받아옴 from 로그인 액티비티 코드
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance(); // 원격으로 배경 받아오기 위함
        splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color)); // 원격으로 받아오기 위함
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        profile = (ImageView)findViewById(R.id.registerActivity_ivProfile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM); // intent 구별 위해서 request code 필요
            }
        });

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

                                final String uid = task.getResult().getUser().getUid();
                                FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        Task<Uri> imageUrl = task.getResult().getStorage().getDownloadUrl();
                                        while(!imageUrl.isComplete());

                                        UserModel userModel = new UserModel();
                                        userModel.userName = name.getText().toString();
                                        userModel.profileImageUrl = imageUrl.getResult().toString();

                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel); // uid 는 주민번호 같은 역할로 암호화된 것
                                        // child users 는 이 하위 데이터로 만들겠다.

                                    } // OnCompleteListener 정상적으로 완료됐는지 파악 위함
                                }); // 파일 이름이 uid 고유값이기 때문에 중복 x


                                
                            }
                        });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) { // result ok 는 제대로 처리 됐는지 확인.
            profile.setImageURI(data.getData()); // 회원가입 시 가운데 이미지 뷰 변경
            imageUri = data.getData(); // 이미지 경로 원본 저장
        }
    }
}