package com.project.poupay.sql;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;

public class Preferences {
    private static final String prefsId = "com.project.poupay";

    public static final String REMIND_LOGIN_ENABLED = "REMIND_LOGIN_ENABLED";
    public static final String REMIND_LOGIN_USERNAME = "REMIND_LOGIN_USERNAME";
    public static final String REMIND_LOGIN_PASSWORD = "REMIND_LOGIN_PASSWORD";

    public static void set(String id, boolean value, Context context) {
        context.getSharedPreferences(prefsId, MODE_PRIVATE)
                .edit()
                .putBoolean(id, value)
                .apply();
    }

    public static void set(String id, String value, Context context) {
        context.getSharedPreferences(prefsId, MODE_PRIVATE)
                .edit()
                .putString(id, value)
                .apply();
    }

    public static void set(String id, int value, Context context) {
        context.getSharedPreferences(prefsId, MODE_PRIVATE)
                .edit()
                .putInt(id, value)
                .apply();
    }

    public static int getInt(String id, int defValue, Context context) {
        return context.getSharedPreferences(prefsId, MODE_PRIVATE)
                .getInt(id, defValue);
    }


    public static boolean getBool(String id, boolean defValue, Context context) {
        return context.getSharedPreferences(prefsId, MODE_PRIVATE)
                .getBoolean(id, defValue);
    }

    public static String getString(String id, String defValue, Context context) {
        return context.getSharedPreferences(prefsId, MODE_PRIVATE)
                .getString(id, defValue);
    }

}
