package com.allem.alleminmotion.visacheckout;

/**
 * Created by victor on 19/02/15.
 * com.allem.allemevent.fragactiv
 */

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.allem.alleminmotion.visacheckout.async.AsyncSoapObject;
import com.allem.alleminmotion.visacheckout.async.AsyncTaskSoapObjectResultEvent;
import com.allem.alleminmotion.visacheckout.async.MyBus;
import com.allem.alleminmotion.visacheckout.models.AllemUser;
import com.allem.alleminmotion.visacheckout.parsers.SoapObjectParsers;
import com.allem.alleminmotion.visacheckout.utils.Constants;
import com.allem.alleminmotion.visacheckout.utils.KeySaver;
import com.allem.alleminmotion.visacheckout.utils.Util;
import com.squareup.otto.Subscribe;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

public class LoginActivity extends FrontBackAnimate implements FrontBackAnimate.InflateReadyListener {
    private final String TAG = "LoginActivity";

    private ActionBar actionBar;
    private Context ctx;
    private ImageButton ib_visibilitypass;
    private boolean passIsVisible = false;
    private EditText username, password;
    private Button btn_login, btn_newaccount;
    private ArrayList<NameValuePair> postValues;
    private ProgressBar pb_login;
    private TextView version, forgotpass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Enter LoginActivity");
        ctx = this;
        MyBus.getInstance().register(this);
        super.setView(R.layout.fragment_login, this);
        postValues = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        MyBus.getInstance().unregister(this);

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void initViews(View root) {

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            TextView version = (TextView) root.findViewById(R.id.version);
            if (version != null) {
                version.setText("Version: " + versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "No Version number found");
        }
        forgotpass = (TextView)root.findViewById(R.id.forgotPassword);
        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, LoginForgotActivity.class);
                startActivity(intent);
            }
        });
        ib_visibilitypass = (ImageButton) root.findViewById(R.id.ib_visibilitypass);
        username = (EditText) root.findViewById(R.id.et_email);
        password = (EditText) root.findViewById(R.id.et_password);
        btn_login = (Button) root.findViewById(R.id.btn_login);
        btn_newaccount = (Button) root.findViewById(R.id.btn_register);
        btn_login.setEnabled(false);
        pb_login = (ProgressBar) root.findViewById(R.id.pb_login);
        pb_login.setVisibility(View.GONE);
        if (KeySaver.getStringSavedShare(ctx, Constants.KEY_EMAIL) != null) {
            username.setText(KeySaver.getStringSavedShare(ctx, Constants.KEY_EMAIL));
            //password.setFocusableInTouchMode(true);
            password.requestFocus();
            password.postDelayed(new Runnable() {
                public void run() {
                    final InputMethodManager inputMethodManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(password, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 300);
        }

        setListeners();
    }

    private void setListeners() {
        ib_visibilitypass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passIsVisible) {
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passIsVisible = false;
                    ib_visibilitypass.setImageResource(R.drawable.ic_visibility_off_black_18dp);
                } else {
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passIsVisible = true;
                    ib_visibilitypass.setImageResource(R.drawable.ic_visibility_black_18dp);
                }
                password.setSelection(password.length());
            }
        });
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(editable.toString())
                        .matches() && password.length() > 0) {
                    btn_login.setEnabled(true);
                } else {
                    btn_login.setEnabled(false);
                }
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0 && android.util.Patterns.EMAIL_ADDRESS.matcher(username.getText().toString())
                        .matches()) {
                    btn_login.setEnabled(true);
                } else {
                    btn_login.setEnabled(false);
                }
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Util.hasInternetConnectivity(ctx)) {
                    setWaitinUI(true);
                    if (postValues.size() > 0) postValues.clear();
                    postValues.add(new BasicNameValuePair("email", username.getText().toString()));
                    postValues.add(new BasicNameValuePair("password", password.getText().toString()));
                    AsyncSoapObject.getInstance(Constants.getWSDL(), Constants.NAMESPACE_ALLEM,
                            Constants.METHOD_INICIAR_SESION, postValues, Constants.ACTIVITY_LOGIN).execute();
                } else {
                    Toast.makeText(ctx, R.string.err_no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });


        btn_newaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ctx, LoginNewUser.class);
                LoginActivity.this.startActivityForResult(i, Constants.ACTIVITY_LOGIN_NEW_USER);
            }
        });


    }

    private void setWaitinUI(boolean b) {
        if (b) pb_login.setVisibility(View.VISIBLE);
        else pb_login.setVisibility(View.GONE);
        username.setEnabled(!b);
        password.setEnabled(!b);
        btn_login.setEnabled(!b);
        btn_newaccount.setEnabled(!b);
    }

    private void setActionbar() {
        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setIcon(R.drawable.ab_icon_back);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_SHOW_HOME);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.ACTIVITY_LOGIN_NEW_USER) {
            Intent returnIntent = new Intent();
            if (resultCode == RESULT_OK) {
                setResult(resultCode, returnIntent);
                finish();
            }
        }
    }

    @Subscribe
    public void onAsyncTaskResult(AsyncTaskSoapObjectResultEvent event) {
        if (event.getCodeRequest() == Constants.ACTIVITY_LOGIN) {
            setWaitinUI(false);
            if (event.getResult() != null) {
                AllemUser user = SoapObjectParsers.toAllemUser(event.getResult());
                ((VisaCheckoutApp) this.getApplication()).setIdSession(user.idSesion);
                ((VisaCheckoutApp) this.getApplication()).setIdCuenta(user.idCuenta);
                ((VisaCheckoutApp) this.getApplication()).setRawPassword(password.getText().toString());
                String name = user.email.substring(0, user.email.indexOf('@'));
                String domain = user.email.substring(user.email.indexOf('@') + 1, user.email.length()).replace(".", "");
                String channel = name + domain + user.idCuenta;
                Log.d(TAG, "chnel: " + channel);
                Constants.saveUser(ctx, user, channel);
                ((VisaCheckoutApp) this.getApplication()).unSetParseChannels();
                ((VisaCheckoutApp) this.getApplication()).parseUser(user.email, channel);
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(ctx, event.getFaultString(), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "back pressed");
        setResult(RESULT_CANCELED);
        finish();
        overridePendingTransition(R.animator.back_slide_in, R.animator.front_slide_out);
    }

    public void onMenu(View view) {
        animate();
    }
}