package com.dts.mposupd;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;
import android.util.Log;

public class APKInstallService extends Service {

    private String TAG="APKInstallService";

    public APKInstallService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999);
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                Log.d(TAG, "Requesting user confirmation for installation");
                Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(confirmationIntent);
                } catch (Exception e) {
                }
                break;
            case PackageInstaller.STATUS_SUCCESS:
                Log.d(TAG, "Installation succeed");
                break;
            default:
                Log.d(TAG, "Installation failed");
                break;
        }
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
