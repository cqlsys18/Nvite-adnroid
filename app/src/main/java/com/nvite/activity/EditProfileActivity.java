package com.nvite.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nvite.R;
import com.nvite.parser.AllInviteApis;
import com.nvite.parser.AllInviteParameters;
import com.nvite.parser.GetAsync;
import com.nvite.util.SavePref;
import com.nvite.util.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {
    CircleImageView image;
    EditText email;
    TextView dob, update;
    ImageView back;
    SavePref savePref;
    private static int SELECT_FILE = 200;
    private static int REQUEST_CAMERA = 201;
    private Uri file_uri;
    String picture_path = "";
    File file_upload = null;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        context = EditProfileActivity.this;
        image = (CircleImageView) findViewById(R.id.image);
        email = (EditText) findViewById(R.id.email);
        back = (ImageView) findViewById(R.id.back);
        dob = (TextView) findViewById(R.id.dob);
        update = (TextView) findViewById(R.id.update);
        savePref = new SavePref(EditProfileActivity.this);
        setData();

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePicker();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.getText().toString().length() == 0) {
                    Util.showSnackBar(view, "Please enter email address", EditProfileActivity.this);
                } else if (!Util.isValidEmail(email.getText().toString())) {
                    Util.showSnackBar(view, "Please enter valid email address", EditProfileActivity.this);
                } else {
                    updatProfileApi();
                }
            }
        });

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String month = String.valueOf(monthOfYear + 1);
        String day = String.valueOf(dayOfMonth);
        if (month.length() == 1)
            month = "0" + month;
        if (day.length() == 1)
            day = "0" + day;

        dob.setText(day + "-" + month + "-" + String.valueOf(year));
    }

    private void setData() {
        if (savePref.getString(AllInviteParameters.GENDER).equals("1"))
            Glide.with(EditProfileActivity.this).load(savePref.getString(AllInviteParameters.IMAGE)).placeholder(R.drawable.male).override(200, 200).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(image);
        else
            Glide.with(EditProfileActivity.this).load(savePref.getString(AllInviteParameters.IMAGE)).placeholder(R.drawable.female).override(200, 200).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(image);

        email.setText(savePref.getString(AllInviteParameters.EMAIL));
        dob.setText(Util.convertTimeStampDate(Long.parseLong(savePref.getString(AllInviteParameters.BIRTHDAY))));
        email.setSelection(email.getText().toString().length());
    }

    private void openDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(EditProfileActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setThemeDark(false);
        dpd.vibrate(true);
        dpd.setMaxDate(now);
        dpd.dismissOnPause(true);
        dpd.showYearPickerFirst(false);
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap bitmap = null;
                try {
                    bitmap = Util.handleSamplingAndRotationBitmap(context, file_uri);
                    image.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                picture_path = file_upload.getAbsolutePath();

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = Util.handleSamplingAndRotationBitmap(context, selectedImageUri);
                    image.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                picture_path = Util.getAbsolutePath(context, selectedImageUri);
            }


        }

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

    private void updatProfileApi() {

        MediaType mediaType = null;

        final ProgressDialog mDialog = new ProgressDialog(context);
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
        formBuilder.addFormDataPart(AllInviteParameters.EMAIL, email.getText().toString());
        formBuilder.addFormDataPart(AllInviteParameters.BIRTHDAY, String.valueOf(Util.getTimestamp(dob.getText().toString())));

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllInviteApis.UPDATE_PROFILE, formBody, mDialog) {

            @Override
            public void getValueParse(Response response) {
                mDialog.dismiss();
                String result = "";
                try {
                    result = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("edit", "result " + result);
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.getString("code").equals("200")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            savePref.setString(AllInviteParameters.EMAIL, jsonObject1.getString("email"));
                            savePref.setString(AllInviteParameters.BIRTHDAY, jsonObject1.getString("birthday"));
                            savePref.setString(AllInviteParameters.IMAGE, jsonObject1.getString("image"));

                            Util.showToast(context, jsonObject.getString("message"));
                            Intent returnIntent = new Intent();
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else {
                            Util.showToast(context, jsonObject.getString("message"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.checkAuthToken(EditProfileActivity.this, jsonObject.getString("message"), savePref);
//                        Util.showToast(context, jsonObject.getString("message"));
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
