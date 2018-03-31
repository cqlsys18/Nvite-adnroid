package com.nvite.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.nvite.fragment.MessageFragment;
import com.nvite.model.MessageEventListResponse;
import com.nvite.model.MessageGroupListResponse;
import com.nvite.model.PrivateEventListResponse;
import com.nvite.parser.AllInviteParameters;
import com.nvite.util.Util;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by AIM on 9/11/2017.
 */

public class MessageGroupListAdapter extends RecyclerView.Adapter<MessageGroupListAdapter.MyViewHolder> {
    Context context;
    private ArrayList<MessageGroupListResponse> arrayList;
    private MessageFragment fragment;

    public MessageGroupListAdapter(Context context, ArrayList<MessageGroupListResponse> arrayList, MessageFragment fragment) {
        this.context = context;
        this.arrayList = arrayList;
        this.fragment = fragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_row, parent, false);
        return new MessageGroupListAdapter.MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Glide.with(context).load(arrayList.get(position).getImage()).placeholder(R.drawable.creater).override(200, 200).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.image);
        holder.name.setText(arrayList.get(position).getName());
        if (Integer.parseInt(arrayList.get(position).getUnseen_msg()) > 0) {
            holder.unseen_message.setVisibility(View.VISIBLE);
            holder.unseen_message.setText("(" + arrayList.get(position).getUnseen_msg() + ")");
        } else {
            holder.unseen_message.setVisibility(View.GONE);
        }
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("group_id", arrayList.get(position).getId());
                intent.putExtra("from_group", true);
                intent.putExtra("chat_name",arrayList.get(position).getName());
                intent.putExtra("chat_image",arrayList.get(position).getImage());
                fragment.startActivityForResult(intent,202);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView image;
        public RelativeLayout parent;
        public TextView name, unseen_message;

        public MyViewHolder(View view) {
            super(view);
            image = (CircleImageView) view.findViewById(R.id.image);
            parent = (RelativeLayout) view.findViewById(R.id.parent);
            name = (TextView) view.findViewById(R.id.name);
            unseen_message = (TextView) view.findViewById(R.id.unseen_message);
        }
    }
}
