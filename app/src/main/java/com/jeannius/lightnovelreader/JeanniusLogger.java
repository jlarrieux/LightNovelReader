package com.jeannius.lightnovelreader;

import android.util.Log;

public class JeanniusLogger {
    private static String PREFIX_TAG = "Jeannius";

    public static void log(String message){
        Log.i(PREFIX_TAG, message);
    }

    public static void log(StringBuffer message) {
        Log.i(PREFIX_TAG, message.toString());
    }
    public static void log(String tag, String message){
        Log.i(PREFIX_TAG + "-" +tag, message);
    }
}
