package com.allegra.handyuvisa;

import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.allegra.handyuvisa.utils.Constants;
import com.allegra.handyuvisa.utils.KeySaver;
/*import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;*/
import com.allem.onepocket.utils.OPKLibraryConfig;
import com.allem.onepocket.utils.ThemeType;
import com.splunk.mint.Mint;
import com.urbanairship.UAirship;

import java.util.List;

/**
 * Created by lisachui on 12/1/15.
 */
public class VisaCheckoutApp extends MultiDexApplication {

    private static final String TAG = "VisaCheckoutApp";

    private String idSession=null,channel,urlResto;
    private String rawPassword;
    private int idCuenta;
    private String urlHotel;
    //final String SPLUNK_API_KEY = "d87dc4ae6083e6b16e3b473";
    //Mint.initAndStartSession(MyActivity.this, "e74061f2");
    //final String SPLUNK_API_KEY = "e74061f2";

    @Override
    public void onCreate() {
        //super.onCreate();
        //SystemClock.sleep(0);   // For running splash screen
        super.onCreate();


        //UrbanAirship
        UAirship.takeOff(this, new UAirship.OnReadyCallback() {
            @Override
            public void onAirshipReady(UAirship airship) {

                // Enable user notifications
                airship.getPushManager().setUserNotificationsEnabled(true);
                UAirship.shared().getNamedUser().setId(null);
            }
        });
        initOnepocket();
        /*Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(this, "YLafvQVsiUgpHPhTBZFuEIEfdnCtzHNV6fwiOWnY", "PpAKINbIw42zMgFuzstKl6IOrrQqRFAxupHqkGdn");
        ParseInstallation.getCurrentInstallation().saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) Log.e(TAG, e.toString());
                else {
                    ParseUser.enableAutomaticUser();
                    ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            currentUser = ParseUser.getCurrentUser();
                            if (currentUser != null) {
                                Log.d(TAG, "currentuser: " + currentUser.getObjectId() + " platform:" + Build.VERSION.RELEASE);
                                currentUser.put("platform", "Android");
                                currentUser.put("systemVer", Build.VERSION.RELEASE);
                                currentUser.saveInBackground();
                                ParseInstallation currentInstall = ParseInstallation.getCurrentInstallation();
                                currentInstall.put("user", currentUser);
                                currentInstall.saveInBackground();
                            }
                        }
                    });
                    subscribeGlobal();
                }
            }
        });
*/
        // Setup handler for uncaught exceptions.
        /*Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });*/
    }


    public String getIdSession() {
        return idSession;
    }

    public void setIdSession(String idSession) {
        this.idSession = idSession;
    }

    public String getRawPassword() {
        return rawPassword;
    }

    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
    }

    public int getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(int idCuenta) {
        this.idCuenta = idCuenta;
    }

    public void deleteSesion(){
        this.idCuenta=-1;
        this.idSession=null;
    }


    public String getUrlHotel() {
        return urlHotel;
    }

    public void setUrlHotel(String urlHotel) {
        this.urlHotel = urlHotel;
    }

    public void handleUncaughtException (Thread thread, Throwable e) {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        Intent intent = new Intent ();
        intent.setAction("com.allem.alleminmotion.visacheckout.SEND_LOG"); // see step 5.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity(intent);

        System.exit(1); // kill off the crashed app
    }

    private void initOnepocket() {
        OPKLibraryConfig.setTestMode(Constants.TESTING);
    }
}
