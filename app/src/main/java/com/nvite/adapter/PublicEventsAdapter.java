package com.nvite.adapter;

import android.app.Dialog;
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
import com.nvite.activity.FriendListActivity;
import com.nvite.fragment.PublicEventsFragment;
import com.nvite.model.PrivateEventListResponse;
import com.nvite.parser.AllInviteParameters;

import java.util.ArrayList;

/**
 * Created by AIM on 9/11/2017.
 */

public class PublicEventsAdapter extends RecyclerView.Adapter<PublicEventsAdapter.MyViewHolder> {
    Context context;
    private ArrayList<PrivateEventListResponse> arrayList;
    PublicEventsFragment eventsFragment;

    public PublicEventsAdapter(Context context, ArrayList<PrivateEventListResponse> arrayList, PublicEventsFragment eventsFragment) {
        this.context = context;
        this.arrayList = arrayList;
        this.eventsFragment = eventsFragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custompubliclist, parent, false);
        return new PublicEventsAdapter.MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.event_name.setText(arrayList.get(position).getName());
        holder.event_desc.setText(arrayList.get(position).getDescription());
        if (Integer.parseInt(arrayList.get(position).getChat_count()) > 0)
            holder.message_count.setText(arrayList.get(position).getChat_count() + " messages");
        else
            holder.message_count.setText(arrayList.get(position).getChat_count() + " message");
        Glide.with(context).load(arrayList.get(position).getEvent_image()).placeholder(R.drawable.creater).override(300, 300).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.event_image);
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra("event_type", arrayList.get(position).getEvent_type());
                intent.putExtra("event_detail", arrayList.get(position));
                eventsFragment.startActivityForResult(intent, 201);
            }
        });

        holder.chat_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrayList.get(position).getAttend().equals("1")) {
                    Intent i = new Intent(context, ChatActivity.class);
                    i.putExtra("from_group", false);
                    i.putExtra("event_id", arrayList.get(position).getId());
                    i.putExtra("chat_name", arrayList.get(position).getName());
                    i.putExtra("chat_image", arrayList.get(position).getEvent_image());
                    context.startActivity(i);
                } else {
                    final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
                    dialog.setContentView(R.layout.no_data_dialog);
                    dialog.setCancelable(false);
                    dialog.show();

                    final TextView error = (TextView) dialog.findViewById(R.id.error);
                    TextView submit = (TextView) dialog.findViewById(R.id.submit);
                    error.setText("Please accept invitation request of event to continue chat");
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout parent;
        TextView event_name, message_count, event_desc;
        ImageView chat_icon, event_image;

        public MyViewHolder(View view) {
            super(view);
            parent = (LinearLayout) view.findViewById(R.id.parent);
            event_name = (TextView) view.findViewById(R.id.event_name);
            message_count = (TextView) view.findViewById(R.id.message_count);
            event_desc = (TextView) view.findViewById(R.id.event_desc);
            chat_icon = (ImageView) view.findViewById(R.id.chat_icon);
            event_image = (ImageView) view.findViewById(R.id.event_image);
        }
    }
}
