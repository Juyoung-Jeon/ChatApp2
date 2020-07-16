package com.example.chatapp2.Chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.chatapp2.R;
import com.example.chatapp2.model.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        destinationUid = getIntent().getStringExtra("destinationUid"); // PeopleFragment 로 부터 uid 받아옴
        button = (Button)findViewById(R.id.messageActivity_button);
        editText = findViewById(R.id.messageActivity_editText);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel chatModel = new ChatModel();
                chatModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 자신의 uid 받음
                chatModel.destinationUid = destinationUid; // 상대방의 uid 받아옴

                FirebaseDatabase.getInstance().getReference().child("chatRooms").push().setValue(chatModel); // push 는 primary key 같은 역할, 채팅방의 이름.
            }
        });

    }
}