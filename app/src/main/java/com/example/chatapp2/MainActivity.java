package com.example.chatapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chatapp2.fragment.PeopleFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_frameLayout, new PeopleFragment()).commit();
    }
}