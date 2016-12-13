package com.zpdsherlock.zapplib.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.zpdsherlock.zapplib.util.MsgInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zpd on 2016/12/9.
 * This the Activity of Zapp.
 * Support the Android 6.0
 */

public class ZActivity extends AppCompatActivity implements Runnable {
    private static final int ALL_PERMISSION = 0x1;
    private static final int WRITING_SETTINGS_REQUEST_CODE = 0x2;
    private Handler mHandler = new Handler();
    private Bundle mSavedInstanceState;

    /**
     * Request the user permission: ACCESS_COARSE_LOCATION
     *
     * @return true for allowing, or false for denying
     */
    protected boolean request_access_coarse_location() {
        return false;
    }

    /**
     * Request the user permission: ACCESS_FINE_LOCATION
     *
     * @return true for allowing, or false for denying
     */
    protected boolean request_access_fine_location() {
        return false;
    }

    /**
     * Request the user permission: WRITE_EXTERNAL_STORAGE
     *
     * @return true for allowing, or false for denying
     */
    protected boolean request_write_external_storage() {
        return false;
    }

    /**
     * Request the user permission: READ_EXTERNAL_STORAGE
     *
     * @return true for allowing, or false for denying
     */
    protected boolean request_read_external_storage() {
        return false;
    }

    /**
     * Request the user permission: READ_PHONE_STATE
     *
     * @return true for allowing, or false for denying
     */
    protected boolean request_read_phone_state() {
        return false;
    }

    /**
     * Request the user permission: WRITE_SETTINGS
     *
     * @return true for allowing, or false for denying
     */
    protected boolean request_write_settings() {
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(final List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void request_permissions(List<String> permissions, List<String> needed, String permission) {
        boolean valid = false;
        if (Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
            valid = request_access_coarse_location() && !addPermission(permissions, permission);
        } else if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
            valid = request_access_fine_location() && !addPermission(permissions, permission);
        } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
            valid = request_write_external_storage() && !addPermission(permissions, permission);
        } else if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permission)) {
            valid = request_read_external_storage() && !addPermission(permissions, permission);
        } else if (Manifest.permission.READ_PHONE_STATE.equals(permission)) {
            valid = request_read_phone_state() && !addPermission(permissions, permission);
        }
        if (valid) needed.add(permission);
    }

    @Override
    public void run() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsNeeded = new ArrayList<>();
            final List<String> permissionsList = new ArrayList<>();
            request_permissions(permissionsList, permissionsNeeded,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            request_permissions(permissionsList, permissionsNeeded,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            request_permissions(permissionsList, permissionsNeeded,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            request_permissions(permissionsList, permissionsNeeded,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            request_permissions(permissionsList, permissionsNeeded,
                    Manifest.permission.READ_PHONE_STATE);
            if (!permissionsList.isEmpty()) {
                if (!permissionsNeeded.isEmpty()) {
                    String message = "检测到部分权限被阻止，请确保以下权限开启:\n" + permissionsNeeded.get(0);
                    for (int i = 1; i < permissionsNeeded.size(); i++)
                        message = message + ", " + permissionsNeeded.get(i);
                    message = message + "\n点击确定授予权限";
                    MsgInfo.showMessageOKCancel(this, message,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(ZActivity.this,
                                            permissionsList.toArray(new String[permissionsList.size()]),
                                            ALL_PERMISSION);
                                }
                            });
                } else ActivityCompat.requestPermissions(this,
                        permissionsList.toArray(new String[permissionsList.size()]),
                        ALL_PERMISSION);
            } else onZCreate(mSavedInstanceState);
        } else onZCreate(mSavedInstanceState);
    }

    @Deprecated
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == ALL_PERMISSION && permissions.length > 0) {
                int i;
                for (i = 0; i < permissions.length; ++i) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "检测到您阻止了相关权限，请重新开启", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                if (i == permissions.length) {
                    if (request_write_settings() && !Settings.System.canWrite(this)) {
                        startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:" + getPackageName())), WRITING_SETTINGS_REQUEST_CODE);
                    } else onZCreate(mSavedInstanceState);
                } else mHandler.postDelayed(ZActivity.this, 500);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Deprecated
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == WRITING_SETTINGS_REQUEST_CODE) {
                if (Settings.System.canWrite(this)) onZCreate(mSavedInstanceState);
                else
                    MsgInfo.showMessageOKCancel(this, "请您允许配置写权限",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                                Uri.parse("package:" + getPackageName())), WRITING_SETTINGS_REQUEST_CODE);
                                    }
                                }
                            });
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void onZRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onZActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

    @Deprecated
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
        mHandler.post(this);
    }

    protected void onZCreate(Bundle savedInstanceState) {
    }
}
