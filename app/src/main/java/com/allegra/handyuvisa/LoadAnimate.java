package com.allegra.handyuvisa;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by lchui on 1/4/16.
 */

//This class is one that consume a lot of memory, because of the way in that is menu inflated.
//Example: super.onCreate(savedInstanceState);
//setContentView(R.layout.activity_loadanimate);
public class LoadAnimate extends FragmentActivity implements com.allegra.handyuvisa.BackFragment.MenuSelectListener {

    //*************GLOBAL ATTRIBUTES*******************
    private static final String TAG = "LoadAnimate";
    private static final String FRAGMENT_BACK = "FRAGMENT_BACK";
    private static final String FRAGMENT_LOADING = "LOADING";
    private static final String FRAGMENT_IN_PROGRESS = "IN_PROGRESS";
   // private static final String FRAGMENT_FRONT = "FRAGMENT_FRONT";
    private com.allegra.handyuvisa.BackFragment backFragment;
    //private FrontBackAnimate.FrontFragment frontFragment;
    private LoadingFragment loadingFragment;
    private InProgressFragment inProgressFragment;
    private int state = 0;
    private static InflateReadyListener listener;
    private static int inProgressLayoutId = R.layout.fragment_call_in_progress;
    private static int statusIconRes = R.drawable.icon_onetouchcall;
    private static int statusRes = R.string.txt_lbl_callwait;
    private static ImageView fabButton;
    private static TextView status;

    //***************INTERFACES*****************
    public interface InflateReadyListener {

         void initViews(View root);
         void onCancelLoading();
    }

    //***************OVERRIDE METHODS**************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loadanimate);

