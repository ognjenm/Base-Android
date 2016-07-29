package com.allegra.handyuvisa;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.allegra.handyuvisa.async.AsyncSoapObject;
import com.allegra.handyuvisa.async.AsyncTaskSoapObjectResultEvent;
import com.allegra.handyuvisa.async.MyBus;
import com.allegra.handyuvisa.models.AllemUser;
import com.allegra.handyuvisa.models.CuentaClienteInfo;
import com.allegra.handyuvisa.models.CuentaClienteInfoAdicional;
import com.allegra.handyuvisa.parsers.SoapObjectParsers;
import com.allegra.handyuvisa.utils.Constants;
import com.allegra.handyuvisa.utils.CustomizedEditText;
import com.allegra.handyuvisa.utils.Util;
import com.squareup.otto.Subscribe;

import org.ksoap2.serialization.PropertyInfo;

import java.lang.reflect.Field;

//TODO: Wilson Bug
public class EditProfileActivity extends FrontBackAnimate implements FrontBackAnimate.InflateReadyListener {

    private final String M_SELECTION_DIVIDER = "mSelectionDivider";
    CustomizedEditText txtName, txtLastName, txtMobile, txtEmail, txtPass, txtNewPass, txtNewPassConfirm;
    Button cancel, save;
    AllemUser user;
    private ProgressBar pb_create;
    private NumberPicker countryPicker, typeOfIdPicker;
    private Context ctx;
    TextView txtTypeOfIdSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setView(R.layout.activity_edit_profile, this);
        ctx = this;
        MyBus.getInstance().register(this);
    }

    private void setViewElements(View root){

        txtTypeOfIdSelected = (TextView) root.findViewById(R.id.etTypeOfId);
        txtName= (CustomizedEditText) root.findViewById(R.id.et_names);
        txtLastName= (CustomizedEditText) root.findViewById(R.id.et_surname);
        txtMobile= (CustomizedEditText) root.findViewById(R.id.et_mobile);
        txtEmail= (CustomizedEditText)root.findViewById(R.id.et_email);
        cancel= (Button) root.findViewById(R.id.btn_ed_cancel);
        save= (Button)root.findViewById(R.id.btn_ed_save);
        txtPass =(CustomizedEditText) root.findViewById(R.id.et_current_password);
        txtNewPass=(CustomizedEditText) root.findViewById(R.id.et_new_password);
        pb_create = (ProgressBar) root.findViewById(R.id.pb_create);
        txtNewPassConfirm=(CustomizedEditText) root.findViewById(R.id.et_reppassword);
        loadUserProfile();
    }

    @Override
    protected void onDestroy() {
        MyBus.getInstance().unregister(this);
        super.onDestroy();
    }

    private void loadUserProfile(){
        user = Constants.getUser(this);
        txtName.setText(user.nombre);
        txtLastName.setText(user.apellido);
        if(user.celular.length()>0) txtMobile.setText(user.celular);
        txtEmail.setText(user.email);
        CuentaClienteInfoAdicional codigoPais = new CuentaClienteInfoAdicional();

    }

    private void updateTextOfTypeOfId() {

        txtTypeOfIdSelected = (TextView) findViewById(R.id.etTypeOfId);
        int typeOfID = typeOfIdPicker.getValue();

        switch (typeOfID) {
            case 0:
                txtTypeOfIdSelected.setText(getString(R.string.txt_citizenship_card));
                break;
            case 1:
                txtTypeOfIdSelected.setText(getString(R.string.txt_Foreigner_ID));
                break;
            case 2:
                txtTypeOfIdSelected.setText(getString(R.string.txt_passport));
                break;
            case 3:
                txtTypeOfIdSelected.setText(getString(R.string.txt_identity_card));
                break;
        }

    }

    private void onAlertSelectTypeOfId() {//View view

        Log.e("Bug 1", "Entro");
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_select_type_of_id);
        dialog.show();
        typeOfIdPicker = new NumberPicker(this);
        typeOfIdPicker = (NumberPicker) dialog.findViewById(R.id.selectTypeOfIdPicker);
        typeOfIdPicker.setMinValue(0);
        typeOfIdPicker.setMaxValue(3);
        typeOfIdPicker.setDisplayedValues(new String[]{getString(R.string.txt_citizenship_card),
                getString(R.string.txt_Foreigner_ID), getString(R.string.txt_passport), getString(R.string.txt_identity_card)});
        setDividerColor(typeOfIdPicker);
        //TextViews (Cancel and Ok)
        TextView textCancel = (TextView) dialog.findViewById(R.id.textCancelDialogTypeOfId);
        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        TextView textOk = (TextView) dialog.findViewById(R.id.textOkDialogTypeOfId);
        textOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                updateTextOfTypeOfId();
            }
        });
    }

    private void setDividerColor(NumberPicker picker) {

        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals(M_SELECTION_DIVIDER)) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.dark_gray));
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

    }

    private void updateUser(){

        if(validateData()) {
            CuentaClienteInfo client = new CuentaClienteInfo();
            CuentaClienteInfoAdicional additionalInfo = new CuentaClienteInfoAdicional();
            client.setSaludo("1");
            client.setCurrentPassword(txtPass.getText().toString());
            client.setIdSesion(((VisaCheckoutApp) this.getApplication()).getIdSession());
            client.setNombre(txtName.getText().toString());
            client.setApellido(txtLastName.getText().toString());
            if(txtNewPass.getText().toString().length()>0) {
                client.setPassword(txtNewPass.getText().toString());
            }
            additionalInfo.setCelular(txtMobile.getText().toString());
            additionalInfo.setEmpresa("");
            additionalInfo.setCargo("");
            additionalInfo.setCiudad("");
            additionalInfo.setClase("");

            client.setCuentaClienteInformacionAdicional(additionalInfo);

            PropertyInfo property = new PropertyInfo();
            property.setName(CuentaClienteInfo.PROPERTY);
            property.setValue(client);
            property.setType(client.getClass());
            Log.d("IDSession",((VisaCheckoutApp)this.getApplication()).getIdSession());
            if (Util.hasInternetConnectivity(this)) {
                setWaitinUI(true);
                AsyncSoapObject.getInstance(Constants.getWSDL(), Constants.NAMESPACE_ALLEM,
                        Constants.METHOD_ACTUALIZAR_CUENTA, property, Constants.ACTIVITY_UPDATE_USER).execute();
            } else {
                Toast.makeText(this, R.string.err_no_internet, Toast.LENGTH_SHORT).show();
            }


        }else{
            Toast.makeText(
                    ctx,
                    getString(R.string.form_errors),
                    Toast.LENGTH_LONG).show();
        }

    }

    private boolean validateData(){

        boolean result=true;

        if(txtName.getText().toString().length()<2){
            txtName.setTextColor(Color.RED);
            txtName.setHintTextColor(Color.RED);
            result=false;
        }
        if(txtLastName.getText().toString().length()<2){
            txtLastName.setTextColor(Color.RED);
            txtLastName.setHintTextColor(Color.RED);
            result=false;
        }

        if(txtPass.getText().toString().length()>0 || txtNewPass.getText().toString().length()>0 || txtNewPassConfirm.getText().toString().length()>0) {



            if (txtPass.getText().toString().length() < 6) {
                txtPass.setTextColor(Color.RED);
                txtPass.setHintTextColor(Color.RED);
                result = false;
            }

            if (txtNewPass.getText().toString().length() < 6) {
                txtNewPass.setTextColor(Color.RED);
                txtNewPass.setHintTextColor(Color.RED);
                result = false;
            }


            if (!txtNewPass.getText().toString().equals(txtNewPassConfirm.getText().toString())) {
                txtNewPass.setTextColor(Color.RED);
                txtNewPassConfirm.setTextColor(Color.RED);
                txtNewPass.setHintTextColor(Color.RED);
                txtNewPassConfirm.setHintTextColor(Color.RED);
                result = false;
            }

        }


        return result;
    }

    public void onMenu(View view) {
        animate();
    }


    private void setListeners(){
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //animate();
                onBackPressed();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser();
            }
        });

        txtTypeOfIdSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {onAlertSelectTypeOfId();
            }
        });


    }

    private void setWaitinUI(boolean b) {
        if (b) pb_create.setVisibility(View.VISIBLE);
        else pb_create.setVisibility(View.GONE);
        txtName.setEnabled(!b);
        txtLastName.setEnabled(!b);
        txtMobile.setEnabled(!b);
        txtPass.setEnabled(!b);
        txtNewPass.setEnabled(!b);
        txtNewPassConfirm.setEnabled(!b);
        save.setEnabled(!b);
        cancel.setEnabled(!b);
    }

    @Override
    public void initViews(View root) {
        setViewElements(root);
        setListeners();
    }

    @Subscribe
    public void onAsyncTaskResult(AsyncTaskSoapObjectResultEvent event) {

        if (event.getCodeRequest()== Constants.ACTIVITY_UPDATE_USER){
            setWaitinUI(false);
            Intent returnIntent = new Intent();
            if (event.getResult()!=null){
                AllemUser user = SoapObjectParsers.toAllemUser(event.getResult());
                AllemUser currentUser= Constants.getUser(EditProfileActivity.this);
                user.saludo= currentUser.saludo;
                user.idSesion= currentUser.idSesion;
               Constants.saveUser(EditProfileActivity.this,user,currentUser.channel);

                new AlertDialog.Builder(ctx).setTitle(getString(R.string.txt_lbl_notification)).setMessage(getString(R.string.edit_successful)).setPositiveButton("ok", null).show();
            }else{
                Toast.makeText(EditProfileActivity.this, event.getFaultString(), Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED, returnIntent);
            }

        }
    }

}
