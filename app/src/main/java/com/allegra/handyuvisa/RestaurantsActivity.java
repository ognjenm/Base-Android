package com.allegra.handyuvisa;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.allegra.handyuvisa.utils.Constants;
import com.allegra.handyuvisa.utils.CustomizedTextView;
import com.allegra.handyuvisa.utils.GPSTracker;
import com.allem.onepocket.utils.OPKConstants;

import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

public class RestaurantsActivity extends WebViewActivity implements FrontBackAnimate.InflateReadyListener {

    private ProgressBar progressBar;
    private String url = Constants.getRestaurantUrl();
    private ImageButton arrowBack, arrowF;
    public static final String TAG = "RestaurantsActivity";
    private LocationManager locationManager;
    public String returnURL, onePocketmessage;
    private boolean isGPSEnabled, isNetworkEnabled, enterToGetLocation;
    private Location location;
    GPSTracker gp;
    Double latitude, longitude;
    String mLat, mLong;
    Dialog dialog;
    public static final int REQUEST_LOCATION_CODE = 1234, MY_PERMISSIONS_REQUEST_LOCATION = 4563;
    String[] PERMISSIONS = { android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
    public static final int MY_PERMISSIONS_REQUEST_CALL = 8888;
    String[] MY_PERMISSIONS_CALL = { android.Manifest.permission.CALL_PHONE};
    private String callUrl = "";

    //*************************OVERRIDE METHODS******************

    @Override
    public void initViews(View root) {

        setupWebView(root);
        arrowBack = (ImageButton) root.findViewById(R.id.arrow_back_restaurants);
        arrowF = (ImageButton) root.findViewById(R.id.arrow_foward_restaurants);
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar_Restaurants);

        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGoBack(v);
            }
        });

        arrowF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGoForward(v);
            }
        });
        getLocation();

        // Log.d(TAG, url);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setView(R.layout.activity_restaurants, this); need implement fragment methods
    }

    @Override
    public void onResume() {
        super.onResume();
        if (callUrl.equals("")) {
            loadWebView();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Validate permissions
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasPermissions(getActivity(), PERMISSIONS)) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay! Do the camera-related task you need to do.
                    // Log.e(TAG, "El permiso fue dado");
                    getCurrentLocation();
                } else {
                    // Permission denied, boo! Disable the functionality that depends on this permission.
                    //Log.e(TAG, "Sin el permiso no podemos seguir");
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
            case MY_PERMISSIONS_REQUEST_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCall(callUrl);
                }
                return;
            }
        }
    }

    //************************PROPER METHODS**************************

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity().getApplicationContext());
        builder.setMessage(
                R.string.location_permision)
                .setCancelable(false)
                .setPositiveButton(R.string.location_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION_CODE);
                            }
                        })
                .setNegativeButton(R.string.location_no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public Location getLocation() {

        locationManager = (LocationManager) getActivity().getSystemService(MainActivity.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled) {
            // Log.d(TAG, "ESTOY ACA");
            buildAlertMessageNoGps();
        } else {
            //Validate permissions
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasPermissions(getActivity(), PERMISSIONS)) {
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                getCurrentLocation();
            }
        }
        return location;
    }

    private void getCurrentLocation() {
        
        //TODO: Need to fix. Strong reference. Working on UI Thread
        gp = new GPSTracker(getActivity());
        latitude = gp.getLatitude();
        longitude = gp.getLongitude();

        if (latitude != 0.0) {

            try {
                enterToGetLocation = true;
                Geocoder geocoder = new Geocoder(getActivity(), Locale.ENGLISH);
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String cityName = addresses.get(0).getAddressLine(0);
                String stateName = addresses.get(0).getAddressLine(1);
                String countryName = addresses.get(0).getAddressLine(2);
                String city = "";
                //Split special characters
                if (stateName.contains(", ")) {
                    String[] parts = stateName.split(", ");
                    city = parts[0];
                    //  Log.d("City split", city);
                } else {
                    if (stateName.contains(" ")) {
                        String[] parts = stateName.split(" ");
                        city = parts[0];
                        //  Log.d("City split space", city);
                    }
                }
                String query = URLEncoder.encode(city, "utf-8");
                // Log.d("cityName", cityName);
                // Log.d("stateName", stateName);
                // Log.d("countryName", countryName);
                // Log.d("LATITUDE", latitude.toString());
                // Log.d("LONGITUDE", longitude.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadWebView() {

        mWebView.addJavascriptInterface(new RestaurantsActivity.JsInterface(getActivity()), "androidProxy");
        mWebView.loadUrl(url);
    }

    public void openOnePocket() {

        /*** temporally commented ***/

        /*Intent intent = new Intent(RestaurantsActivity.this, OnepocketPurchaseActivity.class);
        Bundle bundle = Constants.createPurchaseBundle(Constants.getUser(this), onePocketmessage, OPKConstants.TYPE_MCARD, (com.allegra.handyuvisa.VisaCheckoutApp) getApplication());
        intent.putExtras(bundle);
        startActivityForResult(intent, Constants.REQUEST_ONEPOCKET_RETURN);*/
    }

    public void onMenu(View view) {
        ((MainActivity) getActivity()).animate();
    }

    public void onUp(View view) {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

    private void loadArrows() {

        if (mWebView.canGoBack()) {
            arrowBack.setImageDrawable(getResources().getDrawable(R.drawable.navigation__backurl));
        } else {
            arrowBack.setImageDrawable(getResources().getDrawable(R.drawable.navigation__backurl_2));
        }

        if (mWebView.canGoForward()) {
            arrowF.setImageDrawable(getResources().getDrawable(R.drawable.navigation__fwdurl_2));
        } else {
            arrowF.setImageDrawable(getResources().getDrawable(R.drawable.navigation__fwdurl));
        }
    }


    public void onGoBack(View view) {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

    public void onGoForward(View view) {
        if (mWebView.canGoForward()) {
            mWebView.goForward();
        }
    }

    private void setupWebView(View root) {
        mWebView = (WebView) root.findViewById(R.id.webView2);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setAllowContentAccess(false);
        mWebView.getSettings().setAllowFileAccess(false);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(false);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.setWebChromeClient(new RestaurantsActivity.GeoWebChromeClient());
        mWebView.setWebViewClient(new SecureBrowser(this));
        setupLoadingView(root);
    }

    private void setupLoadingView(View root) {
        mLoadingView = (FrameLayout) root.findViewById(R.id.loading_view);
        mLoadingBar = (ImageView) root.findViewById(R.id.pb_search_loader);
    }

    @Override
    public boolean onShouldOverrideUrlLoading(WebView webView, String url) {

        if (url.equals("allegra:touchcallService")) {
            Intent i = new Intent(getActivity(), CallActivityServices.class);
            this.startActivity(i);
            return true;
        }
        //Fix For Phone Call functions
        if (url.startsWith("tel:")) {
            callUrl = url;
            startCall(callUrl);
            return true;
        }
        return super.onShouldOverrideUrlLoading(webView, url);
    }

    private void startCall(String url) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasPermission(getActivity(), MY_PERMISSIONS_CALL[0])) {
            ActivityCompat.requestPermissions(getActivity(), MY_PERMISSIONS_CALL, MY_PERMISSIONS_REQUEST_CALL);
        } else {
            Intent intentCall = new Intent(Intent.ACTION_CALL, Uri.parse(url));
            if (ActivityCompat.checkSelfPermission(getActivity(), MY_PERMISSIONS_CALL[0]) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            this.startActivity(intentCall);
        }
    }

    public static boolean hasPermission(Context context, String permission) {
        if (context != null && permission != null) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        progressBar.setVisibility(View.GONE);
        loadArrows();
    }

    //*******************************INNER CLASSES****************************

    private class JsInterface {

        private Context mContext;
        JsInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void myGeoLocation(String mlat, String mLon) {

            mlat = latitude.toString();
            mLon = longitude.toString();
           // Log.d(TAG, "Lat: "+mlat+" Long:"+ mLon);
        }

        @JavascriptInterface
        public void postMessage(final String message) {
            //Log.e("Message", message);
            //Log.e("Repeat message", message);

            //Obtengo latitud y longitud y Se los envío a la función JavaScript MyGeoLocation
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {

                    if (message.contains("myCurrentLocation")) {

                        if (latitude!=null && longitude!= null) {
                           // Log.d(TAG,"Entra al if != null en postMessage");
                           // Log.d(TAG,"Lat: "+latitude.toString()+ " Long: "+longitude.toString());
                            mWebView.evaluateJavascript("javascript:myGeoLocation(" + latitude.toString() + "," + longitude.toString() + ");", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                   // Log.d(TAG, "El value en postMessage "+value);
                                }
                            });

                        } else {//Hardcode Other location in Bogotá
                            mWebView.evaluateJavascript("javascript:myGeoLocation(4.665417, -74.077237);",null);
                           // Log.d(TAG,"lat lng son null en postMessage");
                        }

                    }
                }
            });

            //******
            if (message.contains("showLoader")){
                //Show native loader
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ImageView pb_search_loader = (ImageView) getView().findViewById(R.id.pb_search_loader);
                        ImageView imgProgress = (ImageView) getView().findViewById(R.id.imgProgress);
                        CustomizedTextView textView = (CustomizedTextView)getView().findViewById(R.id.txtLoading);
                        if (pb_search_loader != null)pb_search_loader.setVisibility(View.VISIBLE);
                        if (imgProgress != null)imgProgress.setVisibility(View.VISIBLE);
                        if (mWebView != null)mWebView.setVisibility(View.GONE);
                        if (textView != null)textView.setVisibility(View.VISIBLE);
                    }
                });

            }
            if (message.contains("hideLoader")){
                //Hide native loader
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ImageView pb_search_loader = (ImageView)getView().findViewById(R.id.pb_search_loader);
                        ImageView imgProgress = (ImageView)getView().findViewById(R.id.imgProgress);
                        CustomizedTextView textView = (CustomizedTextView)getView().findViewById(R.id.txtLoading);

                        if (pb_search_loader != null)pb_search_loader.setVisibility(View.GONE);
                        if (imgProgress != null)imgProgress.setVisibility(View.GONE);
                        if (mWebView != null)mWebView.setVisibility(View.VISIBLE);
                        if (textView != null)textView.setVisibility(View.GONE);
                    }
                });

            }
        }

    }

    public class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                                                       GeolocationPermissions.Callback callback) {
            // Always grant permission since the app itself requires location
            // permission and the user has therefore already granted it
            callback.invoke(origin, true, false);
        }
    }

}
