package com.nvite.activity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nvite.R;
import com.nvite.adapter.InviteContactAdapter;
import com.nvite.adapter.PublicEventsAdapter;
import com.nvite.model.InviteContactResponse;
import com.nvite.parser.AllInviteApis;
import com.nvite.parser.AllInviteParameters;
import com.nvite.parser.GetAsync;
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

public class InviteContactActivity extends BaseActivity {
    private ImageView back;
    private RecyclerView recyclerView;
    private Button create;
    ArrayList<InviteContactResponse> arrayList;
    String selected_id = "";
    JSONArray list;
    private final int LIMIT = 5000;
    private int offset = 0;
    String contactNumber, nameContact;
    private SavePref savePref;
    InviteContactAdapter adapter;
    TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_contact);
        savePref = new SavePref(InviteContactActivity.this);
        back = (ImageView) findViewById(R.id.back);
        create = (Button) findViewById(R.id.create);
        error = (TextView) findViewById(R.id.error);
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        final ProgressDialog mDialog = new ProgressDialog(InviteContactActivity.this);
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Loading...");
        mDialog.show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadContacts(mDialog);
            }
        });
        thread.start();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (arrayList.size() == 0) {
                    Util.showSnackBar(view, "Please select atleast one friend", InviteContactActivity.this);
                } else {
                    selected_id = "";
                    for (int i = 0; i < arrayList.size(); i++) {
                        if (arrayList.get(i).getIs_selected().equals("1"))
                            if (selected_id.equals(""))
                                selected_id = arrayList.get(i).getId();
                            else
                                selected_id = selected_id + "," + arrayList.get(i).getId();
                    }
                    if (selected_id.equals("")) {
                        Util.showSnackBar(view, "Please select atleast one friend", InviteContactActivity.this);
                    } else {
                        createEventApi();
                    }
                }
            }
        });

    }

    private void loadContacts(final ProgressDialog progressDialog) {


        list = new JSONArray();
        String sortOrder = String.format("%s limit " + offset + ", " + LIMIT,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1", null, sortOrder);

        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                        while (pCur.moveToNext()) {
                            contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            nameContact = pCur.getString(pCur.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

                            try {


                                // creating json array
                                JSONObject obj = new JSONObject();
                                contactNumber = contactNumber.replace(" ", "");
                                contactNumber = contactNumber.replace("+", "");
                                obj.put("name", nameContact);
                                obj.put("phone", contactNumber);
                                list.put(obj);

                                /*if (contactNumber.length() >= 7) {
                                    try {
                                        contactNumber = contactNumber.substring(contactNumber.length() - 7);
                                        obj.put("name", nameContact);
                                        obj.put("phone", contactNumber);
                                        list.put(obj);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }*/
                            } catch (Exception e) {
                                Log.d("NumberParseException", "======" + e.toString());
                            }
                            break;
                        }

                        pCur.close();
                    }
                }
                while (cursor.moveToNext());
            }
            hitContactApi(progressDialog);
        }
    }

    private void hitContactApi(final ProgressDialog progressDialog) {
        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);

        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));
        formBuilder.addFormDataPart(AllInviteParameters.CONTACT, String.valueOf(list));

        RequestBody formBody = formBuilder.build();

        Log.e("data", "contact " + Util.bodyToString(formBody));

        GetAsync mAsync = new GetAsync(this, AllInviteApis.CONTACT_LIST, formBody, progressDialog) {

            @Override
            public void getValueParse(Response response) {
                progressDialog.dismiss();
                String result = "";
                try {
                    result = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("invite", "result " + result);
                if (response.isSuccessful()) {
                    try {
                        arrayList = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray jsonArray = jsonObject.getJSONArray("body");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            InviteContactResponse inviteContactResponse = new InviteContactResponse();
                            if (!savePref.getString(AllInviteParameters.USER_ID).equals(jsonObject1.getString("id"))) {
                                inviteContactResponse.setId(jsonObject1.getString("id"));
                                inviteContactResponse.setImage(jsonObject1.getString("image"));
                                inviteContactResponse.setUsername(jsonObject1.getString("username"));
                                inviteContactResponse.setGender(jsonObject1.getString("gender"));
                                inviteContactResponse.setScore(jsonObject1.getString("score"));
                                inviteContactResponse.setIs_selected("0");
                                arrayList.add(inviteContactResponse);
                            }
                        }

                        adapter = new InviteContactAdapter(InviteContactActivity.this, arrayList);
                        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(InviteContactActivity.this, LinearLayoutManager.VERTICAL, false);
                        recyclerView.setLayoutManager(linearLayoutManager1);
                        recyclerView.setAdapter(adapter);

                        if (arrayList.size() > 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            error.setVisibility(View.GONE);
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            error.setVisibility(View.VISIBLE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.checkAuthToken(InviteContactActivity.this, jsonObject.getString("message"), savePref);
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


    private void createEventApi() {

        MediaType mediaType = null;
        final ProgressDialog mDialog = new ProgressDialog(InviteContactActivity.this);
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Loading..");
        mDialog.show();

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);
        if (!getIntent().getStringExtra("event_image").equalsIgnoreCase("")) {
            mediaType = getIntent().getStringExtra("event_image").endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            File file = new File(getIntent().getStringExtra("event_image"));
            formBuilder.addFormDataPart(AllInviteParameters.EVENT_IMAGE, file.getName(), RequestBody.create(mediaType, file));
        }

        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));
        formBuilder.addFormDataPart(AllInviteParameters.NAME, getIntent().getStringExtra("event_name"));
        formBuilder.addFormDataPart(AllInviteParameters.EVENT_TYPE, getIntent().getStringExtra("event_type"));
        formBuilder.addFormDataPart(AllInviteParameters.EVENT_DATE, getIntent().getStringExtra("event_date"));
        formBuilder.addFormDataPart(AllInviteParameters.START_TIME, getIntent().getStringExtra("start_time"));
        formBuilder.addFormDataPart(AllInviteParameters.END_TIME, getIntent().getStringExtra("end_time"));
        formBuilder.addFormDataPart(AllInviteParameters.LATITUDE, getIntent().getStringExtra("lat"));
        formBuilder.addFormDataPart(AllInviteParameters.LONGITUDE, getIntent().getStringExtra("long"));
        formBuilder.addFormDataPart(AllInviteParameters.ADDRESS, getIntent().getStringExtra("address"));
        formBuilder.addFormDataPart(AllInviteParameters.CITY, getIntent().getStringExtra("city"));
        formBuilder.addFormDataPart(AllInviteParameters.ZIP, getIntent().getStringExtra("zip"));
        formBuilder.addFormDataPart(AllInviteParameters.DESCRIPTION, getIntent().getStringExtra("desc"));
        formBuilder.addFormDataPart(AllInviteParameters.CONTACT, selected_id);


        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllInviteApis.CREATE_EVENT, formBody, mDialog) {

            @Override
            public void getValueParse(Response response) {
                mDialog.dismiss();
                String result = "";
                try {
                    result = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("signup", "result " + result);
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.showToast(InviteContactActivity.this, "Event created successfully");
                        Intent intent = new Intent(InviteContactActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.checkAuthToken(InviteContactActivity.this, jsonObject.getString("message"), savePref);
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

    public void selectList(int pos, String is_select) {

        arrayList.get(pos).setIs_selected(is_select);
    }
}
