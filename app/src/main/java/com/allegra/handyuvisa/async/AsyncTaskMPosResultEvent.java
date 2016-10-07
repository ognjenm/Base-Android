package com.allegra.handyuvisa.async;

import android.util.Log;

import java.util.HashMap;

public class AsyncTaskMPosResultEvent {
    private HashMap<String, String> result;
    private String apiName, TAG = "AsyncTaskPosResultEvent";
    private int errorCode;

    public AsyncTaskMPosResultEvent(HashMap<String, String> result, String apiName) {

        this.result = result;
        this.apiName = apiName;
        Log.d(TAG,"Result");
        for(Object objname:result.keySet()) {
            System.out.println(objname);
            System.out.println(result.get(objname));
        }
    }

    public HashMap<String, String> getResult() {

        for(Object objname:result.keySet()) {
            System.out.println(objname);
            System.out.println(result.get(objname));
        }
        return result;
    }

    public String getApiName() {
        return apiName;
    }

}


