package com.nvite.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by AIM on 11/16/2017.
 */

public class ChatGroupListResponse implements Parcelable {
    public String getChat_name() {
        return chat_name;
    }

    public void setChat_name(String chat_name) {
        this.chat_name = chat_name;
    }

    public String getChat_image() {
        return chat_image;
    }

    public void setChat_image(String chat_image) {
        this.chat_image = chat_image;
    }

    private String id, group_id, user_id, msg, msg_type, status, is_seen, created,chat_name,chat_image;
    private ArrayList<UserResponse> user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIs_seen() {
        return is_seen;
    }

    public void setIs_seen(String is_seen) {
        this.is_seen = is_seen;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public ArrayList<UserResponse> getUser() {
        return user;
    }

    public void setUser(ArrayList<UserResponse> user) {
        this.user = user;
    }

    protected ChatGroupListResponse(Parcel in) {
        this.id = in.readString();
        this.group_id = in.readString();
        this.user_id = in.readString();
        this.msg = in.readString();
        this.msg_type = in.readString();
        this.status = in.readString();
        this.is_seen = in.readString();
        this.created = in.readString();
        this.chat_name = in.readString();
        this.chat_image = in.readString();
        user = in.createTypedArrayList(UserResponse.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(group_id);
        parcel.writeString(user_id);
        parcel.writeString(msg);
        parcel.writeString(msg_type);
        parcel.writeString(status);
        parcel.writeString(is_seen);
        parcel.writeString(created);
        parcel.writeString(chat_name);
        parcel.writeString(chat_image);
        parcel.writeTypedList(user);
    }

    public ChatGroupListResponse() {

    }


    public static final Creator<ChatGroupListResponse> CREATOR = new Creator<ChatGroupListResponse>() {
        @Override
        public ChatGroupListResponse createFromParcel(Parcel in) {
            return new ChatGroupListResponse(in);
        }

        @Override
        public ChatGroupListResponse[] newArray(int size) {
            return new ChatGroupListResponse[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }


}
