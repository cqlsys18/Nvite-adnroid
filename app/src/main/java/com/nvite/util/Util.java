package com.nvite.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nvite.R;
import com.nvite.activity.Loginactivity;
import com.wdullaer.materialdatetimepicker.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.RequestBody;
import okio.Buffer;

/**
 * Created by V on 7/26/2017.
 */

public class Util {
    public static String API_KEY = "AIzaSyBsbJHW8QZRTQiT2TZR6g8DnEUMeoiOF_I";
    public static final String NOTIFICATION_MESSAGE = "com.nvite.android.nvite.Message_Handle_Brodcast_Reciever.DISPLAY_MESSAGE";
    public static final String NOTIFICATION_RECEIVE = "com.nvite.android.nvite.Message_Handle_Brodcast_Reciever.DISPLAY_NOTIFICATION";

    public static boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static void showSnackBar(View mParentview, String message, Context context) {
        Snackbar snackbar = Snackbar.make(mParentview, message, Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) snackbar.getView();

        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(3);
        group.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.show();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void hideKeyboard(Activity context) {
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String convertTimeStampDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setTimeZone(tz);
        Date currenTimeZone = new Date(timestamp * 1000);
        return sdf.format(currenTimeZone);
    }

    public static String convertTimeStampDateName(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        sdf.setTimeZone(tz);
        Date currenTimeZone = new Date(timestamp * 1000);
        return sdf.format(currenTimeZone);
    }

    public static String convertTimeStampTime(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(tz);
        Date currenTimeZone = new Date(timestamp * 1000);
        return sdf.format(currenTimeZone);
    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 900;
        int MAX_WIDTH = 900;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Rect rect = new Rect(-1, -1, -1, -1);
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, rect, options);

        img = rotateImageIfRequired(img, selectedImage, context);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }


    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage, Context context) throws
            IOException {

        ExifInterface ei = new ExifInterface(getAbsolutePath(context, selectedImage));
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static String getAbsolutePath(Context activity, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = activity.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static long getTimestamp(String str_date) {
        long time_stamp = 0;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date date = (Date) formatter.parse(str_date);
            time_stamp = date.getTime();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        time_stamp = time_stamp / 1000;
        return time_stamp;
    }

    public static long getTimestampFromTime(String str_date) {
        long time_stamp = 0;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            Date date = (Date) formatter.parse(str_date);
            time_stamp = date.getTime();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        time_stamp = time_stamp / 1000;
        return time_stamp;
    }

    public static ProgressDialog initializeProgress(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(context.getResources().getString(R.string.loading));
        return progressDialog;
    }

    public static String bodyToString(final RequestBody request) {
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            copy.writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    public static void checkAuthToken(Activity context, String message, SavePref savePref) {

        if (message.equals("USER NOT EXIST")) {
            Util.showToast(context, "Auth token does not match");
            savePref.clearPreferences();
            Intent in = new Intent(context, Loginactivity.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(in);
            context.finish();
        } else {
            Util.showToast(context, message);
        }
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }

    public static void viewImage(Context activity, String url) {
        final Dialog dialog = new Dialog(activity, android.R.style.Theme_Holo_NoActionBar_Fullscreen);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.setContentView(R.layout.activity_picture_details);
        dialog.show();
        ImageView icn_back = (ImageView) dialog.findViewById(R.id.icn_cross);
        ImageView image = (ImageView) dialog.findViewById(R.id.image);
        RelativeLayout layout = (RelativeLayout) dialog.findViewById(R.id.activity_picture_details);

        Glide.with(activity).load(url).placeholder(R.drawable.camera_placeholder).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(image);

        icn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

}
