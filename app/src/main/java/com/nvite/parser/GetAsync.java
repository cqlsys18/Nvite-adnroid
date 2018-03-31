package com.nvite.parser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;

import com.nvite.R;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by user on 1/13/2017.
 */

public abstract class GetAsync extends AsyncTask<Response, Void, Response> {

    String url = "";
    RequestBody requestBody;
    Context context;
    WeakReference<Context> contextWeakReference;
    AlertDialog.Builder builder;
    ProgressDialog progressDialog;

    public GetAsync(Context context, String url, RequestBody requestBody, ProgressDialog progressDialog) {
        this.url = url;
        this.requestBody = requestBody;
        contextWeakReference = new WeakReference<Context>(context);
        this.context = contextWeakReference.get();
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Response doInBackground(Response... params) {
        Response jsonData = null;
        try {
            try {
                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(50, TimeUnit.SECONDS).writeTimeout(50, TimeUnit.SECONDS)
                        .readTimeout(50, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build();

                Request request = new Request.Builder()
                        .post(requestBody)
                        .url(url)
                        .build();
                try {
                    Response responses = client.newCall(request).execute();
                    jsonData = responses;
//                    jsonData = responses.body().string();
                } catch (SocketTimeoutException ex) {
                    dismissDialog();
                    showDialog();
                } catch (ConnectException ex) {
                    dismissDialog();
                    showDialog();
                } catch (Exception ex) {
                    dismissDialog();
                    ex.printStackTrace();
                }

            } catch (Exception ex) {
                dismissDialog();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            dismissDialog();
            e.printStackTrace();
        }
        return jsonData;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Response result) {
        super.onPostExecute(result);

        if (result != null) {
            getValueParse(result);
        } else {

        }
    }

    public abstract void getValueParse(Response listValue);

    public abstract void retry();

    private void dismissDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (final IllegalArgumentException e) {
            // Handle or log or ignore
        } catch (final Exception e) {
            // Handle or log or ignore
        }
    }

    private void showDialog() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        builder.setTitle(context.getResources().getString(R.string.warning));
        builder.setMessage(context.getResources().getString(R.string.internet_error));
        builder.setCancelable(true);

        builder.setPositiveButton(
                context.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        retry();
                    }
                });

        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                if (context != null) {
                    AlertDialog alert11 = builder.create();
                    alert11.show();
                }
            }
        });

    }
}