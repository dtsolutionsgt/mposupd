package com.dts.mposupd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.StrictMode;
import android.os.UserManager;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.se.omapi.Session;
import android.view.Gravity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;


import android.app.admin.DeviceAdminReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL;

public class MainActivity extends AppCompatActivity {

    private TextView lblTitle;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private PackageInstaller packageInstaller;

    private Uri localfile,fileUri;
    private String fname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lblTitle = findViewById(R.id.textView);lblTitle.setText("");

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Handler mtimer = new Handler();
        Runnable mrunner=new Runnable() {
            @Override
            public void run() {
                grantPermissions();
            }
        };
        mtimer.postDelayed(mrunner,50);
    }

    //region Main

    private void startApplication() {
        try {
            //fname=getIntent().getExtras().getString("filename");
            fname="mpos.apk";
        } catch (Exception e) {
            //toast("DTS Update\nNo está definido archivo de actualización");finish();
            fname="mpos.apk";
        }

        lblTitle.setText("Descargando "+fname+"  . . . .");
        downloadFile();

    }

    private void downloadFile() {
        try {
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();

            localfile = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/"+fname));
            StorageReference ref = storageReference.child(fname);

            ref.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    updateFile();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    toastlong("DTS Update descarga\n"+exception.getMessage());finish();
                }
            });

        } catch (Exception e) {
            toastlong("DTS Update descarga\n"+e.getMessage());finish();
        }
    }

    private void updateFile() {
        try {
            Uri uri =Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/"+fname));

            Intent intentUrl = new Intent(Intent.ACTION_VIEW);
            intentUrl.setDataAndType(uri,"application/vnd.android.package-archive");
            intentUrl.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentUrl);

            finish();
        } catch (Exception e) {
            toastlong("DTS Update Instalación\n"+e.getMessage());finish();
        }
    }

    //endregion

    //region Aux

    private void grantPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startApplication();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            } else {
                startApplication();
            }
        } catch (Exception e) {}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
                startApplication();
            } else {
                Toast.makeText(this, "Permission not granted.", Toast.LENGTH_LONG).show();
                super.finish();
            }
        } catch (Exception e){}
    }

    protected void toast(String msg) {
        Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    protected void toastlong(String msg) {
        Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = this.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    //endregion

}