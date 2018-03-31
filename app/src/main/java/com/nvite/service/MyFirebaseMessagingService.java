package com.nvite.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nvite.R;
import com.nvite.activity.ChatActivity;
import com.nvite.activity.FriendListActivity;
import com.nvite.activity.MainActivity;
import com.nvite.model.ChatEventListResponse;
import com.nvite.model.ChatGroupListResponse;
import com.nvite.model.FriendListResponse;
import com.nvite.model.UserResponse;
import com.nvite.parser.AllInviteApis;
import com.nvite.parser.AllInviteParameters;
import com.nvite.parser.GetAsync;
import com.nvite.util.SavePref;
import com.nvite.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by user on 10/27/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    SavePref savePref;
    private static int i;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        savePref = new SavePref(getApplicationContext());
        Log.e(TAG, "Notification Message Body: " + remoteMessage.getData());
        String notification_code = remoteMessage.getData().get("notification_code");
// if event chat
        if (notification_code.equals("6")) {
            String body = remoteMessage.getData().get("body");
            JSONObject jsonObject1 = null;
            try {
                jsonObject1 = new JSONObject(body);
                UserResponse userResponse = new UserResponse();
                ArrayList<UserResponse> userResponseArrayList = new ArrayList<>();
                ChatEventListResponse chatEventListResponse = new ChatEventListResponse();

                chatEventListResponse.setId(jsonObject1.getString("id"));
                chatEventListResponse.setEvent_id(jsonObject1.getString("event_id"));
                chatEventListResponse.setChat_name(jsonObject1.getString("event_name"));
                chatEventListResponse.setChat_image(jsonObject1.getString("event_image"));
                chatEventListResponse.setUser_id(jsonObject1.getString("user_id"));
                chatEventListResponse.setMsg(jsonObject1.getString("msg"));
                chatEventListResponse.setMsg_type(jsonObject1.getString("msg_type"));
                chatEventListResponse.setStatus(jsonObject1.getString("status"));

                JSONObject jsonObject2 = jsonObject1.getJSONObject("user");
                userResponse.setUsername(jsonObject2.getString("username"));
                userResponse.setImage(jsonObject2.getString("image"));
                userResponse.setId(jsonObject2.getString("id"));
                userResponseArrayList.add(userResponse);
                chatEventListResponse.setUser(userResponseArrayList);
                if (savePref.getString("user_online").equals("1") && savePref.getString("thread_id").equals(jsonObject1.getString("event_id"))) {
                    publishEventMessage(chatEventListResponse);
                } else {
                    sendMessageSendPush(getApplicationContext(), false, chatEventListResponse.getEvent_id(), chatEventListResponse, null);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (notification_code.equals("5")) {
            String body = remoteMessage.getData().get("body");
            JSONObject jsonObject1 = null;
            try {
                jsonObject1 = new JSONObject(body);
                UserResponse userResponse = new UserResponse();
                ArrayList<UserResponse> userResponseArrayList = new ArrayList<>();
                ChatGroupListResponse chatEventListResponse = new ChatGroupListResponse();

                chatEventListResponse.setId(jsonObject1.getString("id"));
                chatEventListResponse.setGroup_id(jsonObject1.getString("group_id"));
                chatEventListResponse.setChat_name(jsonObject1.getString("group_name"));
                chatEventListResponse.setChat_image(jsonObject1.getString("group_image"));
                chatEventListResponse.setUser_id(jsonObject1.getString("user_id"));
                chatEventListResponse.setMsg(jsonObject1.getString("message"));
                chatEventListResponse.setMsg_type(jsonObject1.getString("message_type"));
                chatEventListResponse.setStatus(jsonObject1.getString("status"));


                JSONObject jsonObject2 = jsonObject1.getJSONObject("user");
                userResponse.setUsername(jsonObject2.getString("username"));
                userResponse.setImage(jsonObject2.getString("image"));
                userResponse.setId(jsonObject2.getString("id"));
                userResponseArrayList.add(userResponse);
                chatEventListResponse.setUser(userResponseArrayList);
                if (savePref.getString("user_online").equals("1") && savePref.getString("thread_id").equals(jsonObject1.getString("group_id"))) {
                    publishEventMessageGroup(chatEventListResponse);
                } else {
                    sendMessageSendPush(getApplicationContext(), true, chatEventListResponse.getGroup_id(), null, chatEventListResponse);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            sendPushInviteEvent(getApplicationContext(), remoteMessage.getData().get("message"));
            Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    getNotificationCount();
                }
            };
            mainHandler.post(myRunnable);
        }

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendPushInviteEvent(Context context, String message) {
        Intent intent = null;

        intent = new Intent(context, MainActivity.class);
        intent.putExtra("from_push",true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notificationBuilder = null;

        notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setOngoing(false)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent).build();


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(i++, notificationBuilder);

        int notify_count = savePref.getInt("badge");
        notify_count = notify_count + 1;
        savePref.setInt("badge", notify_count);
        setBadge(getApplicationContext(), notify_count);
    }

    public static void setBadge(Context context, int count) {

        String launcherClassName = Util.getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sendMessageSendPush(Context context, boolean from_group, String thread_id, ChatEventListResponse chatEventListResponse, ChatGroupListResponse chatGroupListResponse) {
        Intent intent = null;
        String message = "", title = "";
        intent = new Intent(context, ChatActivity.class);
        intent.putExtra("from_group", from_group);
        if (from_group) {
            title = chatGroupListResponse.getUser().get(0).getUsername();
            if (chatGroupListResponse.getMsg_type().equals("0"))
                message = chatGroupListResponse.getMsg();
            else
                message = "Photo";
            intent.putExtra("group_id", thread_id);
            intent.putExtra("chat_name", chatGroupListResponse.getChat_name());
            intent.putExtra("chat_image", chatGroupListResponse.getChat_image());
        } else {
            title = chatEventListResponse.getUser().get(0).getUsername();
            if (chatEventListResponse.getMsg_type().equals("0"))
                message = chatEventListResponse.getMsg();
            else
                message = "Photo";
            intent.putExtra("event_id", thread_id);
            intent.putExtra("chat_name", chatEventListResponse.getChat_name());
            intent.putExtra("chat_image", chatEventListResponse.getChat_image());
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon1 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notificationBuilder = null;

        notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(icon1)
                .setContentTitle(title)
                .setOngoing(false)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent).build();


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(i++, notificationBuilder);

        int notify_count = savePref.getInt("badge");
        notify_count = notify_count + 1;
        savePref.setInt("badge", notify_count);
        setBadge(getApplicationContext(), notify_count);
    }

    private void getNotificationCount() {
        final ProgressDialog mDialog = new ProgressDialog(getApplicationContext());

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);
        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));
        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(getApplicationContext(), AllInviteApis.COUNT_NOTIFICATION, formBody, mDialog) {

            @Override
            public void getValueParse(Response response) {
                mDialog.dismiss();
                String result = "";
                try {
                    result = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("count", "result " + result);
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                        Log.e("count", "here " + jsonObject1.getString("count"));
                        if (MainActivity.tab_badge != null) {
                            if (Integer.parseInt(jsonObject1.getString("count")) > 0) {
                                MainActivity.tab_badge.setVisibility(View.VISIBLE);
                                MainActivity.tab_badge.setText(jsonObject1.getString("count"));
                            } else {
                                MainActivity.tab_badge.setVisibility(View.GONE);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.showToast(getApplicationContext(), jsonObject.getString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

    private void publishEventMessage(ChatEventListResponse chatEventListResponse) {
        Intent intent = new Intent(Util.NOTIFICATION_MESSAGE);
        intent.putExtra("message_detail", chatEventListResponse);
        intent.putExtra("from_group", false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void publishEventMessageGroup(ChatGroupListResponse chatGroupListResponse) {
        Intent intent = new Intent(Util.NOTIFICATION_MESSAGE);
        intent.putExtra("message_detail", chatGroupListResponse);
        intent.putExtra("from_group", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
