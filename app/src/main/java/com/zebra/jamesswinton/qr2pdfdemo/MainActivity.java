package com.zebra.jamesswinton.qr2pdfdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zebra.jamesswinton.qr2pdfdemo.databinding.ActivityMainBinding;
import com.zebra.jamesswinton.qr2pdfdemo.utilities.DataWedgeUtilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Intent.CATEGORY_DEFAULT;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final int PERMISSIONS_REQUEST = 0;
    private static final String[] PERMISSIONS = {
            WRITE_EXTERNAL_STORAGE
    };

    // Variables
    private ActivityMainBinding mDataBinding;

    // DW
    private IntentFilter mIntentFilter = null;

    // Downloads Holder
    private Map<Long, File> mDownloads = new HashMap<>();

    // Dialog
    private AlertDialog mDownloadDialog = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Init Scan Animation
        Glide.with(this).load(R.raw.tap_to_scan).into(mDataBinding.taptoScanImageView);

        // Get Permission
        if (checkStandardPermissions()) {
            DataWedgeUtilities.EnableDataWedge(true, this);
            mDataBinding.taptoScanImageView.setOnTouchListener((view, motionEvent) -> {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        DataWedgeUtilities.startSoftScan(MainActivity.this);
                        return true;
                    case MotionEvent.ACTION_UP:
                        DataWedgeUtilities.stopSoftScan(MainActivity.this);
                        return true;
                }
                return false;
            });
        } else {
            // Request Permissions
            DataWedgeUtilities.EnableDataWedge(false, this);
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIntentFilter == null) {
            mIntentFilter = new IntentFilter();
            mIntentFilter.addCategory(CATEGORY_DEFAULT);
            mIntentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            mIntentFilter.addAction(DataWedgeUtilities.SCAN_ACTION);
        }
        registerReceiver(mScanReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mScanReceiver);
    }

    public boolean checkStandardPermissions() {
        boolean permissionsGranted = true;
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED) {
                permissionsGranted = false;
                break;
            }
        }

        return permissionsGranted;
    }

    private void downloadPdf(String qrCodeData) {
        DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        if (downloadmanager != null) {

            // Init Destination File
            File destinationFile = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "DHL PDFs" + File.separator + "PDF-" + System.currentTimeMillis() + ".pdf");
            if (!destinationFile.getParentFile().exists()) {
                destinationFile.getParentFile().mkdirs();
            }

            // Build Download Request
            DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(qrCodeData))
                    .setTitle("Dummy File")
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationUri(Uri.fromFile(destinationFile))
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true);

            // Start Download
            Long downloadId = downloadmanager.enqueue(downloadRequest);

            // Add Download to Map
            mDownloads.put(downloadId, destinationFile);

            // Show dialog
            mDownloadDialog = new AlertDialog.Builder(this)
                    .setView(R.layout.dialog_download_progress)
                    .create();
            mDownloadDialog.show();

        } else {
            Toast.makeText(this, "Download Manager Unavailable", Toast.LENGTH_LONG).show();
        }
    }

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        private static final String EXTRA_SCAN_DATA = "com.symbol.datawedge.data_string";
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String intentAction = intent.getAction();
                if (intentAction != null) {
                    switch (intentAction) {
                        case DataWedgeUtilities.SCAN_ACTION:
                            if (intent.hasExtra(EXTRA_SCAN_DATA)) {
                                downloadPdf(intent.getStringExtra(EXTRA_SCAN_DATA));
                            } else {
                                Toast.makeText(MainActivity.this, "Invalid Barcode", Toast.LENGTH_LONG).show();
                            }
                            break;
                        case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                            // Hide Dialog
                            if (mDownloadDialog != null && mDownloadDialog.isShowing()) {
                                mDownloadDialog.hide();
                            }

                            // Get Download ID
                            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,
                                    -1);

                            // Get File from Map
                            File pdf = mDownloads.get(downloadId);

                            if (pdf != null) {
                                // Create Content URI
                                Uri pdfUri = FileProvider.getUriForFile(context,
                                        getApplicationContext().getPackageName() + ".provider",
                                        pdf);

                                // Open File
                                Intent printPdfIntent = new Intent(Intent.ACTION_VIEW);
                                printPdfIntent.setDataAndType(pdfUri, "application/pdf");
                                printPdfIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                printPdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(printPdfIntent);
                            } else {
                                Toast.makeText(context, "Could not locate PDF", Toast.LENGTH_LONG).show();
                            }
                            break;
                    }
                }
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);

        // Handle Permissions Request
        if (requestCode == PERMISSIONS_REQUEST) {

            // Validate Permissions State
            boolean permissionsGranted = true;
            if (results.length > 0) {
                for (int result : results) {
                    if (result != PERMISSION_GRANTED) {
                        permissionsGranted = false;
                    }
                }
            } else {
                permissionsGranted = false;
            }

            // Check Permissions were granted & Load slide images or exit
            if (permissionsGranted) {
                DataWedgeUtilities.EnableDataWedge(true, this);
                mDataBinding.taptoScanImageView.setOnTouchListener((view, motionEvent) -> {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            DataWedgeUtilities.startSoftScan(MainActivity.this);
                            return true;
                        case MotionEvent.ACTION_UP:
                            DataWedgeUtilities.stopSoftScan(MainActivity.this);
                            return true;
                    }
                    return false;
                });
            } else {
                // Explain reason
                Toast.makeText(this, "Please enable all permissions to run this app",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}