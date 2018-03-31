package com.nvite.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nvite.R;
import com.nvite.model.ChatEventListResponse;
import com.nvite.model.MessageEventListResponse;
import com.nvite.parser.AllInviteParameters;
import com.nvite.util.SavePref;
import com.nvite.util.Util;

import java.util.ArrayList;


/**
 * Created by V on 8/9/2017.
 */

public class ChattingEventAdapter extends BaseAdapter {
    private Context context;
    ArrayList<ChatEventListResponse> arrayList;
    SavePref savePref;

    public ChattingEventAdapter(Context context, ArrayList<ChatEventListResponse> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        savePref = new SavePref(context);
    }


    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final ChatEventListResponse myMessage = arrayList.get(i);
        view = inflater.inflate(R.layout.chat_row, null, false);

        RelativeLayout left_message_layout = (RelativeLayout) view.findViewById(R.id.left_message_layout);
        RelativeLayout left_image_layout = (RelativeLayout) view.findViewById(R.id.left_image_layout);
        ImageView left_arrow_image = (ImageView) view.findViewById(R.id.left_arrow_image);

        RelativeLayout right_message_layout = (RelativeLayout) view.findViewById(R.id.right_message_layout);
        RelativeLayout right_image_layout = (RelativeLayout) view.findViewById(R.id.right_image_layout);
        ImageView right_arrow_image = (ImageView) view.findViewById(R.id.right_arrow_image);

        TextView text_view_left = (TextView) view.findViewById(R.id.text_view_left);
        TextView message_username_left = (TextView) view.findViewById(R.id.message_username_left);
        TextView image_username_left = (TextView) view.findViewById(R.id.image_username_left);
        ImageView image_view_left = (ImageView) view.findViewById(R.id.image_view_left);

        TextView text_view_right = (TextView) view.findViewById(R.id.text_view_right);
        TextView message_username_right = (TextView) view.findViewById(R.id.message_username_right);
        TextView image_username_right = (TextView) view.findViewById(R.id.image_username_right);
        ImageView image_view_right = (ImageView) view.findViewById(R.id.image_view_right);


        if (savePref.getString(AllInviteParameters.USER_ID).equals(myMessage.getUser_id())) {
            left_message_layout.setVisibility(View.GONE);
            left_image_layout.setVisibility(View.GONE);
            left_arrow_image.setVisibility(View.GONE);
            if (myMessage.getMsg_type().equals("0")) {
                right_image_layout.setVisibility(View.GONE);
                right_arrow_image.setVisibility(View.GONE);
                right_message_layout.setVisibility(View.VISIBLE);
                text_view_right.setText(myMessage.getMsg());
                message_username_right.setText(myMessage.getUser().get(0).getUsername());

            } else {
                right_arrow_image.setVisibility(View.VISIBLE);
                right_image_layout.setVisibility(View.VISIBLE);
                right_message_layout.setVisibility(View.GONE);
                image_username_right.setText(myMessage.getUser().get(0).getUsername());
                Glide.with(context).load(myMessage.getMsg()).placeholder(R.drawable.camera_placeholder).override(200, 200).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(image_view_right);
            }
        } else {
            right_image_layout.setVisibility(View.GONE);
            right_message_layout.setVisibility(View.GONE);
            right_arrow_image.setVisibility(View.GONE);

            if (myMessage.getMsg_type().equals("0")) {
                left_message_layout.setVisibility(View.VISIBLE);
                left_image_layout.setVisibility(View.GONE);
                left_arrow_image.setVisibility(View.GONE);
                text_view_left.setText(myMessage.getMsg());
                message_username_left.setText(myMessage.getUser().get(0).getUsername());
            } else {
                left_message_layout.setVisibility(View.GONE);
                left_image_layout.setVisibility(View.VISIBLE);
                left_arrow_image.setVisibility(View.VISIBLE);
                image_username_left.setText(myMessage.getUser().get(0).getUsername());
                Glide.with(context).load(myMessage.getMsg()).placeholder(R.drawable.camera_placeholder).override(200, 200).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(image_view_left);
            }
        }
        image_view_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.viewImage(context, myMessage.getMsg());
            }
        });
        image_view_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.viewImage(context, myMessage.getMsg());
            }
        });
        return view;
    }
}
