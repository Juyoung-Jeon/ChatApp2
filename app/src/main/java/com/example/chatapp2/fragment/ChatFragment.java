package com.example.chatapp2.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp2.Chat.MessageActivity;
import com.example.chatapp2.R;
import com.example.chatapp2.model.ChatModel;
import com.example.chatapp2.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// 채팅방 리스트 프래그먼트

public class ChatFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat,container,false); // fragment chat 레이아웃 불러옴

        RecyclerView recyclerView = view.findViewById(R.id.chatFragment_recyclerView); // 리사이클러 뷰 찾고 정해줌
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext())); // 리스트로 보여주기 위함

        return view;
    }

    // 리사이클러뷰 어댑터 정의
    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        // 채팅 목록 가져오기
        private List<ChatModel> chatModels = new ArrayList<>();
        private String uid;
        private ArrayList<String> destinationUsers = new ArrayList<>();

        // Constructor
        public ChatRecyclerViewAdapter() {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // 채팅 정보 가져오기 from chatRoom DB
            FirebaseDatabase.getInstance().getReference().child("chatRooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    chatModels.clear(); // 데이터 쌓기 전에 클리어
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        chatModels.add(item.getValue(ChatModel.class));
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        // 받아온 Data 보여주는 부분
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false); // 만들었던 아이템 사용

            return new CustomViewHolder(view); // 반복 사용 가능하도록 커스텀 뷰 홀더 사용
        }

        // 뷰에 바인딩
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            final CustomViewHolder customViewHolder = (CustomViewHolder)holder;
            String destinationUid = null; // 대화할 상대를 null 로 일단 받아옴

            // 받아오기 위해 채팅방에 있는 유저 일일이 체크
            for(String user: chatModels.get(position).users.keySet()){
                // 내가 아닌 유저를 받아옴
                if(!user.equals(uid)){
                    destinationUid = user;
                    destinationUsers.add(destinationUid); // 대화할 사람이 담기는 곳
                }
            }
            // destination 이 누군지 정의, 한 번만 하면돼서 single 리스너
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class); // 유저 모델에는 이미지와 주소가 있음
                    Glide.with(customViewHolder.itemView.getContext())
                            .load(userModel.profileImageUrl)
                            .apply(new RequestOptions().circleCrop())
                            .into(customViewHolder.imageView);

                    customViewHolder.tvTitle.setText(userModel.userName); // 채팅방 이름을 상대방 이름으로 함
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // 메시지를 내림차순으로 정렬 후 마지막 메시지를 키값으로 가져오는 코드
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
            commentMap.putAll(chatModels.get(position).comments); // 채팅에 대한 내용 넣어줌
            String lastMessageKey = (String) commentMap.keySet().toArray()[0]; // 첫번째 값(마지막 메시지)만 받아옴
            customViewHolder.tvLastMessage.setText(chatModels.get(position).comments.get(lastMessageKey).message); // 마지막 메시지를 lastMessageKey 로 받아옴

            // 채팅방 리스트에서 채팅방 클릭 시 채팅 화면으로 전환
            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getView().getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid", destinationUsers.get(position)); // 누구랑 대화할지 정해주면 방이 열림

                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getView().getContext(), R.anim.fromright,R.anim.toleft); // 화면전환 시 애니메이션 효과 추가
                    startActivity(intent, activityOptions.toBundle());
                }
            });

        }

        @Override
        public int getItemCount() {
            return chatModels.size(); // 카운터, 사이즈 넣어서 보이게 함
        }

        // 커스텀뷰홀더 이너클래스 정의
        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView tvTitle;
            public TextView tvLastMessage;
            public CustomViewHolder(View view) {
                super(view);

                imageView = view.findViewById(R.id.chatItem_imageView);
                tvTitle = view.findViewById(R.id.chatItem_tvTitle);
                tvLastMessage = view.findViewById(R.id.chatItem_tvLastMessage); // 채팅방 목록에 마지막 메시지 띄움
            }
        }
    }
}
