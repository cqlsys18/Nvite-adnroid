package com.nvite.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nvite.R;
import com.nvite.activity.NewGroupActivity;
import com.nvite.adapter.HorizontalAdapter;
import com.nvite.adapter.MessageEventListAdapter;
import com.nvite.adapter.MessageGroupListAdapter;
import com.nvite.model.MessageEventListResponse;
import com.nvite.model.MessageGroupListResponse;
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

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {
    RecyclerView recycle_group, recycle_events;
    View view;
    FloatingActionButton fab;
    SavePref savePref;
    private ArrayList<MessageEventListResponse> eventList;
    private ArrayList<MessageGroupListResponse> groupList;
    private MessageEventListAdapter eventAdapter;
    private MessageGroupListAdapter groupAdapter;
    TextView group_list, event_list, error;
    SwipeRefreshLayout swipe_refresh_layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_message, container, false);
        savePref = new SavePref(getActivity());
        initialize();

        if (ConnectivityReceiver.isConnected())
            getGroupMessageList(true);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NewGroupActivity.class);
                startActivity(intent);
            }
        });

        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ConnectivityReceiver.isConnected()) {
                    if (recycle_group.getVisibility() == View.VISIBLE)
                        getGroupMessageList(false);
                    else if (recycle_events.getVisibility() == View.VISIBLE)
                        getEventMessageList(false);
                }
            }
        });
        return view;
    }

    private void initialize() {
        swipe_refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        recycle_group = (RecyclerView) view.findViewById(R.id.recycle_group);
        recycle_events = (RecyclerView) view.findViewById(R.id.recycle_events);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        group_list = (TextView) view.findViewById(R.id.group_list);
        event_list = (TextView) view.findViewById(R.id.event_list);
        error = (TextView) view.findViewById(R.id.error);

        eventList = new ArrayList<>();
        eventAdapter = new MessageEventListAdapter(getActivity(), eventList, MessageFragment.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recycle_events.setLayoutManager(linearLayoutManager);
        recycle_events.setAdapter(eventAdapter);


        groupList = new ArrayList<>();
        groupAdapter = new MessageGroupListAdapter(getActivity(), groupList, MessageFragment.this);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recycle_group.setLayoutManager(linearLayoutManager1);
        recycle_group.setAdapter(groupAdapter);

        group_list.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        group_list.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        event_list.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_gray));
        event_list.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
        recycle_group.setVisibility(View.VISIBLE);
        recycle_events.setVisibility(View.GONE);

        group_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                group_list.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                group_list.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                event_list.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_gray));
                event_list.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                recycle_group.setVisibility(View.VISIBLE);
                recycle_events.setVisibility(View.GONE);

                if (ConnectivityReceiver.isConnected())
                    getGroupMessageList(true);

            }
        });

        event_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                event_list.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                event_list.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                group_list.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_gray));
                group_list.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                recycle_events.setVisibility(View.VISIBLE);
                recycle_group.setVisibility(View.GONE);

                if (ConnectivityReceiver.isConnected())
                    getEventMessageList(true);


            }
        });
    }


    private void getGroupMessageList(boolean show_progress) {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        if (show_progress)
            progressDialog.show();

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);

        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(getActivity(), AllInviteApis.GET_CHAT_GROUPS, formBody, progressDialog) {

            @Override
            public void getValueParse(Response response) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                swipe_refresh_layout.setRefreshing(false);
                String result = "";
                try {
                    result = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("group_list", "result " + result);
                if (response.isSuccessful()) {


                    if (groupList.size() > 0)
                        groupList.clear();

                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray jsonArray = jsonObject.getJSONArray("body");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            MessageGroupListResponse groupListResponse = new MessageGroupListResponse();
                            groupListResponse.setId(jsonObject1.getString("id"));
                            groupListResponse.setName(jsonObject1.getString("name"));
                            groupListResponse.setImage(jsonObject1.getString("image"));
                            groupListResponse.setUser_id(jsonObject1.getString("user_id"));
                            groupListResponse.setStatus(jsonObject1.getString("status"));
                            groupListResponse.setCreated(jsonObject1.getString("created"));
                            groupListResponse.setModified(jsonObject1.getString("modified"));
                            groupListResponse.setUnseen_msg(jsonObject1.getString("unseen_msg"));

                            groupList.add(groupListResponse);
                        }
//                        groupAdapter.notifyDataSetChanged();

                        groupAdapter = new MessageGroupListAdapter(getActivity(), groupList, MessageFragment.this);
                        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                        recycle_group.setLayoutManager(linearLayoutManager1);
                        recycle_group.setAdapter(groupAdapter);
                        if (groupList.size() > 0) {
                            recycle_group.setVisibility(View.VISIBLE);
                            error.setVisibility(View.GONE);
                        } else {
                            recycle_group.setVisibility(View.GONE);
                            error.setVisibility(View.VISIBLE);
                            error.setText("No Group chat available");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.showToast(getActivity(), jsonObject.getString("message"));
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

    private void getEventMessageList(boolean show_progress) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        if (show_progress)
            progressDialog.show();

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);

        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(getActivity(), AllInviteApis.GET_CHAT_EVENTS, formBody, progressDialog) {

            @Override
            public void getValueParse(Response response) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                swipe_refresh_layout.setRefreshing(false);
                String result = "";
                try {
                    result = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("event_list", "result " + result);
                if (response.isSuccessful()) {

                    if (eventList.size() > 0)
                        eventList.clear();

                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray jsonArray = jsonObject.getJSONArray("body");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            MessageEventListResponse eventListResponse = new MessageEventListResponse();
                            eventListResponse.setName(jsonObject1.getString("name"));
                            eventListResponse.setEvent_image(jsonObject1.getString("event_image"));
                            eventListResponse.setId(jsonObject1.getString("id"));
                            eventListResponse.setDescription(jsonObject1.getString("description"));
                            eventListResponse.setUnseen_msg(jsonObject1.getString("unseen_msg"));
                            eventList.add(eventListResponse);
                        }
//                        eventAdapter.notifyDataSetChanged();

                        eventAdapter = new MessageEventListAdapter(getActivity(), eventList, MessageFragment.this);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                        recycle_events.setLayoutManager(linearLayoutManager);
                        recycle_events.setAdapter(eventAdapter);

                        if (eventList.size() > 0) {
                            recycle_events.setVisibility(View.VISIBLE);
                            error.setVisibility(View.GONE);
                        } else {
                            recycle_events.setVisibility(View.GONE);
                            error.setVisibility(View.VISIBLE);
                            error.setText("No Event chat available");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.showToast(getActivity(), jsonObject.getString("message"));
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 202) {
                if (data.getBooleanExtra("from_group", false)) {
                    for (int i = 0; i < groupList.size(); i++) {
                        if (data.getStringExtra("group_id").equals(groupList.get(i).getId())) {
                            groupList.get(i).setUnseen_msg("0");
                            break;
                        }
                    }
                    groupAdapter.notifyDataSetChanged();
                } else {
                    for (int i = 0; i < eventList.size(); i++) {
                        if (data.getStringExtra("event_id").equals(eventList.get(i).getId())) {
                            eventList.get(i).setUnseen_msg("0");
                            break;
                        }
                    }
                    eventAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
