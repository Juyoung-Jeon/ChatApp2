package com.example.chatapp2.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp2.R;
import com.example.chatapp2.model.ChatModel;
import com.example.chatapp2.model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 자신의 uid 받음 // 채팅을 요구하는 아이디, 단말기에 로그인 된 아이디
        destinationUid = getIntent().getStringExtra("destinationUid"); // PeopleFragment 로 부터 uid 받아옴 // 채팅을 당하는 아이디
        button = (Button)findViewById(R.id.messageActivity_button);
        editText = findViewById(R.id.messageActivity_editText);

        recyclerView = (RecyclerView)findViewById(R.id.messageActivity_recyclerView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel chatModel = new ChatModel();
                // 대화한 사람들 넣어줌
                chatModel.users.put(uid, true);
                chatModel.users.put(destinationUid, true);

                if(chatRoomUid == null){ // null 이면 바로 생성
                    button.setEnabled(false); // 값 입력 전까지 잠시 불능으로 만들어 버그 방지
                    FirebaseDatabase.getInstance().getReference().child("chatRooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom(); // 데이터 입력 완료 후에 방을 체크하도록 하여 중복을 방지하기 위한 코드
                        }
                    }); // push 는 primary key 같은 역할, 채팅방의 이름.

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
                            button.setEnabled(true); // 방 id 받아온 다음에 비활성화시킨 버튼 다시 활성화
                            recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                            recyclerView.setAdapter(new RecyclerViewAdapter());
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }); // 채팅방 중복 체크 방지, users 의 uid 값 활용해서
    }
    // 리사이클러뷰 어댑터 정의
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<ChatModel.Comment> comments;
        UserModel userModel;
        public RecyclerViewAdapter() {
            comments = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // 유저 모델부터 불러온 후 메시지 리스트를 불러오는 게 자연스러운 순서
                    userModel = dataSnapshot.getValue(UserModel.class);
                    getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        void getMessageList(){
            FirebaseDatabase.getInstance().getReference().child("chatRooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear(); // 데이터 추가될 때마다 서버에 대화 내옹 다보내주기 때문에 데이터가 계속 쌓임

                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        comments.add(item.getValue(ChatModel.Comment.class));
                    }
                    notifyDataSetChanged(); // 데이터 갱신 역할
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder)holder);

            if(comments.get(position).uid.equals(uid)){ // 전자는 comments 내의 uid, 후자는 내 uid
                messageViewHolder.tvMessage.setText(comments.get(position).message);
                messageViewHolder.tvMessage.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.llDestination.setVisibility(View.INVISIBLE); // 내 메시지뷰는 상대방에게 감추기 위함

            }else {

                Glide.with(holder.itemView.getContext())
                        .load(userModel.profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.ivProfile);
                messageViewHolder.tvName.setText(userModel.userName);
                messageViewHolder.llDestination.setVisibility(View.VISIBLE);
                messageViewHolder.tvMessage.setBackgroundResource(R.drawable.leftbubble); // 말풍선
                messageViewHolder.tvMessage.setText(comments.get(position).message);

            }
            messageViewHolder.tvMessage.setTextSize(25);

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView tvMessage;
            public TextView tvName;
            public ImageView ivProfile;
            public LinearLayout llDestination;

            public MessageViewHolder(View view) {
                super(view);
                tvMessage = view.findViewById(R.id.messageItem_tvMessage);
                tvName = (TextView)view.findViewById(R.id.messageItem_tvName);
                ivProfile = (ImageView)view.findViewById(R.id.messageItem_ivProfile);
                llDestination = (LinearLayout)view.findViewById(R.id.messageItem_llDestination);


            }
        }
    }
}