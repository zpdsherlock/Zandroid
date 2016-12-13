package com.zpdsherlock.zapplib.manager.downloader.interfaces;

/**
 * Created by zpd on 2016/12/8.
 * This is the definition of task's listener of ZDownloader's task.
 */

public interface UserTaskControlListener {
    void successTask(String filepath);
    void failTask();
    void userOpenFile(String filename);
}
