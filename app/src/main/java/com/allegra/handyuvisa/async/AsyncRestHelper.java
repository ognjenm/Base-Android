package com.allegra.handyuvisa.async;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//import java.net.HttpURLConnection;

//TODO remove logging of username and password!

public class AsyncRestHelper extends AsyncTask<Void, Void, HashMap<String, String>> {

    //******************GLOBAL ATTRIBUTES*********************
    private static final String TAG = "AsyncRestHelper";
    private static final int APP_SERVER_CONNECTION_TIMEOUT = 30000;
    private APIInfo apiInfo;

    //***************CONSTRUCTOR*********************
    public AsyncRestHelper(APIInfo apiInfo) {
        this.apiInfo = apiInfo;
    }

    //*******************OVERRIDE METHODS************************
    @Override
    protected void onPostExecute(HashMap<String, String> data) {
        MyBus.getInstance().post(new AsyncTaskMPosResultEvent(data, apiInfo.getApiName()));

    }

    @Override
    protected HashMap<String, String> doInBackground(Void... params) {
        HashMap<String, String> result;

        try {
            result = communicate(apiInfo);
        } catch (IOException e) {
           // Log.e(TAG, "Can't complete task to endpoint", e);
            result = null;
        }
        return result ;
    }

    //*******************PROPER METHODS************************

    public static HashMap<String, String> communicate(APIInfo apiInfo) throws IOException {

        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection urlConnection = null;
        HashMap<String, String> dataReturn = null;
        StringBuilder tmp = new StringBuilder();

        try {
            URL url = new URL(apiInfo.getEndpoint());
            urlConnection = (HttpURLConnection) url.openConnection();
           // Log.d(TAG, "URL: " + apiInfo.getEndpoint());

            urlConnection.setRequestMethod(apiInfo.getMethod());
            urlConnection.setConnectTimeout(APP_SERVER_CONNECTION_TIMEOUT);
            urlConnection.setRequestProperty("Content-Type", apiInfo.getContentType());
            urlConnection.setRequestProperty("Accept", apiInfo.getAccept());
            urlConnection.setRequestProperty("Authorization", apiInfo.getAuthorization());

            HashMap<String, String> headers = apiInfo.getHeaders();
            Set<String> keys = headers.keySet();
            Iterator<String> iter = keys.iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                urlConnection.setRequestProperty(key, headers.get(key));
                tmp.append("[" + key + ": " + headers.get(key) + "] ");
            }

            // TODO REMOVE BEFORE RELEASE
           // Log.d(TAG, "Request Header: " + tmp);


            if (apiInfo.getMethod().equals("DELETE") || apiInfo.getPayload() == null) {
              //  Log.d(TAG,"Llega al if");
                urlConnection.connect();
            } else {
               // Log.d(TAG, "Payload: " + new String(apiInfo.getPayload()));
                urlConnection.setDoOutput(true);
                out = urlConnection.getOutputStream();
                out.write(apiInfo.getPayload());
            }

            int code = urlConnection.getResponseCode();
           // Log.d(TAG, "Response code: " + code);
            if (code == 200 || code == 201 ||  code == 400 ||  code == 404){
                if (code == 400 ||  code == 404){
                    dataReturn = apiInfo.parseData(urlConnection.getErrorStream());
                  //  Log.e(TAG, "HTTPx Response: " + code + " - " + urlConnection.getResponseMessage());
                  //  Log.d(TAG,"Entra al code == 400");
                } else {
                    dataReturn = apiInfo.parseData(urlConnection.getInputStream());
                }

                HashSet set = apiInfo.getParseRespHeaders();
                if (set != null && set.contains("resp_code")) {
                   // Log.d(TAG,"Entra al if set.contains(resp_code)");
                    dataReturn.put("resp_code", Integer.toString(code));
                }
                // Log the HTTP response headers from the server
                Iterator<String> respHeaders = urlConnection.getHeaderFields().keySet().iterator();
                tmp.setLength(0);
                while (respHeaders.hasNext()) {
                    String key = respHeaders.next();
                    if (set != null && set.contains(key)) {
                        List<String> list = urlConnection.getHeaderFields().get(key);
                        if (dataReturn == null) {
                            dataReturn = new HashMap<>();
                        }
                        dataReturn.put(key, list.get(0));
                    }
                    tmp.append(key).append(": ")
                            .append(urlConnection.getHeaderFields().get(key))
                            .append("\n");
                }
               // Log.d(TAG, "Server's response: (" + tmp + ") ");
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                   // Log.e(TAG, "Can't close input stream", e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                   // Log.e(TAG, "Can't close input stream", e);
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return dataReturn;

    }

}
