package com.zpdsherlock.zapplib.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.text.DecimalFormat;

public class MsgInfo {
    public static void showMessageOKCancel(Context context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    public static String getDataSize(long size) {
        StringBuilder builder = new StringBuilder();
        if (size < 0) size = 0;
        DecimalFormat formater = new DecimalFormat("####.00");
        if (size < 1024L) {
            builder.append(size).append("bytes");
        } else if (size < 1048576L) {
            float kbsize = size / 1024f;
            builder.append(formater.format(kbsize)).append("KB");
        } else if (size < 1073741824L) {
            float mbsize = size / 1048576f;
            builder.append(formater.format(mbsize)).append("MB");
        } else if (size < 1099511627776L) {
            float gbsize = size / 1073741824f;
            builder.append(formater.format(gbsize)).append("GB");
        }
        return builder.toString();
    }
}
