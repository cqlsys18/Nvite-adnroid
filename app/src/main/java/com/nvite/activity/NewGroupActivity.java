package com.nvite.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nvite.R;
import com.nvite.adapter.HorizontalAdapter;
import com.nvite.adapter.NewGroupHorizontalAdapter;
import com.nvite.model.FriendListResponse;
import com.nvite.model.MessageEventListResponse;
import com.nvite.parser.AllInviteApis;
import com.nvite.parser.AllInviteParameters;
import com.nvite.parser.GetAsync;
import com.nvite.util.ConnectivityReceiver;
import com.nvite.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewGroupActivity extends BaseActivity {
    ImageView back, tick;
    RecyclerView recycle_horizontal;
    NewGroupHorizontalAdapter adapter;
    private ArrayList<FriendListResponse> arrayList;
    CircleImageView group_image;
    ImageView camera;
    EditText group_name;
    private static int SELECT_FILE = 200;
    private static int REQUEST_CAMERA = 201;
    private Uri file_uri;
    String picture_path = "";
    File file_upload = null;
    String selected_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        back = (ImageView) findViewById(R.id.back);
        tick = (ImageView) findViewById(R.id.tick);
        group_image = (CircleImageView) findViewById(R.id.group_image);
        camera = (ImageView) findViewById(R.id.camera);
        group_name = (EditText) findViewById(R.id.group_name);
        recycle_horizontal = (RecyclerView) findViewById(R.id.recycle_horizontal);

        arrayList = new ArrayList<>();
        adapter = new NewGroupHorizontalAdapter(NewGroupActivity.this, arrayList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(NewGroupActivity.this, 3);
        recycle_horizontal.setLayoutManager(gridLayoutManager);
        recycle_horizontal.setAdapter(adapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        tick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (group_name.getText().toString().length() == 0) {
                    Util.showSnackBar(view, "Please enter group name", NewGroupActivity.this);
                } else if (picture_path.equals("")) {
                    Util.showSnackBar(view, "Please select image of group", NewGroupActivity.this);
                } else if (arrayList.size() == 0) {
                    Util.showSnackBar(view, "Please select atleast one friend", NewGroupActivity.this);
                } else {
                    selected_id = "";
                    for (int i = 0; i < arrayList.size(); i++) {
                        if (arrayList.get(i).getIs_select().equals("1"))
                            if (selected_id.equals(""))
                                selected_id = arrayList.get(i).getId();
                            else
                                selected_id = selected_id + "," + arrayList.get(i).getId();
                    }
                    if (selected_id.equals("")) {
                        Util.showSnackBar(view, "Please select atleast one friend", NewGroupActivity.this);
                    } else {
                        Log.e("create", "id " + selected_id);
                        createGroupApi();
                    }
                }

            }
        });


        if (ConnectivityReceiver.isConnected())
            getFriendListApi();
    }


    private void getFriendListApi() {

        final ProgressDialog mDialog = new ProgressDialog(NewGroupActivity.this);
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Loading..");
        mDialog.show();
        Log.e("auth", "token " + savePref.getString(AllInviteParameters.AUTH_TOKEN));
        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);
        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));
        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(NewGroupActivity.this, AllInviteApis.FRIEND_LIST, formBody, mDialog) {

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
                                if (!jsonObject1.getString("id").equals(savePref.getString(AllInviteParameters.USER_ID))) {
                                    friendListResponse.setId(jsonObject1.getString("id"));
                                    friendListResponse.setUsername(jsonObject1.getString("username"));
                                    friendListResponse.setGender(jsonObject1.getString("gender"));
                                    friendListResponse.setImage(jsonObject1.getString("image"));
                                    friendListResponse.setScore(jsonObject1.getString("score"));
                                    friendListResponse.setIs_select("0");
                                    arrayList.add(friendListResponse);
                                }
                            }

                            if (arrayList.size() == 0) {
                                openFriendListDialog();
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Util.showToast(NewGroupActivity.this, jsonObject.getString("message"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.checkAuthToken(NewGroupActivity.this, jsonObject.getString("message"), savePref);
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

    private void selectImage() {
        final CharSequence[] items = getResources().getStringArray(R.array.photo_array);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.add_photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    cameraIntent();
                } else if (item == 1) {
                    galleryIntent();
                } else if (item == 2) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file_upload = new File(Environment.getExternalStorageDirectory() + File.separator + "image.jpg");
        file_uri = Uri.fromFile(file_upload);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file_uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_FILE);
    }


    private void openFriendListDialog() {
        final Dialog dialog = new Dialog(NewGroupActivity.this, R.style.Theme_Dialog);
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
            }
        });
    }

    public void selectListMultiple(int pos, String is_select) {
        arrayList.get(pos).setIs_select(is_select);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap bitmap = null;
                try {
                    bitmap = Util.handleSamplingAndRotationBitmap(NewGroupActivity.this, file_uri);
                    group_image.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                picture_path = file_upload.getAbsolutePath();

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = Util.handleSamplingAndRotationBitmap(NewGroupActivity.this, selectedImageUri);
                    group_image.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                picture_path = Util.getAbsolutePath(NewGroupActivity.this, selectedImageUri);
            }
        }
    }

    private void createGroupApi() {
        MediaType mediaType = null;
        final ProgressDialog mDialog = new ProgressDialog(NewGroupActivity.this);
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Loading..");
        mDialog.show();

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);
        if (!picture_path.equalsIgnoreCase("")) {
            mediaType = picture_path.endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            File file = new File(picture_path);
            formBuilder.addFormDataPart(AllInviteParameters.IMAGE, file.getName(), RequestBody.create(mediaType, file));
        }

        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, savePref.getString(AllInviteParameters.AUTH_TOKEN));
        formBuilder.addFormDataPart(AllInviteParameters.IDS, selected_id);
        formBuilder.addFormDataPart(AllInviteParameters.NAME, group_name.getText().toString());

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllInviteApis.CREATE_GROUP, formBody, mDialog) {

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
                        Util.showToast(NewGroupActivity.this, "Group created successfully");
                        Intent intent = new Intent(NewGroupActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.checkAuthToken(NewGroupActivity.this, jsonObject.getString("message"), savePref);
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
}

