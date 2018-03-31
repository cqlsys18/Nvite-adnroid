package com.nvite.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.nvite.R;
import com.nvite.adapter.MyAdapter;

public class AddFriendslistActivity extends BaseActivity {
    ImageView rightarrow;
    String[] titles = {"Linda Natasha", "Linda Natasha", "Linda Natasha", "Linda Natasha","Linda Natasha", "Linda Natasha", "Linda Natasha", "Linda Natasha", "Linda Natasha", "Linda Natasha"};
    String[] description = {"Lorem ipsum doler sit amet", "Lorem ipsum doler sit amet", "Lorem ipsum doler sit amet", "Lorem ipsum doler sit amet","Lorem ipsum doler sit amet", "Lorem ipsum doler sit amet", "Lorem ipsum doler sit amet", "Lorem ipsum doler sit amet", "Lorem ipsum doler sit amet", "Lorem ipsum doler sit amet"};
    int[] images1 = {R.drawable.invited_btn, R.drawable.invitedgreen, R.drawable.invited_btn, R.drawable.invited_btn,R.drawable.invited_btn, R.drawable.invitedgreen, R.drawable.invited_btn, R.drawable.invited_btn, R.drawable.invited_btn, R.drawable.invited_btn};
    int[] images = {R.drawable.friendlist2, R.drawable.friendlist3, R.drawable.friendlist2, R.drawable.friendlist2,R.drawable.friendlist2, R.drawable.friendlist3, R.drawable.friendlist2, R.drawable.friendlist2, R.drawable.friendlist2, R.drawable.friendlist2};
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friendslist);
        Toast.makeText(AddFriendslistActivity.this, "yes", Toast.LENGTH_LONG).show();
        lv = (ListView) findViewById(R.id.listview);
        MyAdapter adapter = new MyAdapter(AddFriendslistActivity.this, titles, description, images, images1);
        lv.setAdapter(adapter);
        rightarrow= (ImageView) findViewById(R.id.rightarrow);


        rightarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ii = new Intent(AddFriendslistActivity.this,ContactlistActivity.class);
                startActivity(ii);
            }
        });

    }}
