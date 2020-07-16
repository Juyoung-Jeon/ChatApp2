package com.example.chatapp2.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.chatapp2.R;
import com.example.chatapp2.model.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 자신의 uid 받음 // 채팅을 요구하는 아이디, 단말기에 로그인 된 아이디
        destinationUid = getIntent().getStringExtra("destinationUid"); // PeopleFragment 로 부터 uid 받아옴 // 채팅을 당하는 아이디
        button = (Button)findViewById(R.id.messageActivity_button);
        editText = findViewById(R.id.messageActivity_editText);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel chatModel = new ChatModel();
                // 대화한 사람들 넣어줌
                chatModel.users.put(uid, true);
                chatModel.users.put(destinationUid, true);

                if(chatRoomUid == null){ // null 이면 바로 생성
                    FirebaseDatabase.getInstance().getReference().child("chatRooms").push().setValue(chatModel); // push 는 primary key 같은 역할, 채팅방의 이름.
                } else  {
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = editText.getText().toString();
                    FirebaseDatabase.getInstance().getReference().child("chatRooms").child(chatRoomUid).child("comments").push().setValue(comment);
                }
            }
        });
        checkChatRoom();

    }

    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatRooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    ChatModel chatModel = item.getValue(ChatModel.class); // chatRoom 하위의 user 불러와 검토
                        if(chatModel.users.containsKey(destinationUid)){
                            chatRoomUid = item.getKey(); // 방 아이디 chatRoom 바로 하위
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }); // 채팅방 중복 체크 방지, users 의 uid 값 활용해서
    }
}