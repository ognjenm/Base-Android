package com.allem.alleminmotion.visacheckout.utils;

/**
 * Created by jsandoval on 21/06/16.
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.IntentCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.allegra.handysdk.bean.BeanConstants;
import com.allem.alleminmotion.visacheckout.EditProfileActivity;
import com.allem.alleminmotion.visacheckout.HomeActivity_Handy;
import com.allem.alleminmotion.visacheckout.MainActivity;
import com.allem.alleminmotion.visacheckout.MyBookingActivity;
import com.allem.alleminmotion.visacheckout.R;
import com.squareup.picasso.Picasso;


public class DrawerMenu extends LinearLayout implements View.OnClickListener {

    LinearLayout llHome,llMyBooking,llMyWallet,llAboutUs,llLogout,llEditProfile,llmyaddress;
    Context context;
    TextView tvName,tvEditName;
    CircularImageView cv_icon;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public DrawerMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        pref=context.getSharedPreferences("My_Pref",0);
        editor= pref.edit();
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.drawer_menu, null);
        LinearLayout sliderLinearLayout = (LinearLayout) view
                .findViewById(R.id.ll_main_drawer);
        sliderLinearLayout.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        this.addView(sliderLinearLayout);

        init();

        llHome.setOnClickListener(this);
        llMyBooking.setOnClickListener(this);
        llMyWallet.setOnClickListener(this);
        llAboutUs.setOnClickListener(this);
        llLogout.setOnClickListener(this);
        llEditProfile.setOnClickListener(this);
        llmyaddress.setOnClickListener(this);

        if (Constants.menu_selection == 0) {
        } else if (Constants.menu_selection == 1) {
            llHome.setBackgroundDrawable(getResources().getDrawable(R.drawable.leftmenu_selected_bg));

        } else if (Constants.menu_selection == 2) {
            llMyBooking.setBackgroundDrawable(getResources().getDrawable(R.drawable.leftmenu_selected_bg));
        }
        else if (Constants.menu_selection == 3) {
            llMyWallet.setBackgroundDrawable(getResources().getDrawable(R.drawable.leftmenu_selected_bg));
        }
        else if (Constants.menu_selection == 4) {
            llAboutUs.setBackgroundDrawable(getResources().getDrawable(R.drawable.leftmenu_selected_bg));
        } else if (Constants.menu_selection == 5) {
            llAboutUs.setBackgroundDrawable(getResources().getDrawable(R.drawable.leftmenu_selected_bg));
        }


    }
    private void init() {

        llHome = (LinearLayout) findViewById(R.id.llHome);
        llMyBooking = (LinearLayout) findViewById(R.id.llMyBooking);
        llMyWallet = (LinearLayout) findViewById(R.id.llMyWallet);
        llAboutUs = (LinearLayout) findViewById(R.id.llAboutUs);
        llLogout = (LinearLayout) findViewById(R.id.llLogout);
        llEditProfile=(LinearLayout)findViewById(R.id.llEditProfile);
        llmyaddress=(LinearLayout)findViewById(R.id.llmyaddress);

        tvName=(TextView)findViewById(R.id.tv_name_drawermenu);
        tvEditName=(TextView)findViewById(R.id.tv_editprofile_drawermenu);
        cv_icon=(CircularImageView)findViewById(R.id.cv_icon);

        Picasso.with(context).load(BeanConstants.customer_img_get+BeanConstants.loginData.getUser_Profile_Image()+"&h=120").error(R.drawable.ic_launcher).placeholder(R.drawable.ic_launcher).into(cv_icon);
        tvName.setText(BeanConstants.loginData.getUser_First_Name()+" "+BeanConstants.loginData.getUser_Last_Name());

    }

    @Override
    public void onClick(View v) {
        if (v == llHome) {
            Constants.menu_selection = 1;
            Intent intent=new Intent(context, HomeActivity_Handy.class);
            context.startActivity(intent);
        }
        else if(v==llEditProfile)
        {
            Constants.menu_selection = 0;
            Intent intent=new Intent(context, EditProfileActivity.class);
            context.startActivity(intent);
        }
        else if (v == llMyBooking) {
            Constants.menu_selection = 2;
            Intent intent=new Intent(context, MyBookingActivity.class);
            context.startActivity(intent);
        } else if (v == llLogout) {
            editor.clear();
            editor.commit();
            Intent i = new Intent(context, MainActivity.class);
            ComponentName cn = i.getComponent();
            Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(mainIntent);
        }

    }

}
