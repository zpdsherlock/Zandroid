package com.zpdsherlock.zapplib.manager.downloader.interfaces;


import android.support.v4.app.NotificationCompat;

/**
 * Created by zpd on 2016/12/11.
 * This is listener of Notification.
 */

public interface NotificationListener {
    void preTaskNotifier(NotificationCompat.Builder builder);
    void updateTaskNotifier(NotificationCompat.Builder builder, String filename, long progress, long total);
    void finishTaskNotifier(NotificationCompat.Builder builder, boolean status);
}
