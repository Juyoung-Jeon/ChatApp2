package com.example.chatapp2.model;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    public Map<String, Boolean> users = new HashMap<>(); // 채팅방의 유저들 - 나와 상대방의 uid(destination uid) 모두 받음
    public Map<String, Comment> comments = new HashMap<>(); // 채팅방 대화내용

    public static class Comment {
        public String uid;
        public String message;
    }

}
