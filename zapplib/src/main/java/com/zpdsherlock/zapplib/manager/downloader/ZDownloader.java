package com.zpdsherlock.zapplib.manager.downloader;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.zpdsherlock.zapplib.R;
import com.zpdsherlock.zapplib.manager.downloader.interfaces.NotificationListener;
import com.zpdsherlock.zapplib.manager.downloader.interfaces.UserTaskControlListener;
import com.zpdsherlock.zapplib.manager.downloader.interfaces.ZDownloadNotificationBuilder;
import com.zpdsherlock.zapplib.util.MsgInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by zpd on 2016/12/8.
 * This is the Factory of ZDownloader. It is in single instance mode.
 */

public class ZDownloader {
    private enum Proto {
        HTTP, HTTPS
    }

    private static ZDownloader instance;
    private LinkedHashMap<Integer, ZDownloadTask> tasks;
    private int maxLoopIds;
    private int taskLoopId;

    private ZDownloader() {
        tasks = new LinkedHashMap<>();
        maxLoopIds = Integer.MAX_VALUE;
    }

    public static ZDownloader getInstance() {
        synchronized (ZDownloader.class) {
            if (instance == null) instance = new ZDownloader();
        }
        return instance;
    }

    public static boolean destroyTask(int key) {
        synchronized (ZDownloader.class) {
            if (instance != null) return instance.removeTask(key);
        }
        return false;
    }

    public void downloadFromHTTPInBackground(final Context context,
                                             int notificationId, boolean notification, String from, String to,
                                             NotificationListener listener,
                                             boolean userChooseOpener) {
        final int id = taskLoopId++;
        taskLoopId %= maxLoopIds;
        final ZDownloadTask task = new ZDownloadTask(context, notificationId, to);
        tasks.put(id, task.setProto(Proto.HTTP)
                .showNofication(notification).setNotificationListener(listener)
                .setUserChooseOpener(userChooseOpener));
        task.setSystemTaskCancelListener(new SystemTaskControlListener() {
            @Override
            public void finish() {
                tasks.remove(id);
            }
        }).execute(from, to);
    }

    public int downloadFromHTTPInBackground(final Context context,
                                            int notificationId,
                                            boolean notification, String from, String to,
                                            NotificationListener listener, UserTaskControlListener callback,
                                            boolean userChooseOpener) {
        final int id = taskLoopId++;
        taskLoopId %= maxLoopIds;
        final ZDownloadTask task = new ZDownloadTask(context, notificationId, to);
        tasks.put(id, task.setProto(Proto.HTTP)
                .showNofication(notification).setNotificationListener(listener)
                .UserTaskControlListener(callback).setUserChooseOpener(userChooseOpener));
        task.setSystemTaskCancelListener(new SystemTaskControlListener() {
            @Override
            public void finish() {
                tasks.remove(id);
            }
        }).execute(from, to);
        return id;
    }

    public int downloadFromHTTPSInBackground(final Context context,
                                             int notificationId,
                                             boolean notification, String from, String to,
                                             NotificationListener listener,
                                             boolean userChooseOpener) {
        final int id = taskLoopId++;
        taskLoopId %= maxLoopIds;
        final ZDownloadTask task = new ZDownloadTask(context, notificationId, to);
        tasks.put(id, task.setProto(Proto.HTTPS)
                .showNofication(notification).setNotificationListener(listener)
                .setUserChooseOpener(userChooseOpener));
        task.setSystemTaskCancelListener(new SystemTaskControlListener() {
            @Override
            public void finish() {
                tasks.remove(id);
            }
        }).execute(from, to);
        return id;
    }

    public int downloadFromHTTPSInBackground(final Context context,
                                             int notificationId,
                                             boolean notification, String from, String to,
                                             NotificationListener listener, UserTaskControlListener callback,
                                             boolean userChooseOpener) {
        final int id = taskLoopId++;
        taskLoopId %= maxLoopIds;
        final ZDownloadTask task = new ZDownloadTask(context, notificationId, to);
        tasks.put(id, task.setProto(Proto.HTTPS)
                .showNofication(notification).setNotificationListener(listener)
                .UserTaskControlListener(callback)
                .setUserChooseOpener(userChooseOpener));
        task.setSystemTaskCancelListener(new SystemTaskControlListener() {
            @Override
            public void finish() {
                tasks.remove(id);
            }
        }).execute(from, to);
        return id;
    }

