package com.nvite.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nvite.R;
import com.nvite.adapter.ContactListAdapter;
import com.nvite.adapter.FriendListAdapter;
import com.nvite.model.FriendListResponse;
import com.nvite.parser.AllInviteApis;
import com.nvite.parser.AllInviteParameters;
import com.nvite.parser.GetAsync;
import com.nvite.util.ConnectivityReceiver;
import com.nvite.util.SavePref;
import com.nvite.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FriendListActivity extends BaseActivity {
    RecyclerView recyclerView;
    ImageView back;
    private ArrayList<FriendListResponse> arrayList;
    private FriendListAdapter adapter;
    SavePref savePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        savePref = new SavePref(FriendListActivity.this);
        back = (ImageView) findViewById(R.id.back);
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);

        arrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManagerBooks = new LinearLayoutManager(FriendListActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManagerBooks);

        adapter = new FriendListAdapter(FriendListActivity.this, arrayList, true);
        recyclerView.setAdapter(adapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if (ConnectivityReceiver.isConnected())
            getFriendListApi();
        else
            Util.showToast(FriendListActivity.this, getResources().getString(R.string.internet_error));

    }

    private void getFriendListApi() {

        final ProgressDialog mDialog = new ProgressDialog(FriendListActivity.this);
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Loading..");
        mDialog.show();
        Log.e("auth", "token " + savePref.getString(AllInviteParameters.AUTH_TOKEN));
        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);
        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));
        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(FriendListActivity.this, AllInviteApis.FRIEND_LIST, formBody, mDialog) {

            @Override
            public void getValueParse(Response response) {
                mDialog.dismiss();
                String result = "";
                try {
                    result = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("friend", "result " + result);
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.getString("code").equals("200")) {
                            if (arrayList.size() > 0)
                                arrayList.clear();

                            JSONArray jsonArray = jsonObject.getJSONArray("body");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                FriendListResponse friendListResponse = new FriendListResponse();
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                friendListResponse.setId(jsonObject1.getString("id"));
                                friendListResponse.setUsername(jsonObject1.getString("username"));
                                friendListResponse.setGender(jsonObject1.getString("gender"));
                                friendListResponse.setImage(jsonObject1.getString("image"));
                                friendListResponse.setScore(jsonObject1.getString("score"));
                                arrayList.add(friendListResponse);
                            }

                            if (arrayList.size() == 0) {
                                recyclerView.setVisibility(View.GONE);
                                openFriendListDialog();
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Util.showToast(FriendListActivity.this, jsonObject.getString("message"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.checkAuthToken(FriendListActivity.this, jsonObject.getString("message"), savePref);
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

    private void openFriendListDialog() {
        final Dialog dialog = new Dialog(FriendListActivity.this, R.style.Theme_Dialog);
        dialog.setContentView(R.layout.no_data_dialog);
        dialog.setCancelable(false);
        dialog.show();

        final TextView error = (TextView) dialog.findViewById(R.id.error);
        TextView submit = (TextView) dialog.findViewById(R.id.submit);
        error.setText("You have no friends");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 101) {
                if (ConnectivityReceiver.isConnected())
                    getFriendListApi();
            }
        }
    }
}
