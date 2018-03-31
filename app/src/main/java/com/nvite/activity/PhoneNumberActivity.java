package com.nvite.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;
import com.nvite.R;
import com.nvite.parser.AllInviteApis;
import com.nvite.parser.AllInviteParameters;
import com.nvite.parser.GetAsync;
import com.nvite.util.ConnectivityReceiver;
import com.nvite.util.SavePref;
import com.nvite.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PhoneNumberActivity extends BaseActivity implements View.OnClickListener {
    private ImageView back;
    private CountryCodePicker codePicker;
    private EditText mobile;
    private TextView submit;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);
        initialize();

        setDefaultCountryCode();
    }

    private void initialize() {
        context = PhoneNumberActivity.this;
        back = (ImageView) findViewById(R.id.back);
        codePicker = (CountryCodePicker) findViewById(R.id.codePicker);
        mobile = (EditText) findViewById(R.id.mobile);
        submit = (TextView) findViewById(R.id.submit);

        back.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.submit:
                if (ConnectivityReceiver.isConnected()) {
                    checkValidation(view);
                } else {
                    Util.showSnackBar(view, getResources().getString(R.string.internet_error), context);
                }
                break;
        }
    }

    private void setDefaultCountryCode() {
        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String CountryID = manager.getSimCountryIso().toUpperCase();
        codePicker.setDefaultCountryUsingNameCode(CountryID);
        codePicker.resetToDefaultCountry();
    }

    private void checkValidation(View view) {
        if (mobile.getText().toString().length() == 0) {
            Util.showSnackBar(view, "Please enter mobile number", context);
        } else {
            if (getIntent().getBooleanExtra("from_social_login", false)) {
                verifyPhoneApi();
            } else {
                SignupApi();
            }

        }
    }

    private void SignupApi() {
        String gender_type = "";
        MediaType mediaType = null;
        /*if (getIntent().getStringExtra("gender").toLowerCase().equals("male")) {
            gender_type = "0";
        } else if (getIntent().getStringExtra("gender").toLowerCase().equals("female")) {
            gender_type = "1";
        } else if (getIntent().getStringExtra("gender").toLowerCase().equals("other")) {
            gender_type = "2";
        }*/

        final ProgressDialog mDialog = new ProgressDialog(context);
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Loading..");
        mDialog.show();

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);
        if (!getIntent().getStringExtra("picture_path").equalsIgnoreCase("")) {
            mediaType = getIntent().getStringExtra("picture_path").endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            File file = new File(getIntent().getStringExtra("picture_path"));
            formBuilder.addFormDataPart(AllInviteParameters.IMAGE, file.getName(), RequestBody.create(mediaType, file));
        }

        formBuilder.addFormDataPart(AllInviteParameters.USERNAME, getIntent().getStringExtra("username"));
        formBuilder.addFormDataPart(AllInviteParameters.EMAIL, getIntent().getStringExtra("email"));
        formBuilder.addFormDataPart(AllInviteParameters.BIRTHDAY, String.valueOf(Util.getTimestamp(getIntent().getStringExtra("dob"))));
        formBuilder.addFormDataPart(AllInviteParameters.PASSWORD, getIntent().getStringExtra("password"));
        formBuilder.addFormDataPart(AllInviteParameters.GENDER, getIntent().getStringExtra("gender"));
        formBuilder.addFormDataPart(AllInviteParameters.DEVICE_TYPE, "2");
        formBuilder.addFormDataPart(AllInviteParameters.DEVICE_TOKEN, SavePref.getDeviceToken(context, "token", ""));
        formBuilder.addFormDataPart(AllInviteParameters.PHONE_NUMBER_ONLY, mobile.getText().toString());
        formBuilder.addFormDataPart(AllInviteParameters.PHONE_CODE, codePicker.getSelectedCountryCode());

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllInviteApis.SIGNUP_URL, formBody, mDialog) {

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
                        JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
                        Util.showToast(context, jsonObject.getString("message"));
                        Intent intent = new Intent(context, OtpActivity.class);
                        intent.putExtra("auth_token", jsonObjectBody.getString("auth_token"));
                        intent.putExtra("phone_no", mobile.getText().toString());
                        intent.putExtra("phone_code", codePicker.getSelectedCountryCode());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.showToast(context, jsonObject.getString("message"));
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


    private void verifyPhoneApi() {

        final ProgressDialog mDialog = new ProgressDialog(context);
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Loading..");
        mDialog.show();

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);

        formBuilder.addFormDataPart(AllInviteParameters.AUTH_TOKEN, getIntent().getStringExtra("auth_token"));
        formBuilder.addFormDataPart(AllInviteParameters.PHONE_NUMBER_ONLY, mobile.getText().toString());
        formBuilder.addFormDataPart(AllInviteParameters.PHONE_CODE, codePicker.getSelectedCountryCode());

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(this, AllInviteApis.PHONE_VERIFY, formBody, mDialog) {

            @Override
            public void getValueParse(Response response) {
                mDialog.dismiss();
                String result = "";
                try {
                    result = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("phone", "result " + result);
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
                        JSONObject jsonObjectUser = jsonObjectBody.getJSONObject("User");
                        Util.showToast(context, jsonObject.getString("message"));
                        Intent intent = new Intent(context, OtpActivity.class);
                        intent.putExtra("auth_token", jsonObjectUser.getString("auth_token"));
                        intent.putExtra("phone_no", mobile.getText().toString());
                        intent.putExtra("phone_code", codePicker.getSelectedCountryCode());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Util.showToast(context, jsonObject.getString("message"));
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