        loadingFragment = new LoadingFragment();
        inProgressFragment = new InProgressFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.container_load_animate, inProgressFragment, FRAGMENT_IN_PROGRESS)
                .add(R.id.container_load_animate, loadingFragment, FRAGMENT_LOADING)
                .commit();
        //********
        /*frontFragment = new FrontBackAnimate.FrontFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.container_front_back, frontFragment, FRAGMENT_FRONT)
                .commit();*/

        getFragmentManager().beginTransaction().hide(inProgressFragment).commit();
        backFragment = (com.allegra.handyuvisa.BackFragment) getFragmentManager().findFragmentByTag(FRAGMENT_BACK);
        backFragment.menulistener = this;
    }

    @Override
    public void getStartActivity(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.animator.front_slide_in, R.animator.back_slide_out);
        finish();
    }

    //***************PROPER METHODS**************

     public  void animateBetter(){

         //Log.d(TAG, "Llega al animateBetter");
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int resId = R.animator.front_open;
        if (width > 800) {
            resId = R.animator.front_open_xlarge;
        }
         getFragmentManager().beginTransaction().hide(loadingFragment).hide(backFragment).commit();
         if (state == 0) {//Button menu pressed, BackFragment is hidden
             state = 1;
             showStatusBar(false);
             getFragmentManager().beginTransaction()
                     .setCustomAnimations(resId, 0)
                     .show(loadingFragment)
                     .setCustomAnimations(R.animator.back_exposed, 0)
                     .show(backFragment)
                     .commit();
            // Log.d(TAG, "Llega 0");
         } else {
             state = 0;
             showStatusBar(true);
             getFragmentManager().beginTransaction()
                     .setCustomAnimations(R.animator.front_close, 0)
                     .show(loadingFragment)
                     .setCustomAnimations(R.animator.back_hidden, 0)
                     .show(backFragment)
                     .commit();
            // Log.d(TAG, "Llega 1");
         }


         /* if (state == 1) {
            getFragmentManager().beginTransaction().hide(loadingFragment).hide(backFragment).commit();
            //state = 1;
            Log.d(TAG, "Llega al state == 1");
            state = 0;
            showStatusBar(true);
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.front_close, 0)
                    .show(loadingFragment)
                    .setCustomAnimations(R.animator.back_hidden, 0)
                    .show(backFragment)
                    .commit();
        }*/
    }

    public void onCloseMenu(View view) {
        animateBetter();
    }

    protected void setView(int inProgressRes, int iconRes, int status, InflateReadyListener inflateReadyListener) {
        listener = inflateReadyListener;
        inProgressLayoutId = inProgressRes;
        statusIconRes = iconRes;
        statusRes = status;
        showStatusBar(false);
    }

    private void showStatusBar(boolean toShow) {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = (toShow) ? View.SYSTEM_UI_FLAG_VISIBLE : View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

    }

    protected void animate() {
        showStatusBar(true);
       // Log.d(TAG, "Switching fragment now - in CALLING");
        loadingFragment = (LoadingFragment) getFragmentManager().findFragmentByTag(FRAGMENT_LOADING);
        inProgressFragment = (InProgressFragment) getFragmentManager().findFragmentByTag(FRAGMENT_IN_PROGRESS);
        if (loadingFragment != null) {
          //  Log.d(TAG,"Llega al loadingFragment != null");
            getFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .hide(loadingFragment)
                    .show(inProgressFragment)
                    .commit();
        } else {
           // Log.d(TAG, "fragment is null????");
        }
    }

    protected void animateFrontBack() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;

        int resId = R.animator.front_open;
        if (width > 800) {
            resId = R.animator.front_open_xlarge;
        }

        getFragmentManager().beginTransaction().remove(loadingFragment).hide(inProgressFragment).hide(backFragment).commit();
        if (state == 0) {
            state = 1;
            showStatusBar(false);
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(resId, 0)
                    .show(inProgressFragment)
                    .setCustomAnimations(R.animator.back_exposed, 0)
                    .show(backFragment)
                    .commit();
        } else {
            state = 0;
            showStatusBar(true);
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.front_close, 0)
                    .show(inProgressFragment)
                    .setCustomAnimations(R.animator.back_hidden, 0)
                    .show(backFragment)
                    .commit();
        }


    }

    protected int getInProgressLayoutRes() {
        return inProgressLayoutId;
    }

    protected int getStatusStringRes(){
        return R.string.txt_lbl_callwait;
    }

    protected int getStatusIconRes() {
        return statusIconRes;
    }

    protected void stopProgress() {
        //fabButton.showProgress(false);
    }

    protected void showProgress(boolean show) {
        //fabButton.showProgress(show);
        //fabButton.setEnabled(show);
    }

    protected void setStatus(int resId) {
        status.setText(resId);
    }

    public void onMenu(View view) {
        //onBackPressed();
        //overridePendingTransition(R.animator.back_slide_in, R.animator.front_slide_out);
        animateFrontBack();
    }

    public void onHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.animator.front_slide_in, R.animator.back_slide_out);
        finish(); // call this to finish the current activity
    }

    public void onBackButton(View view){
        finish();
    }

  /*  public void onCloseMenu(View view) {
        animateFrontBack();
    }*/

    public void onUp(View view) {
        super.onBackPressed();
    }

    //******************INNER CLASSES***************************
    public static class LoadingFragment extends Fragment {

        public LoadingFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            //*LLamar al layout del loader
            View root = inflater.inflate(R.layout.fragment_loading, container, false);

            fabButton = (ImageView) root.findViewById(R.id.load_circle);
            //ImageView icon_wait = (ImageView)root.findViewById(R.id.frame_gif);
            Button endCall = (Button) root.findViewById(R.id.cancel_one_touch);
            int iconRes = statusIconRes;
            //icon_wait.setImageDrawable(getResources().getDrawable(iconRes));
            status = (TextView) root.findViewById(R.id.tv_status_otc);
            status.setText(statusRes);

            /*Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.custom_louder);
            fabButton.startAnimation(animation);*/
            //Animation
            fabButton.setVisibility(View.VISIBLE);
            fabButton.post(new Runnable() {
                @Override
                public void run() {
                    ((AnimationDrawable) fabButton.getBackground()).start();
                }
            });
            endCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCancelLoading();
                   // Log.d(TAG,"Llega al clickListener de endCall" );
                }
            });

            return root;
        }
    }

    public static class InProgressFragment extends Fragment {

        public InProgressFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(inProgressLayoutId, container, false);
            listener.initViews(root);
            return root;
        }
    }
}
