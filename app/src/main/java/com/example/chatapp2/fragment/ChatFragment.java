package com.example.chatapp2.fragment;

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

public class ChatFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat,container,false);

        RecyclerView recyclerView = view.findViewById(R.id.chatFragment_recyclerView);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return view;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        // 채팅 목록 가져오기
        private List<ChatModel> chatModels = new ArrayList<>();
        private String uid;
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false);

            return new CustomViewHolder(view); // 반복 사용 가능하도록 커스텀 뷰 홀더 사용
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            final CustomViewHolder customViewHolder = (CustomViewHolder)holder;
            String destinationUid = null;

            // 채팅방에 있는 유저 일일이 체크
            for(String user: chatModels.get(position).users.keySet()){
                if(!user.equals(uid)){
                    destinationUid = user;
                }
            }
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    Glide.with(customViewHolder.itemView.getContext())
                            .load(userModel.profileImageUrl)
                            .apply(new RequestOptions().circleCrop())
                            .into(customViewHolder.imageView);

                    customViewHolder.tvTitle.setText(userModel.userName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // 메시지를 내림차순으로 정렬 후 마지막 메시지를 키값으로 가져오는 코드
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
            commentMap.putAll(chatModels.get(position).comments);
            String lastMessageKey = (String) commentMap.keySet().toArray()[0];
            customViewHolder.tvLastMessage.setText(chatModels.get(position).comments.get(lastMessageKey).message);

        }

        @Override
        public int getItemCount() {
            return chatModels.size(); // 카운터
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView tvTitle;
            public TextView tvLastMessage;
            public CustomViewHolder(View view) {
                super(view);

                imageView = view.findViewById(R.id.chatItem_imageView);
                tvTitle = view.findViewById(R.id.chatItem_tvTitle);
                tvLastMessage = view.findViewById(R.id.chatItem_tvLastMessage);
            }
        }
    }
}