    private boolean removeTask(int key) {
        if (tasks.containsKey(key)) {
            ZDownloadTask task = tasks.get(key);
            if (task.getStatus() != ZDownloadTask.Status.FINISHED) {
                task.cancel(true);
                task.clearNotification();
            }
            tasks.remove(key);
            return true;
        }
        return false;
    }

    private class ZDownloadTask extends AsyncTask<String, Bundle, String> implements ZDownloadNotificationBuilder {
        private static final String TARGET_NAME = "target";
        private static final String UPDATE_KEY_PROGRESS = "progress";
        private static final String UPDATE_KEY_TOTAL = "total";
        private static final String UPDATE_KEY_SPEED = "speed";
        private static final int TIME_OUT = 100000;

        private Context context;
        private SystemTaskControlListener systemTaskCancelListener;
        private UserTaskControlListener userTaskCancelListener;
        private NotificationListener mNotificationListener;
        private Proto mode = Proto.HTTP;
        private int maxbuffer = 4 * 1024;
        private final String to;
        private final int mNofiticationId;
        private NotificationCompat.Builder mBuilder;
        private ZDownloadNotificationBuilder customBuilder;
        private boolean showNotifyFlag;
        private boolean userChooseOpener;

        @Override
        public NotificationCompat.Builder customBuilder() {
            return new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher).setOngoing(true);
        }

        ZDownloadTask(Context context, int notificationId, String to) {
            this.context = context;
            this.mNofiticationId = notificationId;
            this.to = to;
        }

        ZDownloadTask setSystemTaskCancelListener(SystemTaskControlListener systemTaskCancelListener) {
            this.systemTaskCancelListener = systemTaskCancelListener;
            return this;
        }

        ZDownloadTask UserTaskControlListener(UserTaskControlListener userTaskControlListener) {
            this.userTaskCancelListener = userTaskControlListener;
            return this;
        }

        ZDownloadTask setNotificationListener(NotificationListener listener) {
            this.mNotificationListener = listener;
            return this;
        }

        ZDownloadTask setProto(Proto mode) {
            this.mode = mode;
            return this;
        }

        public void customNotification(ZDownloadNotificationBuilder builder) {
            this.customBuilder = builder;
        }

        public ZDownloadTask setMaxbuffer(int maxbuffer) {
            this.maxbuffer = maxbuffer;
            return this;
        }

        ZDownloadTask showNofication(boolean show) {
            this.showNotifyFlag = show;
            return this;
        }

        ZDownloadTask setUserChooseOpener(boolean enable) {
            this.userChooseOpener = enable;
            return this;
        }

        @Override
        protected String doInBackground(String... strings) {
            if (strings == null || strings.length != 2) return null;
            String from = strings[0];
            if (mode == Proto.HTTP)
                return connectByHttp(from);
            else return connectByHttps(from);
        }

        private String connectByHttp(String from) {
            boolean success = false;
            HttpURLConnection con = null;
            try {
                URL url = new URL(from);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(TIME_OUT);
                con.setReadTimeout(TIME_OUT);
                long total;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) total = con.getContentLength();
                else total = con.getContentLengthLong();
                if (con.getResponseCode() == 200) success = download(total, con.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (con != null) con.disconnect();
            }
            if (success) return to;
            else return null;
        }

        private String connectByHttps(String from) {
            boolean success = false;
            HttpsURLConnection con = null;
            try {
                URL url = new URL(from);
                con = (HttpsURLConnection) url.openConnection();
                con.setConnectTimeout(TIME_OUT);
                con.setReadTimeout(TIME_OUT);
                long total;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) total = con.getContentLength();
                else total = con.getContentLengthLong();
                if (con.getResponseCode() == 200) success = download(total, con.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (con != null) con.disconnect();
            }
            if (success) return to;
            else return null;
        }

