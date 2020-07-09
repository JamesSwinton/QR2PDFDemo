package com.zebra.jamesswinton.qr2pdfdemo;

import android.app.Application;

import com.zebra.jamesswinton.qr2pdfdemo.utilities.DataWedgeUtilities;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataWedgeUtilities.setProfileConfig(this, "QR2PDFDemo",
                getPackageName(), ".MainActivity");
    }

}
