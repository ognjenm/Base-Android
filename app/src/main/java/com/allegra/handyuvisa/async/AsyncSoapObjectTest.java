package com.allegra.handyuvisa.async;

import android.os.AsyncTask;
import android.util.Log;

import com.allegra.handyuvisa.models.Server;

import org.apache.http.NameValuePair;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.util.ArrayList;

/**
 * Created by victor on 27/02/15.
 * com.allem.allemevent.async
 */
public class AsyncSoapObjectTest extends AsyncTask<String,Void,SoapObject> {

    private static final String TAG = "AsyncSoap" ;
    private String url,namespace,method,soapaction,faultstring;
    private ArrayList<NameValuePair> postValues;
    private int codeRequest, faultcode;
    private PropertyInfo property;
    private String userserver,passserver;
    private SoapObject soapObject;

    private AsyncSoapObjectTest(Server server, String method, SoapObject soapObject, int codeRequest){
        this.url = server.wsdl;
        this.namespace=server.namespace;
        this.method = method;
        this.soapaction =namespace+method;
        this.codeRequest = codeRequest;
        this.soapObject = soapObject;
        this.userserver = server.user;
        this.passserver = server.pass;
    }

    public static AsyncSoapObjectTest getInstance(Server server,String method,SoapObject soapObject,int codeRequest){
        return new AsyncSoapObjectTest(server,method,soapObject,codeRequest);
    }

    @Override
    protected SoapObject doInBackground(String... strings) {
        Log.d(TAG, "url: " + url);
        Log.d(TAG, "namespace: " + namespace);
        Log.d(TAG, "soapaction:" + soapaction);
        HttpTransportSE transporte = new HttpTransportSE(url,30000);

        SoapObject request = new SoapObject(namespace, method),result = null;
        request.addSoapObject(soapObject);
        try {
            /*if (postValues!=null){
                for (int i=0;i<postValues.size();i++){
                    Log.d(TAG, postValues.get(i).getName() + ":"+postValues.get(i).getValue());
                    request.addProperty(postValues.get(i).getName(),postValues.get(i).getValue());
                }
            }



            if (property!=null){
                request.addProperty(property);
            }*/

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.implicitTypes = true;
            envelope.setOutputSoapObject(request);

            envelope.headerOut = buildAuthHeader(envelope);


            transporte.debug=true;
            transporte.call(soapaction, envelope);
            Log.d(TAG, transporte.requestDump);
            result = (SoapObject) envelope.getResponse();

            Log.d(TAG, transporte.responseDump);
            faultcode =-1;
            faultstring="OK";
        } catch (Exception e) {

            //faultcode=Integer.valueOf(error);
            faultcode=-1000;
            faultstring=e.getMessage();
        }

        return result;
    }

    @Override
    protected void onPostExecute(SoapObject result) {
        MyBus.getInstance().post(new AsyncTaskSoapObjectResultEvent((SoapObject)result,codeRequest,faultcode,faultstring));
    }

    private Element[] buildAuthHeader(SoapSerializationEnvelope envelope) {
        Element headers[] = new Element[1];
        headers[0]= new Element().createElement("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");
        headers[0].setAttribute(envelope.env, "mustUnderstand", "1");
        Element security=headers[0];

        Element to = new Element().createElement(security.getNamespace(), "UsernameToken");
        to.setAttribute("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id", "UsernameToken-2");

        Element action1 = new Element().createElement(security.getNamespace(), "Username");
        action1.addChild(Node.TEXT, userserver);
        to.addChild(Node.ELEMENT,action1);

        Element action2 = new Element().createElement(security.getNamespace(), "Password");
        action2.setAttribute(null, "Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
        action2.addChild(Node.TEXT, passserver);
        to.addChild(Node.ELEMENT,action2);
        headers[0].addChild(Node.ELEMENT, to);
        return headers;
    }
}