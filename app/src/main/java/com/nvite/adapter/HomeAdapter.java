package com.nvite.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nvite.R;
import com.nvite.activity.ChatActivity;
import com.nvite.activity.EventDetailActivity;
import com.nvite.activity.EventsGraphActivity;
import com.nvite.fragment.HomeFragment;
import com.nvite.model.PrivateEventListResponse;
import com.nvite.util.Util;

import java.util.ArrayList;

/**
 * Created by AIM on 9/11/2017.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {
    Context context;
    private ArrayList<PrivateEventListResponse> arrayList;
    private HomeFragment homeFragment;

    public HomeAdapter(Context context, ArrayList<PrivateEventListResponse> arrayList, HomeFragment homeFragment) {
        this.context = context;
        this.arrayList = arrayList;
        this.homeFragment = homeFragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_recycle_row, parent, false);
        return new HomeAdapter.MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Glide.with(context).load(arrayList.get(position).getEvent_image()).placeholder(R.drawable.creater).override(300, 300).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.event_image);
        holder.event_name.setText(arrayList.get(position).getName());
        holder.date.setText(Util.convertTimeStampDateName(Long.parseLong(arrayList.get(position).getEvent_date())));
        holder.city.setText(arrayList.get(position).getAddress());
        holder.hosted_by.setText(arrayList.get(position).getUser_name());
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra("event_type", arrayList.get(position).getEvent_type());
                intent.putExtra("event_detail", arrayList.get(position));
                homeFragment.startActivityForResult(intent, 202);
            }
        });

        holder.attending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EventsGraphActivity.class);
                intent.putExtra("event_detail", arrayList.get(position));
                intent.putExtra("from_save", false);
                context.startActivity(intent);
            }
        });

        holder.chat_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra("from_group", false);
                i.putExtra("event_id", arrayList.get(position).getId());
                i.putExtra("chat_name",  arrayList.get(position).getName());
                i.putExtra("chat_image",  arrayList.get(position).getEvent_image());
                context.startActivity(i);
            }
        });

        if(arrayList.get(position).getAttend().equals("0")){
            holder.attn.setBackground(context.getResources().getDrawable(R.drawable.login_btn_bg_black));
            holder.attn.setText("Invited");
        }else{
            holder.attn.setBackground(context.getResources().getDrawable(R.drawable.login_btn_bg_orange));
            holder.attn.setText("Attending");
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView event_image;
        public LinearLayout chat_icon, attending;
        public RelativeLayout parent;
        public TextView event_name, date, city, hosted_by, attn;

        public MyViewHolder(View view) {
            super(view);
            event_image = (ImageView) view.findViewById(R.id.event_image);
            parent = (RelativeLayout) view.findViewById(R.id.parent);
            attending = (LinearLayout) view.findViewById(R.id.attending);
            chat_icon = (LinearLayout) view.findViewById(R.id.chat_icon);
            event_name = (TextView) view.findViewById(R.id.event_name);
            city = (TextView) view.findViewById(R.id.city);
            hosted_by = (TextView) view.findViewById(R.id.hosted_by);
            attn = (TextView) view.findViewById(R.id.attn);
            date = (TextView) view.findViewById(R.id.date);
        }
    }
}
