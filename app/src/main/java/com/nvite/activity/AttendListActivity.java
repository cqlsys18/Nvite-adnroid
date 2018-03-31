package com.nvite.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nvite.R;
import com.nvite.adapter.FriendListAdapter;
import com.nvite.model.FriendListResponse;
import com.nvite.model.InviteContactResponse;
import com.nvite.parser.AllInviteApis;
import com.nvite.parser.AllInviteParameters;
import com.nvite.parser.GetAsync;
import com.nvite.util.ConnectivityReceiver;
import com.nvite.util.SavePref;
import com.nvite.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AttendListActivity extends AppCompatActivity {
    private SavePref savePref;
    ArrayList<FriendListResponse> arrayList;
    private ImageView back;
    private RecyclerView recyclerView;
    FriendListAdapter adapter;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_list);
        savePref = new SavePref(AttendListActivity.this);

        back = (ImageView) findViewById(R.id.back);
        title = (TextView) findViewById(R.id.title);
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);

        arrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManagerBooks = new LinearLayoutManager(AttendListActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManagerBooks);

        adapter = new FriendListAdapter(AttendListActivity.this, arrayList,false);
        recyclerView.setAdapter(adapter);

        if (getIntent().getBooleanExtra("is_attending_list", false)) {
            title.setText("Attending");
        } else {
            title.setText("Invited");
        }

        if (ConnectivityReceiver.isConnected()) {
            getAttendListApi();
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void getAttendListApi() {
        String url = "";

        if (getIntent().getBooleanExtra("is_attending_list", false)) {
            url = AllInviteApis.ATTEND_EVENT_LIST;
        } else {
            url = AllInviteApis.INVITED_EVENT_LIST;
        }

        final ProgressDialog mDialog = new ProgressDialog(AttendListActivity.this);
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Loading..");
        mDialog.show();

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);
        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));
        formBuilder.addFormDataPart(AllInviteParameters.EVENT_ID, getIntent().getStringExtra("event_id"));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(AttendListActivity.this, url, formBody, mDialog) {

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
                            arrayList.add(friendListResponse);
                        }

                        if (arrayList.size() == 0) {
                            recyclerView.setVisibility(View.GONE);
                            openerrorDialog();
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.checkAuthToken(AttendListActivity.this, jsonObject.getString("message"), savePref);
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


    private void openerrorDialog() {
        final Dialog dialog = new Dialog(AttendListActivity.this, R.style.Theme_Dialog);
        dialog.setContentView(R.layout.no_data_dialog);
        dialog.setCancelable(false);
        dialog.show();

        final TextView error = (TextView) dialog.findViewById(R.id.error);
        TextView submit = (TextView) dialog.findViewById(R.id.submit);
        if (getIntent().getBooleanExtra("is_attending_list", false))
            error.setText("No one is attending");
        else
            error.setText("No one is invited");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });

    }
}
