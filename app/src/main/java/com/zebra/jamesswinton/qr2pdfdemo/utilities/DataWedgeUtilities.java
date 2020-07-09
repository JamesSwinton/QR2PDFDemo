package com.zebra.jamesswinton.qr2pdfdemo.utilities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DataWedgeUtilities {

    // Output Params
    public static final String SCAN_ACTION = "com.zebra.jamesswinton.qr2pdfdemo.SCAN";

    //  DataWedge API Strings
    public static final String ACTION_DATAWEDGE_FROM_6_2 = "com.symbol.datawedge.api.ACTION";
    public static final String ACTION_RESULT_DATAWEDGE_FROM_6_2 = "com.symbol.datawedge.api.RESULT_ACTION";
    public static final String ACTION_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION_ACTION";

    // Datawedge Extra Strings
    private static final String EXTRA_GET_PROFILES_LIST = "com.symbol.datawedge.api.GET_PROFILES_LIST";
    private static final String EXTRA_ENUMERATESCANNERS_FROM_6_3 = "com.symbol.datawedge.api.ENUMERATE_SCANNERS";
    private static final String EXTRA_GET_DATAWEDGE_STATUS = "com.symbol.datawedge.api.GET_DATAWEDGE_STATUS";
    private static final String EXTRA_ENABLE_DATAWEDGE = "com.symbol.datawedge.api.ENABLE_DATAWEDGE";
    private static final String EXTRA_GET_VERSION_INFO = "com.symbol.datawedge.api.GET_VERSION_INFO";
    private static final String EXTRA_GET_DISABLED_APP_LIST = "com.symbol.datawedge.api.GET_DISABLED_APP_LIST";
    private static final String EXTRA_RESTORE_CONFIG = "com.symbol.datawedge.api.RESTORE_CONFIG";
    private static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";
    private static final String EXTRA_CLONE_PROFILE = "com.symbol.datawedge.api.CLONE_PROFILE";
    private static final String EXTRA_RENAME_PROFILE = "com.symbol.datawedge.api.RENAME_PROFILE";
    private static final String EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";
    private static final String EXTRA_SOFT_SCAN_TRIGGER = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER";


    /**
     * Configure Profile
     */

    public static void setProfileConfig(Context context, String profileName, String packageName,
                                        String activityName) {

        /**
         * Create Main Bundle, which we will nest our other bundles into
         */

        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", profileName); // name of profile
        profileConfig.putString("PROFILE_ENABLED", "true"); // enable / disable profile
        profileConfig.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST"); // configuration mode

        /**
         * Create AppConfig bundle, to define which application & activity(s) to associate with
         * our profile
         */

        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", packageName); // package name of application
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{packageName + activityName}); // list of activities to associate

        /**
         * Add AppConfig Bundle to Main Bundle (ProfileConfig)
         */

        profileConfig.putParcelableArray("APP_LIST", new Bundle[]{ appConfig });

        /**
         * Create BarcodeConfig Bundle
         */

        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE"); // Define plugin name
        // True: Clear any existing configuration and create a new configuration with the specified parameter values
        // False: Update the existing values and add values not already in the configuration
        barcodeConfig.putString("RESET_CONFIG", "true");

        /**
         * Create BarcodeProperties Bundle - This will be added to the BarcodeConfig Bundle,
         * which will be added to the Main (ProfileConfig) Bundle
         */

        Bundle barcodeProps = new Bundle();
        barcodeProps.putString("scanner_selection", "auto");
        barcodeProps.putString("scanner_input_enabled", "true");
        barcodeProps.putString("decoder_code128", "true");
        barcodeProps.putString("decoder_code39", "true");
        barcodeProps.putString("decoder_ean8", "true");
        barcodeProps.putString("decoder_ean13", "true");

        /**
         * Add BarcodeProperties Bundle to BarcodeConfig Bundle
         */

        barcodeConfig.putBundle("PARAM_LIST", barcodeProps);

        /**
         * Add BarcodeConfig Bundle to ProfileConfig Bundle
         */

        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);

        /**
         * Send Intent to DataWedge
         */

        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_SET_CONFIG, profileConfig, context);

        /*************************************************************************************************
         DataWedge 6.4 only allows a single plugin config to be defined per intent.
         DW6.6 supports the ability to configure multiple plugin configs in a single intent

         If we want to define multiple plugin configs (I.e. Barcode & Intent Output) we need to send
         multiple broadcasts

         DW 6.4 only supports BARCODE, INTENT & KEYSTROKE plugins via intents
         *************************************************************************************************/

        /**
         * Clear our Existing Bundles
         */

        profileConfig.remove("APP_LIST");
        profileConfig.remove("PLUGIN_CONFIG");

        /**
         * Create IntentConfig Bundle
         */

        Bundle intentConfig = new Bundle();
        intentConfig.putString("PLUGIN_NAME", "INTENT"); // Define plugin name
        // True: Clear any existing configuration and create a new configuration with the specified parameter values
        // False: Update the existing values and add values not already in the configuration
        intentConfig.putString("RESET_CONFIG", "true");

        /**
         * Create IntentProperties Bundle - This will be added to the IntentConfig Bundle,
         * which will be added to the Main (ProfileConfig) Bundle
         */

        Bundle intentProps = new Bundle();
        intentProps.putString("intent_output_enabled", "true"); // Enable this plugin
        intentProps.putString("intent_action", SCAN_ACTION); // Specify our action string
        intentProps.putString("intent_delivery", "2"); // Set the intent type - Use "0" for Start Activity, "1" for Start Service, "2" for Broadcast

        /**
         * Add IntentProperties Bundle to IntentConfig Bundle
         */

        intentConfig.putBundle("PARAM_LIST", intentProps);

        /**
         * Add IntentConfig Bundle to Main (ProfileConfig) Bundle
         */

        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig);

        /**
         * Send Intent to DataWedge
         */

        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_SET_CONFIG, profileConfig, context);

    }

    /**
     * Simple DataWedge Methods
     */

    public static void CreateProfile(String profileName, Context context)
    {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_CREATE_PROFILE, profileName, context);
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_GET_PROFILES_LIST, "", context);
    }

    public static void GetProfilesList(Context context) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_GET_PROFILES_LIST, "", context);
    }

    public static void EnumerateScanners(Context context) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_ENUMERATESCANNERS_FROM_6_3, "", context);
    }

    public static void GetDataWedgeStatus(Context context) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_GET_DATAWEDGE_STATUS, "", context);
    }

    public static void GetVersionInfo(Context context) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_GET_VERSION_INFO, "", context);
    }

    public static void GetDisabledAppList(Context context) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_GET_DISABLED_APP_LIST, "", context);
    }

    public static void EnableDataWedge(boolean isChecked, Context context) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_ENABLE_DATAWEDGE, isChecked, context);
    }

    public static void CloneProfile(String[] profilesBeingCloned, Context context) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_CLONE_PROFILE, profilesBeingCloned, context);
    }

    public static void RenameProfile(String[] profilesBeingRenamed, Context context) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_RENAME_PROFILE, profilesBeingRenamed, context);
    }

    public static void RestoreConfig(Context context) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_RESTORE_CONFIG, "", context);
    }

    public static void startSoftScan(Context context) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_SOFT_SCAN_TRIGGER, "START_SCANNING", context);
    }

    public static void stopSoftScan(Context context) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE_FROM_6_2, EXTRA_SOFT_SCAN_TRIGGER, "STOP_SCANNING", context);
    }

    /**
     * Utility Methods for Sending DataWedge Intents
     */

    private static void sendDataWedgeIntentWithExtra(String action, String extraKey, String extraValue, Context context)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        // This will trigger DataWedge to send the results of this intent as a broadcast, which we can catch in our activity via a broadcast receiver
        dwIntent.putExtra("SEND_RESULT", "TRUE");
        // Optional Command Identifier
        // dwIntent.putExtra("COMMAND_IDENTIFIER", "123");
        context.sendBroadcast(dwIntent);
    }

    private static void sendDataWedgeIntentWithExtra(String action, String extraKey, boolean extraValue, Context context) {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        // This will trigger DataWedge to send the results of this intent as a broadcast, which we can catch in our activity via a broadcast receiver
        dwIntent.putExtra("SEND_RESULT", "TRUE");
        // Optional Command Identifier
        // dwIntent.putExtra("COMMAND_IDENTIFIER", "123");
        context.sendBroadcast(dwIntent);
    }

    private static void sendDataWedgeIntentWithExtra(String action, String extraKey, Bundle extraValue, Context context)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        // This will trigger DataWedge to send the results of this intent as a broadcast, which we can catch in our activity via a broadcast receiver
        dwIntent.putExtra("SEND_RESULT", "TRUE");
        // Optional Command Identifier
        // dwIntent.putExtra("COMMAND_IDENTIFIER", "123");
        context.sendBroadcast(dwIntent);
    }

    private static void sendDataWedgeIntentWithExtra(String action, String extraKey, String[] extraValue, Context context)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        // This will trigger DataWedge to send the results of this intent as a broadcast, which we can catch in our activity via a broadcast receiver
        dwIntent.putExtra("SEND_RESULT", "TRUE");
        // Optional Command Identifier
        // dwIntent.putExtra("COMMAND_IDENTIFIER", "123");
        context.sendBroadcast(dwIntent);
    }


}
