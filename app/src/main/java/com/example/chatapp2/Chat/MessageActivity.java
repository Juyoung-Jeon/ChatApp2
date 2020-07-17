package com.example.chatapp2.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Gravity;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// 리사이클러뷰 클릭 시 채팅창 뜨도록

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid; // 채팅방 uid

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

        // 메시지 보내는 부분
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel chatModel = new ChatModel(); // ChatModel 생성 후 불러옴
                // 대화한 사람들 넣어줌
                chatModel.users.put(uid, true); // uid 에는 내 것을
                chatModel.users.put(destinationUid, true); // destination 에는 상대방의 것을

                if(chatRoomUid == null){ // null 이면 바로 생성
                    button.setEnabled(false); // 채팅 요구 후 값이 입력되기 전까지 잠시 불능으로 만들어 같은 요청이 여러번 들어오는 것 방지
                    FirebaseDatabase.getInstance().getReference().child("chatRooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom(); // 데이터 입력 완료 후에 방을 체크하도록 하여 중복을 방지하기 위한 코드, 리스너 사용 // 여기서 파이어베이스 db 에 이름 chatRooms 로 저장 by push
                        }
                    }); // push 는 primary key 같은 역할, 채팅방의 이름 넣어줌

                } else  {
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = editText.getText().toString(); // 메시지 editText 에서 받아옴
                        FirebaseDatabase.getInstance().getReference().child("chatRooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                           // 메시지 보낸 후 입력창 초기화 by CompleteListener
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                editText.setText("");
                            }
                        });
                    }
                }
            });
            checkChatRoom();

        }

        // 채팅방 중복형성 방지 코드
        // orderByChild 사용
        void checkChatRoom(){
            FirebaseDatabase.getInstance().getReference().child("chatRooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        ChatModel chatModel = item.getValue(ChatModel.class); // chatRoom 하위에 users 불러와 id 있는지 검토
                            if(chatModel.users.containsKey(destinationUid)){ // 상대방 uid 가 있으면
                                chatRoomUid = item.getKey(); // 방 아이디 chatRoom 바로 하위
                                button.setEnabled(true); // 방 id 받아온 다음에 비활성화시킨 버튼 다시 활성화

                                // 리사이클러뷰 불러옴옴
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

        //Constructor
        public RecyclerViewAdapter() {
            comments = new ArrayList<>(); // 코멘츠 선언

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
            // chatRooms 하위에 comments 로 메시지 저장
            FirebaseDatabase.getInstance().getReference().child("chatRooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear(); // 데이터 추가될 때마다 서버에 대화 내옹 다보내주기 때문에 데이터가 계속 쌓임

                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        comments.add(item.getValue(ChatModel.Comment.class));
                    }
                    // 메시지 넣어주고 나서 갱신위함
                    notifyDataSetChanged(); // 데이터 갱신 역할
                    recyclerView.scrollToPosition(comments.size() - 1); // 갱신 후 스크롤이 맨 마지막 챗으로 이동
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 뷰 입력
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);
            return new MessageViewHolder(view); // 뷰홀더는 뷰 재사용할 때 쓰이는 클래스
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder)holder);

            // 내가 보낸 메시지
            if(comments.get(position).uid.equals(uid)){ // 전자는 comments 내의 uid, 후자는 내 uid
                messageViewHolder.tvMessage.setText(comments.get(position).message); // 메시지 받아오는 코드
                messageViewHolder.tvMessage.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.llDestination.setVisibility(View.INVISIBLE); // 내 메시지뷰는 상대방에게 감추기 위함
                messageViewHolder.llMain.setGravity(Gravity.RIGHT); // 내 메시지 우측 정렬

            // 상대방이 보낸 메시지
            }else {

                Glide.with(holder.itemView.getContext())
                        .load(userModel.profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.ivProfile);
                messageViewHolder.tvName.setText(userModel.userName);
                messageViewHolder.llDestination.setVisibility(View.VISIBLE);
                messageViewHolder.tvMessage.setBackgroundResource(R.drawable.leftbubble); // 말풍선
                messageViewHolder.tvMessage.setText(comments.get(position).message);
                messageViewHolder.llMain.setGravity(Gravity.LEFT); // 상대 메시지 좌측 정렬
            }
            messageViewHolder.tvMessage.setTextSize(25);

        }

        @Override
        public int getItemCount() {
            return comments.size();
        } // 코멘트를 추가, 몇 번 돌아가는지 파악 위함

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView tvMessage;
            public TextView tvName;
            public ImageView ivProfile;
            public LinearLayout llDestination;
            public LinearLayout llMain;

            public MessageViewHolder(View view) {
                super(view);
                tvMessage = view.findViewById(R.id.messageItem_tvMessage);
                tvName = (TextView)view.findViewById(R.id.messageItem_tvName);
                ivProfile = (ImageView)view.findViewById(R.id.messageItem_ivProfile);
                llDestination = (LinearLayout)view.findViewById(R.id.messageItem_llDestination);
                llMain = (LinearLayout)view.findViewById(R.id.messageItem_llMain); // 채팅창 정렬에 쓰임


            }
        }
    }

    // 뒤로가기 눌렀을 때도 애니메이션 작동하도록
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fromleft,R.anim.toright);
    }
}