        private boolean download(long total, InputStream in) {
            boolean success = false;
            BufferedInputStream bin = null;
            BufferedOutputStream bout = null;
            try {
                bin = new BufferedInputStream(in);
                File toFile = new File(to), dir = new File(toFile.getParent());
                if (dir.exists() || dir.mkdir()) {
                    if (toFile.exists() || toFile.createNewFile()) {
                        bout = new BufferedOutputStream(new FileOutputStream(to));
                        byte[] data = new byte[maxbuffer + 1];
                        int len;
                        long progressSize = 0, last_progressSize = 0;
                        long last = System.currentTimeMillis(), now;
                        Bundle bundle = new Bundle();
                        bundle.putString(TARGET_NAME, toFile.getName());
                        bundle.putLong(UPDATE_KEY_PROGRESS, progressSize);
                        bundle.putLong(UPDATE_KEY_TOTAL, total);
                        publishProgress(bundle);
                        while (!isCancelled() && (len = bin.read(data, 0, maxbuffer)) != -1) {
                            bout.write(data, 0, len);
                            progressSize += len;
                            now = System.currentTimeMillis();
                            if (now - last > 500) {
                                bundle.putLong(UPDATE_KEY_PROGRESS, progressSize);
                                bundle.putLong(UPDATE_KEY_SPEED, 2 * (progressSize - last_progressSize));
                                publishProgress(bundle);
                                last = now;
                                last_progressSize = progressSize;
                            }
                        }
                        success = true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bin != null) try {
                    bin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bout != null) try {
                    bout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return success;
        }

        void clearNotification() {
            if (showNotifyFlag && mBuilder != null) {
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(mNofiticationId);
                mBuilder = null;
                customBuilder = null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (showNotifyFlag) {
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (customBuilder == null) {
                    mBuilder = customBuilder();
                    mBuilder.setContentTitle(context.getResources().getString(R.string.download_start));
                } else {
                    mBuilder = customBuilder.customBuilder();
                    if (mNotificationListener != null)
                        mNotificationListener.preTaskNotifier(customBuilder == null ? null : mBuilder);
                }
                mNotificationManager.notify(mNofiticationId, mBuilder.build());
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (showNotifyFlag) {
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (customBuilder == null) {
                    mBuilder.setProgress(0, 0, false);
                    if (s != null)
                        mBuilder.setContentText(context.getResources().getString(R.string.download_complete));
                    else
                        mBuilder.setContentText(context.getResources().getString(R.string.download_fail));
                    mBuilder.setOngoing(false);
                }
                if (mNotificationListener != null) {
                    if (s != null)
                        mNotificationListener.finishTaskNotifier(customBuilder == null ? null : mBuilder, true);
                    else
                        mNotificationListener.finishTaskNotifier(customBuilder == null ? null : mBuilder, false);
                }
                mNotificationManager.notify(mNofiticationId, mBuilder.build());
            }
            if (s == null) {
                if (userTaskCancelListener != null) userTaskCancelListener.failTask();
                File df = new File(to);
                if (df.exists() && df.delete()) ;
            } else {
                if (userTaskCancelListener != null) userTaskCancelListener.successTask(to);
                if (userChooseOpener && userTaskCancelListener != null)
                    userTaskCancelListener.userOpenFile(to);
            }
            if (systemTaskCancelListener != null) systemTaskCancelListener.finish();
        }

        @Override
        protected void onProgressUpdate(Bundle... values) {
            super.onProgressUpdate(values);
            if (values == null) return;
            Bundle data = values[0];
            if (!data.containsKey(UPDATE_KEY_PROGRESS) || !data.containsKey(UPDATE_KEY_TOTAL))
                return;
            if (showNotifyFlag) {
                long progress = data.getLong(UPDATE_KEY_PROGRESS), total = data.getLong(UPDATE_KEY_TOTAL);
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                int percent = (int) (100 * ((float) progress / (float) total));
                StringBuilder content = new StringBuilder().append(percent).append("%");
                if (data.containsKey(UPDATE_KEY_SPEED))
                    content.append("\t\t").append(MsgInfo.getDataSize(data.getLong(UPDATE_KEY_SPEED))).append("/s");
                content.append("\t\t").append(MsgInfo.getDataSize(total));
                if (customBuilder != null) {
                    if (mNotificationListener != null)
                        mNotificationListener.updateTaskNotifier(mBuilder, data.getString(TARGET_NAME), progress, total);
                } else {
                    mBuilder.setContentTitle(data.getString(TARGET_NAME));
                    mBuilder.setProgress(100, percent, false);
                    mBuilder.setContentText(content);
                }
                mNotificationManager.notify(mNofiticationId, mBuilder.build());
            }
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            clearNotification();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            clearNotification();
        }
    }

    private interface SystemTaskControlListener {
        void finish();
    }
}
