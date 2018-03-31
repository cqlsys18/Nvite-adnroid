package com.nvite.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by cqlsys on 1/15/2016.
 */
public class SavePref {
    Context context;
    public static final String PREF_TOKEN = "NviteApp";
    SharedPreferences preferences;
    SharedPreferences preferences_seller;
    SharedPreferences.Editor editor;


    public SavePref(Context c) {
        context = c;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
    }


    public void setString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key) {
        String value = preferences.getString(key, "");
        return value;
    }

    public void setInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public int getInt(String key) {
        int value = preferences.getInt(key, 0);
        return value;
    }

    public static void setDeviceToken(Context mContext, String key, String value) {
        SharedPreferences sharedpreferences = mContext.getSharedPreferences(PREF_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getDeviceToken(Context mContext, String key, String def_value) {
        SharedPreferences preferences = mContext.getSharedPreferences(PREF_TOKEN, Context.MODE_PRIVATE);
        String stringvalue = preferences.getString(key, def_value);
        return stringvalue;
    }


    public void clearPreferences() {
        preferences.edit().clear().commit();
    }
}
