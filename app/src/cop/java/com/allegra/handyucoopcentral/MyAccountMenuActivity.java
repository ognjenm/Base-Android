package com.allegra.handyuvisa;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.allegra.handyuvisa.MyTips;
import com.allegra.handyuvisa.utils.Connectivity;
import com.allegra.handyuvisa.utils.CustomizedTextView;
import com.allegra.handyuvisa.utils.UsuarioSQLiteHelper;

public class MyAccountMenuActivity extends FrontBackAnimate implements FrontBackAnimate.InflateReadyListener {


    private ActionBar actionBar;
    Context ctx;
    CustomizedTextView txtGetYourCertificate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setView(R.layout.activity_my_account_menu, this);
        ctx = this;

    }


    @Override
    public void initViews(View root) {
        setActionbar();
        initializeListView(root);
    }

    private void initializeListView(View root) {


        ListView lv = (ListView) root.findViewById(R.id.profileOptionsListView);
        final String[] names = {
                getString(R.string.title_my_profile),
                getString(R.string.benefits),
                getString(R.string.mytips),
                getString(R.string.coverage),
                getString(R.string.transactions_history),
                getString(R.string.legal_title),
        };
        final Integer[] images = {R.drawable.menu__profile, R.drawable.my_benefits,R.drawable.menu_tips, R.drawable.coverage, R.drawable.menu__history,
                R.drawable.legal2};
        final Class[] activities = {MyAccountActivity.class, com.allegra.handyuvisa.MyBenefits.class, MyTips.class,
                ProofOfCoverageDinamicoActivity.class, OneTransactionsActivity.class, LegalActivity.class};
        lv.setAdapter(new ArrayAdapter<String>(MyAccountMenuActivity.this, R.layout.profile_layout, names) {

            public View getView(final int position, View view, ViewGroup parent) {
                final LayoutInflater inflater = MyAccountMenuActivity.this.getLayoutInflater();
                View rowView = inflater.inflate(R.layout.profile_layout, null, true);

                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MyAccountMenuActivity.this, activities[position]);
                        MyAccountMenuActivity.this.startActivity(intent);
                    }

                });
/*                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position==2){
                            if(Util.hasInternetConnectivity(ctx)){
                                if(((VisaCheckoutApp)ctx.getApplicationContext()).getIdSession()==null){
                                    setGetYourCertificateLayout();
                                }else {
                                    Intent i = new Intent(ctx,ProofOfCoverageActivity.class);
                                    startActivity(i);
                                }
                            }else {

                                if(((VisaCheckoutApp)ctx.getApplicationContext()).getIdSession()==null){
                                    setGetYourCertificateLayout();
                                }else {
                                    //Toast.makeText(ctx,"NO TENGO INTERNET Y YA ME HE LOGEADO ACA LLAMO LA BASE DE DATOS", Toast.LENGTH_SHORT).show();
                                    usuarioSQLiteHelper = new UsuarioSQLiteHelper(getApplicationContext());
                                    db = usuarioSQLiteHelper.getReadableDatabase();
                                    cursor = usuarioSQLiteHelper.getInformationDatabase(db);
                                    if (cursor.moveToFirst()){
                                        do{
                                            nombre = cursor.getString(0);
                                            apellido = cursor.getString(1);
                                            tipoid = cursor.getString(2);
                                            numid = cursor.getString(3);
                                            nummcard = cursor.getString(4);
                                            value1 = cursor.getString(5);
                                            value2 = cursor.getString(6);
                                            value3 = cursor.getString(7);
                                        }while (cursor.moveToNext());
                                    }
                                    Intent intent = new Intent(ctx, ProofOfCoverageActivityOff.class);
                                    intent.putExtra("nombre",nombre);
                                    intent.putExtra("apellido",apellido);
                                    intent.putExtra("tipoid",tipoid);
                                    intent.putExtra("numid",numid);
                                    intent.putExtra("numcard",nummcard);
                                    intent.putExtra("value1",value1);
                                    intent.putExtra("value2",value2);
                                    intent.putExtra("value3",value3);
                                    startActivity(intent);

                                }
                            }


                        } else {
                            Intent intent = new Intent(MyAccountMenuActivity.this, activities[position]);
                            MyAccountMenuActivity.this.startActivity(intent);
                        }
                    }
                });*/
                TextView txtTitle = (TextView) rowView.findViewById(R.id.profileOptionText);
                ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);

                txtTitle.setText(names[position]);
                imageView.setImageResource(images[position]);
                return rowView;
            }

            ;
        });
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

    void setGetYourCertificateLayout() {
        //Change layout
        setContentView(R.layout.get_your_certificate);
        txtGetYourCertificate = (CustomizedTextView) findViewById(R.id.txtGetYourCertificate);
        txtGetYourCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchloginActivity();
            }
        });


    }

    public void onUpProof(View view) {
        onBackPressed();
    }

    void launchloginActivity() {

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void onMenu(View view) {
        animate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Validate login successful
        if (resultCode == RESULT_OK)//Constants.ACTIVITY_LOGIN
        {
            Intent i = new Intent(this, ProofOfCoverageActivity.class);
            this.startActivity(i);
            //sendIntentForProofOfCoverage();
        }

    }
}