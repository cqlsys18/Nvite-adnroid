package com.nvite.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.nvite.R;

import com.nvite.activity.CreateEventActivity;
import com.nvite.activity.MainActivity;
import com.nvite.adapter.HomeAdapter;
import com.nvite.adapter.HorizontalAdapter;

import com.nvite.model.InvitationResponse;
import com.nvite.model.NotificationResponse;
import com.nvite.model.PrivateEventListResponse;
import com.nvite.model.SavedEventListResponse;
import com.nvite.model.SenderData;
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


public class HomeFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult> {
    View view;
    RecyclerView recycle_horizontal, recycle_vertical;
    HorizontalAdapter horizontalAdapter;
    HomeAdapter homeAdapter;
    FloatingActionButton fab;

    private MenuItem menu_search, menu_more;
    SavePref savePref;
    private ArrayList<SavedEventListResponse> arrayListSavedEvent;
    private ArrayList<PrivateEventListResponse> arrayListPrivateEvent;
    private TextView error_saved_event, error_private_event;
    String result = "";
    String result_saved = "";
    SwipeRefreshLayout swipe_refresh_layout;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected GoogleApiClient mGoogleApiClient;
    Location mcurrentlocation;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;

    boolean is_update_called = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);
        savePref = new SavePref(getActivity());
        initialize();

        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
