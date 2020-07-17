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
import com.example.chatapp2.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {
    @Nullable
    @Override

    // onCreateView : onCreate 로 생성 이후 화면 구성에 쓰임, layout inflater 는 layout xml 가져오는 것.
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people,container,false); // (뷰 만들고 싶은 레이아웃 파일 id, 생성될 view 의 parent, true 시 자식 view 로 자동추가)
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.peopleFragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext())); // 리사이클러뷰에 레이아웃 매지너 객체 지정
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter()); // 리사이클러뷰에 어댑터 객체 지정

        return view;
    }

    // 리스트를 표현하기 위한 요소의 공통점 : 어댑터를 사용함. 데이터 리스트가 어댑터를 거쳐 리사이클러뷰로 표현되는 것. 어댑터가 기존 리스트뷰에서의 아이템뷰를 만들어주는 것.
    // 어댑터는 반드시 개발자가 직접 구현해야 함
    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;  // 목록에 쌓일 array list
        public PeopleFragmentRecyclerViewAdapter() {
            userModels = new ArrayList<>(); // 파이어베이스 db 받아오기 위함
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 내 uid 면 리스트에 안 쌓이게 -> 중복 제거 위함
            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() { // 파이어베이스 db 중 users 를 받아옴.
                // valueEventListener 는 전체 데이터 목록을 단일 DataSnapshot 으로 반환. 이를 루프 처리하여 개별 하위 항목에 엑세스 가능.

                // 서버에서 넘어온 데이터
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userModels.clear(); // 친구 목록 쌓일 때 중복되는 것 제거


                    // 서버에서 넘어온 친구 목록이 담기는 곳
                    for(DataSnapshot snapshot :dataSnapshot.getChildren()){ // 하위 데이터 받아오기 위한 루프 처리

                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if(userModel.uid.equals(myUid)){
                            continue; // 건너뜀
                        }
                        userModels.add(snapshot.getValue(UserModel.class)); // 유저 모델에 데이터 쌓임
                    }
                    notifyDataSetChanged(); // 데이터 쌓이고 새로고침 해서 불러올 수 있게 해주는 것!(필수)
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        // 뷰홀더는 화면에 표시될 아이템 뷰를 저장하는 객체
        // onCreate 뷰홀더 객체 생성
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend,parent,false);
            return new CustomViewHolder(view);
        }

        // onBind 는 position 에 해당하는 데이터를 뷰홀더 아이템 뷰에 표시
        // 이미지 넣어줄 곳
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            Glide.with // 이미지 자를 때 쓰임, Glider 라이브러리에 추가
                    (holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop()) // 어떻게 이미지 줄 건지 정하는 것, 원형으로 넣기 위함
                    .into(((CustomViewHolder)holder).imageView);

            ((CustomViewHolder)holder).textView.setText(userModels.get(position).userName); // 이미지 뷰는 위에서 넣었고, 텍스트뷰도 마찬가지로 넣어줌.

            // 유저 리스트 클릭 시 이벤트 작동 코드 -> 채팅창으로 넘어가기 위함
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getView().getContext(), MessageActivity.class); // 메시지 액티비티로 넘어가기 위함
                    intent.putExtra("destinationUid", userModels.get(position).uid); // 회원가입 때 정해진 uid 를 불러와서 채팅방 형성
                    // 애니메이션 효과 삽입
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());
                }
            });

        }

        // 전체 아이템 개수 리턴턴
       @Override
        public int getItemCount() {
            return userModels.size();
        }

        // 아이템뷰에 있던 것 받아줄 커스텀뷰홀더, 친구목록의 사진과 이름 받아오기 위함
        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.friendItem_imageView);
                textView = (TextView)view.findViewById(R.id.friendItem_textView);
            }
        }
    }
}
