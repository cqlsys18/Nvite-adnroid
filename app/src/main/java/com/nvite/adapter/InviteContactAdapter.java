package com.nvite.adapter;

import android.content.Context;
import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nvite.R;
import com.nvite.activity.EditProfileActivity;
import com.nvite.activity.InviteContactActivity;
import com.nvite.model.InvitationResponse;
import com.nvite.model.InviteContactResponse;
import com.nvite.parser.AllInviteParameters;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by AIM on 9/11/2017.
 */

public class InviteContactAdapter extends RecyclerView.Adapter<InviteContactAdapter.MyViewHolder> {
    Context context;
    ArrayList<InviteContactResponse> arrayList;

    public InviteContactAdapter(Context context, ArrayList<InviteContactResponse> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_contact_row, parent, false);
        return new InviteContactAdapter.MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if (arrayList.get(position).getGender().equals("1"))
            Glide.with(context).load(arrayList.get(position).getImage()).placeholder(R.drawable.male).override(200, 200).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.user_pic);
        else
            Glide.with(context).load(arrayList.get(position).getImage()).placeholder(R.drawable.female).override(200, 200).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.user_pic);

        holder.user_name.setText(arrayList.get(position).getUsername());

        if (arrayList.get(position).getIs_selected().equals("0")) {
            holder.plus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.plus_unselected));
        }else{
            holder.plus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.plus_selected));
        }
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (arrayList.get(position).getIs_selected().equals("0")) {
                    holder.plus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.plus_selected));
                    arrayList.get(position).setIs_selected("1");
                    ((InviteContactActivity) context).selectList(position, "1");
                } else {
                    holder.plus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.plus_unselected));
                    arrayList.get(position).setIs_selected("0");
                    ((InviteContactActivity) context).selectList(position, "0");
                }

            }
        });
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView user_pic;
        private TextView user_name;
        private ImageView plus;
        private RelativeLayout parent;

        public MyViewHolder(View view) {
            super(view);
            user_pic = (CircleImageView) view.findViewById(R.id.user_pic);
            user_name = (TextView) view.findViewById(R.id.user_name);
            plus = (ImageView) view.findViewById(R.id.plus);
            parent = (RelativeLayout) view.findViewById(R.id.parent);
        }
    }
}