//        checkLocationSettings();
        if (ConnectivityReceiver.isConnected()) {
            getPrivateEvents(true);
            getRecentEventsAPi(true);

        } else
            Util.showToast(getActivity(), getResources().getString(R.string.internet_error));
        return view;
    }

    private void initialize() {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        swipe_refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        recycle_horizontal = (RecyclerView) view.findViewById(R.id.recycle_horizontal);
        recycle_vertical = (RecyclerView) view.findViewById(R.id.recycle_vertical);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        error_saved_event = (TextView) view.findViewById(R.id.error_saved_event);
        error_private_event = (TextView) view.findViewById(R.id.error_private_event);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateEventActivity.class);
                startActivity(intent);
            }
        });

        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ConnectivityReceiver.isConnected()) {
                    getPrivateEvents(false);
                    getRecentEventsAPi(false);
                }
            }
        });
       /* add_new_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_new_group.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity(), ContactlistActivity.class);
                startActivity(intent);
            }
        });*/
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.popup, menu);
//        menu_search = menu.findItem(R.id.menu_search);
//        menu_more = menu.findItem(R.id.menu_more);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_more:
               /* if (add_new_group.getVisibility() == View.VISIBLE) {
                    add_new_group.setVisibility(View.GONE);
                } else {
                    add_new_group.setVisibility(View.VISIBLE);
                }*/
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void getRecentEventsAPi(boolean show_progress) {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);

        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(getActivity(), AllInviteApis.SAVED_EVENT_LIST, formBody, progressDialog) {

            @Override
            public void getValueParse(final Response response) {
                swipe_refresh_layout.setRefreshing(false);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            try {
                                result_saved = response.body().string();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.e("saved", "result " + result_saved);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("chat", "result " + result_saved);
                                    if (response.isSuccessful()) {
                                        getNotificationCount();
                                        arrayListSavedEvent = new ArrayList<>();
                                        try {
                                            JSONObject jsonObject = new JSONObject(result_saved);
                                            JSONArray jsonArray = jsonObject.getJSONArray("body");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                ArrayList<InvitationResponse> invitationResponseArrayList = new ArrayList<>();
                                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                                SavedEventListResponse savedEventListResponse = new SavedEventListResponse();
                                                savedEventListResponse.setId(jsonObject1.getString("id"));
                                                savedEventListResponse.setUser_id(jsonObject1.getString("user_id"));
                                                savedEventListResponse.setName(jsonObject1.getString("name"));
                                                savedEventListResponse.setEvent_image(jsonObject1.getString("event_image"));
                                                savedEventListResponse.setEvent_type(jsonObject1.getString("event_type"));
                                                savedEventListResponse.setEvent_date(jsonObject1.getString("event_date"));
                                                savedEventListResponse.setStart_time(jsonObject1.getString("start_time"));
                                                savedEventListResponse.setEnd_time(jsonObject1.getString("end_time"));
                                                savedEventListResponse.setAddress(jsonObject1.getString("address"));
                                                savedEventListResponse.setState(jsonObject1.getString("state"));
                                                savedEventListResponse.setCity(jsonObject1.getString("city"));
                                                savedEventListResponse.setLatitude(jsonObject1.getString("latitude"));
                                                savedEventListResponse.setLongitude(jsonObject1.getString("longitude"));
                                                savedEventListResponse.setDescription(jsonObject1.getString("description"));
                                                savedEventListResponse.setStatus(jsonObject1.getString("status"));
                                                savedEventListResponse.setCreated(jsonObject1.getString("created"));
                                                savedEventListResponse.setTotal_attend(jsonObject1.getString("total_attend"));
                                                savedEventListResponse.setMale(jsonObject1.getString("male"));
                                                savedEventListResponse.setFemale(jsonObject1.getString("female"));
                                                savedEventListResponse.setOther(jsonObject1.getString("other"));
                                                savedEventListResponse.setSave(jsonObject1.getString("save"));
                                                savedEventListResponse.setAttend(jsonObject1.getString("attend"));
                                                savedEventListResponse.setUser_name(jsonObject1.getString("user_name"));

                                                JSONArray jsonArray1 = jsonObject1.getJSONArray("invition");

                                                for (int j = 0; j < jsonArray1.length(); j++) {
                                                    InvitationResponse invitationResponse = new InvitationResponse();
                                                    JSONObject jsonObject2 = jsonArray1.getJSONObject(j);
                                                    invitationResponse.setSenderId(jsonObject2.getString("senderId"));
                                                    invitationResponse.setUserId(jsonObject2.getString("userId"));
                                                    invitationResponseArrayList.add(invitationResponse);
                                                }
                                                savedEventListResponse.setArrayList(invitationResponseArrayList);
                                                arrayListSavedEvent.add(savedEventListResponse);
                                            }

                                            horizontalAdapter = new HorizontalAdapter(getActivity(), arrayListSavedEvent, HomeFragment.this);
                                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                                            recycle_horizontal.setLayoutManager(linearLayoutManager);
                                            recycle_horizontal.setAdapter(horizontalAdapter);

                                            if (arrayListSavedEvent.size() > 0) {
                                                recycle_horizontal.setVisibility(View.VISIBLE);
                                                error_saved_event.setVisibility(View.GONE);
                                            } else {
                                                recycle_horizontal.setVisibility(View.GONE);
                                                error_saved_event.setVisibility(View.VISIBLE);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            JSONObject jsonObject = new JSONObject(result_saved);
                                            Util.showToast(getActivity(), jsonObject.getString("message"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();

            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

    private void getPrivateEvents(boolean show_progres) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        if (show_progres)
            progressDialog.show();
        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);

        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(getActivity(), AllInviteApis.PRIVATE_EVENT_LIST, formBody, progressDialog) {

            @Override
            public void getValueParse(final Response response) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            try {
                                result = response.body().string();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.e("private", "result " + result);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("chat", "result " + result);
                                    if (response.isSuccessful()) {
                                        arrayListPrivateEvent = new ArrayList<>();
                                        try {
                                            JSONObject jsonObject = new JSONObject(result);
                                            JSONArray jsonArray = jsonObject.getJSONArray("body");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                ArrayList<InvitationResponse> invitationResponseArrayList = new ArrayList<>();
                                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                                PrivateEventListResponse privateEventListResponse = new PrivateEventListResponse();
                                                privateEventListResponse.setId(jsonObject1.getString("id"));
                                                privateEventListResponse.setUser_id(jsonObject1.getString("user_id"));
                                                privateEventListResponse.setName(jsonObject1.getString("name"));
                                                privateEventListResponse.setEvent_image(jsonObject1.getString("event_image"));
                                                privateEventListResponse.setEvent_type(jsonObject1.getString("event_type"));
                                                privateEventListResponse.setEvent_date(jsonObject1.getString("event_date"));
                                                privateEventListResponse.setStart_time(jsonObject1.getString("start_time"));
                                                privateEventListResponse.setEnd_time(jsonObject1.getString("end_time"));
                                                privateEventListResponse.setAddress(jsonObject1.getString("address"));
                                                privateEventListResponse.setState(jsonObject1.getString("state"));
                                                privateEventListResponse.setCity(jsonObject1.getString("city"));
                                                privateEventListResponse.setLatitude(jsonObject1.getString("latitude"));
                                                privateEventListResponse.setLongitude(jsonObject1.getString("longitude"));
                                                privateEventListResponse.setDescription(jsonObject1.getString("description"));
                                                privateEventListResponse.setStatus(jsonObject1.getString("status"));
                                                privateEventListResponse.setCreated(jsonObject1.getString("created"));
                                                privateEventListResponse.setTotal_attend(jsonObject1.getString("total_attend"));
                                                privateEventListResponse.setMale(jsonObject1.getString("male"));
                                                privateEventListResponse.setFemale(jsonObject1.getString("female"));
                                                privateEventListResponse.setOther(jsonObject1.getString("other"));
                                                privateEventListResponse.setSave(jsonObject1.getString("save"));
//
                                                privateEventListResponse.setUser_name(jsonObject1.getString("user_name"));
                                                privateEventListResponse.setUnseen_msg(jsonObject1.getString("unseen_msg"));
                                                privateEventListResponse.setChat_count(jsonObject1.getString("chat_count"));

                                                JSONArray jsonArray1 = jsonObject1.optJSONArray("invition");
                                                if (jsonArray1 != null) {
                                                    for (int j = 0; j < jsonArray1.length(); j++) {
                                                        InvitationResponse invitationResponse = new InvitationResponse();
                                                        JSONObject jsonObject2 = jsonArray1.getJSONObject(j);
                                                        invitationResponse.setSenderId(jsonObject2.getString("senderId"));
                                                        invitationResponse.setUserId(jsonObject2.getString("userId"));
                                                        invitationResponseArrayList.add(invitationResponse);
                                                    }
                                                }
                                                privateEventListResponse.setArrayList(invitationResponseArrayList);
                                                privateEventListResponse.setAttend(jsonObject1.optString("attend"));
                                                arrayListPrivateEvent.add(privateEventListResponse);
                                            }

                                            homeAdapter = new HomeAdapter(getActivity(), arrayListPrivateEvent, HomeFragment.this);
                                            LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                            recycle_vertical.setLayoutManager(linearLayoutManager1);
                                            recycle_vertical.setAdapter(homeAdapter);

                                            if (arrayListPrivateEvent.size() > 0) {
                                                recycle_vertical.setVisibility(View.VISIBLE);
                                                error_private_event.setVisibility(View.GONE);
                                            } else {
                                                recycle_vertical.setVisibility(View.GONE);
                                                error_private_event.setVisibility(View.VISIBLE);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            JSONObject jsonObject = new JSONObject(result);
                                            Util.checkAuthToken(getActivity(), jsonObject.getString("message"), savePref);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }


    private void getNotificationCount() {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);
        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));
        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(getActivity(), AllInviteApis.COUNT_NOTIFICATION, formBody, progressDialog) {

            @Override
            public void getValueParse(Response response) {

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

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000 / 2);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }


    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {

            }
        });
        if (getActivity() != null) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                    }
                    if (mcurrentlocation == null) {
                        mcurrentlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        if (mcurrentlocation != null) {
                            if (!is_update_called) {
                                savePref.setString(AllInviteParameters.LATITUDE, String.valueOf(mcurrentlocation.getLatitude()));
                                savePref.setString(AllInviteParameters.LONGITUDE, String.valueOf(mcurrentlocation.getLongitude()));
                                if (ConnectivityReceiver.isConnected())
                                    updateCurrentLatLong();
                            }
                        }
                    } else {
                        if (!is_update_called) {
                            savePref.setString(AllInviteParameters.LATITUDE, String.valueOf(mcurrentlocation.getLatitude()));
                            savePref.setString(AllInviteParameters.LONGITUDE, String.valueOf(mcurrentlocation.getLongitude()));
                            if (ConnectivityReceiver.isConnected())
                                updateCurrentLatLong();
                        }
                    }
                }
            }, 1000);
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLocationSettings();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        is_update_called = false;
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (mcurrentlocation == null) {
            mcurrentlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mcurrentlocation != null) {
                if (!is_update_called) {
                    savePref.setString(AllInviteParameters.LATITUDE, String.valueOf(mcurrentlocation.getLatitude()));
                    savePref.setString(AllInviteParameters.LONGITUDE, String.valueOf(mcurrentlocation.getLongitude()));
                    if (ConnectivityReceiver.isConnected())
                        updateCurrentLatLong();
                }
            }
        } else {
            if (!is_update_called) {
                savePref.setString(AllInviteParameters.LATITUDE, String.valueOf(mcurrentlocation.getLatitude()));
                savePref.setString(AllInviteParameters.LONGITUDE, String.valueOf(mcurrentlocation.getLongitude()));
                if (ConnectivityReceiver.isConnected())
                    updateCurrentLatLong();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {

                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }

        /*if (resultCode == RESULT_OK) {
            *//*if (requestCode == 202) {
                if(data.getBooleanExtra("is_save",false)){
                    String event_id = data.getStringExtra("event_id");
                    SavedEventListResponse savedEventListResponse = new SavedEventListResponse();
                    PrivateEventListResponse privateEventListResponse = data.getParcelableExtra("event_detail");
                    for (int i = 0; i < arrayListPrivateEvent.size(); i++) {
                        if (arrayListPrivateEvent.get(i).getId().equals(event_id)) {
                            arrayListPrivateEvent.get(i).setSave("1");
                            break;
                        }
                    }

                    savedEventListResponse.setId(privateEventListResponse.getId());
                    savedEventListResponse.setUser_id(privateEventListResponse.getUser_id());
                    savedEventListResponse.setName(privateEventListResponse.getName());
                    savedEventListResponse.setEvent_image(privateEventListResponse.getEvent_image());
                    savedEventListResponse.setEvent_type(privateEventListResponse.getEvent_type());
                    savedEventListResponse.setEvent_date(privateEventListResponse.getEvent_date());
                    savedEventListResponse.setStart_time(privateEventListResponse.getStart_time());
                    savedEventListResponse.setEnd_time(privateEventListResponse.getEnd_time());
                    savedEventListResponse.setAddress(privateEventListResponse.getAddress());
                    savedEventListResponse.setState(privateEventListResponse.getState());
                    savedEventListResponse.setCity(privateEventListResponse.getCity());
                    savedEventListResponse.setLatitude(privateEventListResponse.getLatitude());
                    savedEventListResponse.setLongitude(privateEventListResponse.getLongitude());
                    savedEventListResponse.setDescription(privateEventListResponse.getDescription());
                    savedEventListResponse.setStatus(privateEventListResponse.getStatus());
                    savedEventListResponse.setCreated(privateEventListResponse.getCreated());
                    savedEventListResponse.setTotal_attend(privateEventListResponse.getTotal_attend());
                    savedEventListResponse.setMale(privateEventListResponse.getMale());
                    savedEventListResponse.setFemale(privateEventListResponse.getFemale());
                    savedEventListResponse.setOther(privateEventListResponse.getOther());
                    savedEventListResponse.setSave(privateEventListResponse.getSave());
                    savedEventListResponse.setAttend(privateEventListResponse.getAttend());
                    savedEventListResponse.setUser_name(privateEventListResponse.getUser_name());
                    savedEventListResponse.setArrayList(privateEventListResponse.getArrayList());
                    arrayListSavedEvent.add(0,savedEventListResponse);

                    homeAdapter.notifyDataSetChanged();
                    horizontalAdapter.notifyDataSetChanged();
                }else{
                    String event_id = data.getStringExtra("event_id");
                    SavedEventListResponse savedEventListResponse = new SavedEventListResponse();
                    PrivateEventListResponse privateEventListResponse = data.getParcelableExtra("event_detail");
                    for (int i = 0; i < arrayListPrivateEvent.size(); i++) {
                        if (arrayListPrivateEvent.get(i).getId().equals(event_id)) {
                            arrayListPrivateEvent.get(i).setSave("1");
                            break;
                        }
                    }

                    savedEventListResponse.setId(privateEventListResponse.getId());
                    savedEventListResponse.setUser_id(privateEventListResponse.getUser_id());
                    savedEventListResponse.setName(privateEventListResponse.getName());
                    savedEventListResponse.setEvent_image(privateEventListResponse.getEvent_image());
                    savedEventListResponse.setEvent_type(privateEventListResponse.getEvent_type());
                    savedEventListResponse.setEvent_date(privateEventListResponse.getEvent_date());
                    savedEventListResponse.setStart_time(privateEventListResponse.getStart_time());
                    savedEventListResponse.setEnd_time(privateEventListResponse.getEnd_time());
                    savedEventListResponse.setAddress(privateEventListResponse.getAddress());
                    savedEventListResponse.setState(privateEventListResponse.getState());
                    savedEventListResponse.setCity(privateEventListResponse.getCity());
                    savedEventListResponse.setLatitude(privateEventListResponse.getLatitude());
                    savedEventListResponse.setLongitude(privateEventListResponse.getLongitude());
                    savedEventListResponse.setDescription(privateEventListResponse.getDescription());
                    savedEventListResponse.setStatus(privateEventListResponse.getStatus());
                    savedEventListResponse.setCreated(privateEventListResponse.getCreated());
                    savedEventListResponse.setTotal_attend(privateEventListResponse.getTotal_attend());
                    savedEventListResponse.setMale(privateEventListResponse.getMale());
                    savedEventListResponse.setFemale(privateEventListResponse.getFemale());
                    savedEventListResponse.setOther(privateEventListResponse.getOther());
                    savedEventListResponse.setSave(privateEventListResponse.getSave());
                    savedEventListResponse.setAttend(privateEventListResponse.getAttend());
                    savedEventListResponse.setUser_name(privateEventListResponse.getUser_name());
                    savedEventListResponse.setArrayList(privateEventListResponse.getArrayList());
                    arrayListSavedEvent.add(0,savedEventListResponse);

                    homeAdapter.notifyDataSetChanged();
                    horizontalAdapter.notifyDataSetChanged();
                }


            }*//*
        }*/
    }


    private void updateCurrentLatLong() {
        Log.e("currentt", "lat " + savePref.getString(AllInviteParameters.LONGITUDE));
        is_update_called = true;
        ProgressDialog progressDialog = null;
        if (getActivity() != null)
            progressDialog = new ProgressDialog(getActivity());
        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);

        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));
        formBuilder.addFormDataPart(AllInviteParameters.LATITUDE, savePref.getString(AllInviteParameters.LATITUDE));
        formBuilder.addFormDataPart(AllInviteParameters.LONGITUDE, savePref.getString(AllInviteParameters.LONGITUDE));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(getActivity(), AllInviteApis.UPDATE_LOCATION, formBody, progressDialog) {

            @Override
            public void getValueParse(final Response response) {
                swipe_refresh_layout.setRefreshing(false);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (response.isSuccessful()) {

                                    } else {
                                        try {
                                            JSONObject jsonObject = new JSONObject(result_saved);
                                            Util.showToast(getActivity(), jsonObject.getString("message"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();

            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }


}
