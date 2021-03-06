package com.allegra.handyuvisa.async;

import org.ksoap2.serialization.SoapObject;

import java.util.Vector;

/**
 * Created by victor on 27/02/15.
 * com.allem.allemevent.async
 */
public class AsyncTaskSoapObjectResultEvent {

    private SoapObject result;
    private int codeRequest, faultcode;
    private String faultstring;
    //private Vector<SoapObject> soapObjectVector;


    public AsyncTaskSoapObjectResultEvent(SoapObject result, int codeRequest,int faultcode, String faultstring) {
        this.result = result;
        this.codeRequest = codeRequest;
        this.faultcode = faultcode;
        this.faultstring = faultstring;
    }



    public SoapObject getResult() {
       /* SoapObject response = (SoapObject)envelope.getResponse();
        String resp = response.toString();*/

        return result;
    }

    public int getCodeRequest(){
        return codeRequest;
    }

    public int getFaultCode(){
        return faultcode;
    }

    public String getFaultString(){

        return faultstring;
    }


}
